package com.miaoheritage.service.impl;

import com.miaoheritage.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Value("${file.allowed-types}")
    private String allowedTypesString;
    
    private List<String> allowedTypes;
    
    /**
     * 存储文件
     */
    @Override
    public String storeFile(MultipartFile file) {
        if (allowedTypes == null) {
            allowedTypes = Arrays.asList(allowedTypesString.split(","));
        }
        
        try {
            // 检查文件类型
            String fileExtension = getFileExtension(file);
            if (!allowedTypes.contains(fileExtension.toLowerCase())) {
                throw new IllegalArgumentException("不支持的文件类型: " + fileExtension);
            }
            
            // 创建上传目录
            String directory = uploadDir + "/uploads";
            Path dirPath = Paths.get(directory);
            Files.createDirectories(dirPath);
            
            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + "." + fileExtension;
            Path targetPath = dirPath.resolve(fileName);
            
            // 保存文件
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 返回相对URL路径
            return "/uploads/" + fileName;
        } catch (IOException ex) {
            log.error("文件存储失败", ex);
            throw new RuntimeException("文件存储失败: " + ex.getMessage());
        }
    }
    
    /**
     * 存储商品图片
     */
    public String storeProductImage(MultipartFile file, Long productId) {
        if (allowedTypes == null) {
            allowedTypes = Arrays.asList(allowedTypesString.split(","));
        }
        
        try {
            // 检查文件类型
            String fileExtension = getFileExtension(file);
            if (!allowedTypes.contains(fileExtension.toLowerCase())) {
                throw new IllegalArgumentException("不支持的文件类型: " + fileExtension);
            }
            
            // 创建商品图片目录
            String productDir = uploadDir + "/products/" + productId;
            Path productPath = Paths.get(productDir);
            Files.createDirectories(productPath);
            
            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + "." + fileExtension;
            Path targetPath = productPath.resolve(fileName);
            
            // 保存文件
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 返回相对URL路径
            return "/images/products/" + productId + "/" + fileName;
        } catch (IOException ex) {
            log.error("商品图片存储失败", ex);
            throw new RuntimeException("商品图片存储失败: " + ex.getMessage());
        }
    }
    
    /**
     * 加载文件资源
     */
    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath;
            if (fileName.startsWith("/images/products/")) {
                filePath = Paths.get(uploadDir + fileName);
            } else if (fileName.startsWith("/uploads/")) {
                filePath = Paths.get(uploadDir + fileName);
            } else {
                filePath = Paths.get(uploadDir + "/uploads/" + fileName);
            }
            
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                log.error("文件不存在: {}", fileName);
                throw new RuntimeException("文件不存在: " + fileName);
            }
        } catch (MalformedURLException ex) {
            log.error("文件路径错误", ex);
            throw new RuntimeException("文件路径错误: " + ex.getMessage());
        }
    }
    
    /**
     * 删除文件
     */
    @Override
    public boolean deleteFile(String fileName) {
        try {
            Path filePath;
            if (fileName.startsWith("/images/products/")) {
                filePath = Paths.get(uploadDir + fileName);
            } else if (fileName.startsWith("/uploads/")) {
                filePath = Paths.get(uploadDir + fileName);
            } else {
                filePath = Paths.get(uploadDir + "/uploads/" + fileName);
            }
            
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.error("删除文件失败", ex);
            return false;
        }
    }
    
    /**
     * 获取所有文件
     */
    @Override
    public Stream<Path> loadAll() {
        try {
            Path uploadsPath = Paths.get(uploadDir + "/uploads");
            return Files.walk(uploadsPath, 1)
                    .filter(path -> !path.equals(uploadsPath));
        } catch (IOException ex) {
            log.error("获取所有文件失败", ex);
            throw new RuntimeException("获取所有文件失败: " + ex.getMessage());
        }
    }
    
    /**
     * 将文件URL转换为MultipartFile
     */
    @Override
    public MultipartFile getFileAsMultipartFile(String fileUrl) {
        try {
            Path filePath;
            if (fileUrl.startsWith("/images/products/")) {
                filePath = Paths.get(uploadDir + fileUrl);
            } else if (fileUrl.startsWith("/uploads/")) {
                filePath = Paths.get(uploadDir + fileUrl);
            } else {
                filePath = Paths.get(uploadDir + "/uploads/" + fileUrl);
            }
            
            File file = filePath.toFile();
            if (!file.exists()) {
                throw new RuntimeException("文件不存在: " + fileUrl);
            }
            
            FileInputStream input = new FileInputStream(file);
            String fileName = file.getName();
            String contentType = Files.probeContentType(filePath);
            
            return new MockMultipartFile(
                    fileName,
                    fileName,
                    contentType,
                    input
            );
        } catch (IOException ex) {
            log.error("文件转换失败", ex);
            throw new RuntimeException("文件转换失败: " + ex.getMessage());
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
} 