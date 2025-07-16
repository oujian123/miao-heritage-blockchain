package com.miaoheritage.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * 文件存储服务接口
 */
public interface FileStorageService {

    /**
     * 存储文件
     * 
     * @param file 要存储的文件
     * @return 存储后的文件URL
     */
    String storeFile(MultipartFile file);

    /**
     * 加载文件
     * 
     * @param fileName 文件名
     * @return 文件资源
     */
    Resource loadFileAsResource(String fileName);
    
    /**
     * 删除文件
     * 
     * @param fileName 文件名
     * @return 是否删除成功
     */
    boolean deleteFile(String fileName);
    
    /**
     * 获取所有文件
     * 
     * @return 文件路径流
     */
    Stream<Path> loadAll();
    
    /**
     * 将文件URL转换为MultipartFile
     * 
     * @param fileUrl 文件URL
     * @return MultipartFile对象
     */
    MultipartFile getFileAsMultipartFile(String fileUrl);
} 