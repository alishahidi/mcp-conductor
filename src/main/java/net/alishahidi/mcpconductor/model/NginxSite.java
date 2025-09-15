package net.alishahidi.mcpconductor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NginxSite {
    private String siteName;
    private String serverName;
    private int port;
    private int sslPort;
    private boolean sslEnabled;
    private String documentRoot;
    private String proxyPass;
    private String sslCertificate;
    private String sslCertificateKey;
    private String accessLog;
    private String errorLog;
    private List<Location> locations;
    private Map<String, String> customDirectives;
    private boolean enabled;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProxyConfig {
        private String upstreamUrl;
        private int timeout;
        private Map<String, String> headers;
        private boolean websocketSupport;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private String path;
        private String root;
        private String proxy;
        private List<String> allowMethods;
        private Map<String, String> headers;
    }
}