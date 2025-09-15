package net.alishahidi.mcpconductor.config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import net.alishahidi.mcpconductor.util.SSHConnectionPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(SSHProperties.class)
@Slf4j
public class SSHConfig {

    @Bean
    public JSch jsch() {
        JSch jsch = new JSch();
        JSch.setConfig("StrictHostKeyChecking", "no");
        JSch.setConfig("PreferredAuthentications", "publickey,password");
        return jsch;
    }

    @Bean
    public SSHConnectionPool sshConnectionPool(JSch jsch, SSHProperties properties) {
        return new SSHConnectionPool(jsch, properties);
    }
}