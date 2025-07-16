package com.miaoheritage.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QRCodeService {
    
    @Value("${qrcode.width:300}")
    private int width;
    
    @Value("${qrcode.height:300}")
    private int height;
    
    @Value("${qrcode.format:PNG}")
    private String format;
    
    @Value("${qrcode.trace-url}")
    private String traceUrl;
    
    /**
     * 为指定资产ID生成溯源二维码
     *
     * @param assetId 资产ID
     * @return 二维码图片的字节数组
     * @throws WriterException 编码错误
     * @throws IOException IO错误
     */
    public byte[] generateQRCode(String assetId) throws WriterException, IOException {
        // 构建溯源URL
        String traceabilityUrl = traceUrl + assetId;
        
        // 设置二维码参数
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // 高纠错级别
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2); // 边距
        
        // 生成二维码矩阵
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(traceabilityUrl, BarcodeFormat.QR_CODE, width, height, hints);
        
        // 转换为图像
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, format, outputStream);
        
        return outputStream.toByteArray();
    }
} 