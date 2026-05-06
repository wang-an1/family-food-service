package com.familyfood.ai.support;

import com.familyfood.common.AppException;
import java.net.InetAddress;
import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class UrlSafety {
    public void validatePublicHttpUrl(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        URI uri;
        try {
            uri = URI.create(url);
        } catch (Exception ex) {
            throw AppException.validation("链接格式不正确");
        }
        if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
            throw AppException.validation("只支持 HTTP 或 HTTPS 链接");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw AppException.validation("链接缺少域名");
        }
        String lowerHost = host.toLowerCase();
        if ("localhost".equals(lowerHost) || lowerHost.endsWith(".local")) {
            throw AppException.validation("链接解析禁止访问本机或内网地址");
        }
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                        || address.isSiteLocalAddress() || address.isMulticastAddress()) {
                    throw AppException.validation("链接解析禁止访问本机或内网地址");
                }
                String ip = address.getHostAddress();
                if (ip.startsWith("169.254.") || ip.equals("169.254.169.254")) {
                    throw AppException.validation("链接解析禁止访问云元数据地址");
                }
            }
        } catch (AppException ex) {
            throw ex;
        } catch (Exception ex) {
            throw AppException.validation("链接域名无法解析");
        }
    }
}
