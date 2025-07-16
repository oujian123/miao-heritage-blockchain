package com.miaoheritage.blockchain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "fabric")
@Data
public class FabricConfig {
    private String networkConfigPath;
    private String channelName;
    private String chaincodeName;
    private String walletDirectory;
    private String organization;
    private String userName;
} 