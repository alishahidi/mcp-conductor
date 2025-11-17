package net.alishahidi.mcpconductor.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import net.alishahidi.mcpconductor.util.PlatformDetector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DockerConfig {

    private final PlatformDetector platformDetector;

    @Value("${docker.host:}")
    private String dockerHost;

    @Value("${docker.api.version:}")
    private String apiVersion;

    @Value("${docker.registry.url:}")
    private String registryUrl;

    @Value("${docker.registry.username:}")
    private String registryUsername;

    @Value("${docker.registry.password:}")
    private String registryPassword;

    @Value("${docker.tls.verify:false}")
    private boolean tlsVerify;

    @Value("${docker.cert.path:}")
    private String certPath;

    @Bean
    public DockerClient dockerClient() {
        // Use platform-specific Docker host if not explicitly configured
        String effectiveDockerHost = (dockerHost == null || dockerHost.isBlank())
                ? platformDetector.getDefaultDockerHost()
                : dockerHost;

        log.info("Initializing Docker client for platform: {} with host: {}",
                platformDetector.getCurrentPlatform(), effectiveDockerHost);

        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(effectiveDockerHost)
                .withDockerTlsVerify(tlsVerify)
                .withDockerCertPath(certPath)
                .withApiVersion(apiVersion)
                .withRegistryUrl(registryUrl)
                .withRegistryUsername(registryUsername)
                .withRegistryPassword(registryPassword)
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        DockerClient client = DockerClientBuilder.getInstance(config)
                .withDockerHttpClient(httpClient)
                .build();

        log.info("Docker client initialized successfully for host: {}", effectiveDockerHost);
        return client;
    }
}
