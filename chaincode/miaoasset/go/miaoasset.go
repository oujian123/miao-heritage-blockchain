package main

import (
	"encoding/json"
	"fmt"
	"time"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

// 资产结构体定义
type Asset struct {
	ID             string          `json:"id"`             // 资产唯一ID
	Type           string          `json:"type"`           // 类型：银饰/服饰
	Name           string          `json:"name"`           // 名称
	Description    string          `json:"description"`    // 描述
	Owner          string          `json:"owner"`          // 当前所有者
	Artisan        string          `json:"artisan"`        // 制作匠人
	ArtisanID      string          `json:"artisanId"`      // 匠人身份识别
	MaterialSource string          `json:"materialSource"` // 材料来源
	CreateTime     int64           `json:"createTime"`     // 创建时间戳
	CertHash       string          `json:"certHash"`       // 鉴定证书哈希
	ImageHash      string          `json:"imageHash"`      // 图片哈希
	History        []AssetHistory  `json:"history"`        // 历史记录
	Attributes     map[string]any  `json:"attributes"`     // 扩展属性
}

// 资产历史记录
type AssetHistory struct {
	Timestamp int64  `json:"timestamp"` // 时间戳
	Operation string `json:"operation"` // 操作：created, transferred, certified, etc.
	From      string `json:"from"`      // 操作发起方
	To        string `json:"to"`        // 操作接收方
	Details   string `json:"details"`   // 详细信息
}

// 合约定义
type AssetContract struct {
	contractapi.Contract
}

// 初始化账本
func (c *AssetContract) InitLedger(ctx contractapi.TransactionContextInterface) error {
	// 初始化空账本，不需要添加任何示例资产
	return nil
}

// 创建资产
func (c *AssetContract) CreateAsset(ctx contractapi.TransactionContextInterface, 
	id string, assetType string, name string, description string,
	owner string, artisan string, artisanID string, materialSource string,
	certHash string, imageHash string, attributesJSON string) error {
	
	// 检查资产是否已存在
	exists, err := c.AssetExists(ctx, id)
	if err != nil {
		return fmt.Errorf("检查资产存在失败: %v", err)
	}
	if exists {
		return fmt.Errorf("资产已存在: %s", id)
	}
	
	// 解析扩展属性
	attributes := make(map[string]any)
	if attributesJSON != "" {
		err = json.Unmarshal([]byte(attributesJSON), &attributes)
		if err != nil {
			return fmt.Errorf("解析扩展属性失败: %v", err)
		}
	}
	
	// 创建资产对象
	asset := Asset{
		ID:             id,
		Type:           assetType,
		Name:           name,
		Description:    description,
		Owner:          owner,
		Artisan:        artisan,
		ArtisanID:      artisanID,
		MaterialSource: materialSource,
		CreateTime:     time.Now().UnixMilli(),
		CertHash:       certHash,
		ImageHash:      imageHash,
		History: []AssetHistory{
			{
				Timestamp: time.Now().UnixMilli(),
				Operation: "created",
				From:      artisan,
				To:        owner,
				Details:   "资产初始创建",
			},
		},
		Attributes: attributes,
	}
	
	// 将资产序列化为JSON
	assetJSON, err := json.Marshal(asset)
	if err != nil {
		return fmt.Errorf("资产序列化失败: %v", err)
	}
	
	// 写入账本
	err = ctx.GetStub().PutState(id, assetJSON)
	if err != nil {
		return fmt.Errorf("资产存储失败: %v", err)
	}
	
	return nil
}

// 查询资产
func (c *AssetContract) QueryAsset(ctx contractapi.TransactionContextInterface, id string) (*Asset, error) {
	// 从账本读取资产
	assetJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return nil, fmt.Errorf("资产查询失败: %v", err)
	}
	if assetJSON == nil {
		return nil, fmt.Errorf("资产不存在: %s", id)
	}
	
	// 反序列化为资产对象
	var asset Asset
	err = json.Unmarshal(assetJSON, &asset)
	if err != nil {
		return nil, fmt.Errorf("资产反序列化失败: %v", err)
	}
	
	return &asset, nil
}

// 转移资产所有权
func (c *AssetContract) TransferAsset(ctx contractapi.TransactionContextInterface, id string, newOwner string) error {
	// 查询资产
	asset, err := c.QueryAsset(ctx, id)
	if err != nil {
		return err
	}
	
	// 记录原所有者
	oldOwner := asset.Owner
	
	// 更新所有者
	asset.Owner = newOwner
	
	// 添加历史记录
	asset.History = append(asset.History, AssetHistory{
		Timestamp: time.Now().UnixMilli(),
		Operation: "transferred",
		From:      oldOwner,
		To:        newOwner,
		Details:   fmt.Sprintf("所有权从 %s 转移到 %s", oldOwner, newOwner),
	})
	
	// 序列化并存储更新后的资产
	assetJSON, err := json.Marshal(asset)
	if err != nil {
		return fmt.Errorf("资产序列化失败: %v", err)
	}
	
	err = ctx.GetStub().PutState(id, assetJSON)
	if err != nil {
		return fmt.Errorf("资产更新失败: %v", err)
	}
	
	return nil
}

// 添加资产证书
func (c *AssetContract) CertifyAsset(ctx contractapi.TransactionContextInterface, id string, certifier string, certHash string, details string) error {
	// 查询资产
	asset, err := c.QueryAsset(ctx, id)
	if err != nil {
		return err
	}
	
	// 更新证书哈希
	asset.CertHash = certHash
	
	// 添加历史记录
	asset.History = append(asset.History, AssetHistory{
		Timestamp: time.Now().UnixMilli(),
		Operation: "certified",
		From:      certifier,
		To:        asset.Owner,
		Details:   details,
	})
	
	// 序列化并存储更新后的资产
	assetJSON, err := json.Marshal(asset)
	if err != nil {
		return fmt.Errorf("资产序列化失败: %v", err)
	}
	
	err = ctx.GetStub().PutState(id, assetJSON)
	if err != nil {
		return fmt.Errorf("资产更新失败: %v", err)
	}
	
	return nil
}

// 添加历史记录
func (c *AssetContract) AddHistory(ctx contractapi.TransactionContextInterface, id string, operation string, from string, to string, details string) error {
	// 查询资产
	asset, err := c.QueryAsset(ctx, id)
	if err != nil {
		return err
	}
	
	// 添加历史记录
	asset.History = append(asset.History, AssetHistory{
		Timestamp: time.Now().UnixMilli(),
		Operation: operation,
		From:      from,
		To:        to,
		Details:   details,
	})
	
	// 序列化并存储更新后的资产
	assetJSON, err := json.Marshal(asset)
	if err != nil {
		return fmt.Errorf("资产序列化失败: %v", err)
	}
	
	err = ctx.GetStub().PutState(id, assetJSON)
	if err != nil {
		return fmt.Errorf("资产更新失败: %v", err)
	}
	
	return nil
}

// 更新资产属性
func (c *AssetContract) UpdateAttributes(ctx contractapi.TransactionContextInterface, id string, attributesJSON string) error {
	// 查询资产
	asset, err := c.QueryAsset(ctx, id)
	if err != nil {
		return err
	}
	
	// 解析新属性
	attributes := make(map[string]any)
	if attributesJSON != "" {
		err = json.Unmarshal([]byte(attributesJSON), &attributes)
		if err != nil {
			return fmt.Errorf("解析扩展属性失败: %v", err)
		}
	}
	
	// 更新属性
	if asset.Attributes == nil {
		asset.Attributes = make(map[string]any)
	}
	
	for k, v := range attributes {
		asset.Attributes[k] = v
	}
	
	// 序列化并存储更新后的资产
	assetJSON, err := json.Marshal(asset)
	if err != nil {
		return fmt.Errorf("资产序列化失败: %v", err)
	}
	
	err = ctx.GetStub().PutState(id, assetJSON)
	if err != nil {
		return fmt.Errorf("资产更新失败: %v", err)
	}
	
	return nil
}

// 检查资产是否存在
func (c *AssetContract) AssetExists(ctx contractapi.TransactionContextInterface, id string) (bool, error) {
	assetJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return false, fmt.Errorf("资产查询失败: %v", err)
	}
	
	return assetJSON != nil, nil
}

// 查询所有资产
func (c *AssetContract) GetAllAssets(ctx contractapi.TransactionContextInterface) ([]*Asset, error) {
	resultsIterator, err := ctx.GetStub().GetStateByRange("", "")
	if err != nil {
		return nil, fmt.Errorf("获取资产失败: %v", err)
	}
	defer resultsIterator.Close()
	
	var assets []*Asset
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, fmt.Errorf("迭代资产失败: %v", err)
		}
		
		var asset Asset
		err = json.Unmarshal(queryResponse.Value, &asset)
		if err != nil {
			return nil, fmt.Errorf("资产反序列化失败: %v", err)
		}
		assets = append(assets, &asset)
	}
	
	return assets, nil
}

func main() {
	assetChaincode, err := contractapi.NewChaincode(&AssetContract{})
	if err != nil {
		fmt.Printf("创建智能合约失败: %v", err)
		return
	}
	
	if err := assetChaincode.Start(); err != nil {
		fmt.Printf("启动智能合约失败: %v", err)
	}
} 