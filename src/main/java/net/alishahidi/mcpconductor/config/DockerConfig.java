package net.alishahidi.mcpconductor.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Configuration
@Slf4j
public class DockerConfig {

    @Value("${docker.host:unix:///var/run/docker.sock}")
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
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
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

        log.info("Docker client initialized for host: {}", dockerHost);
        return client;
    }
}
