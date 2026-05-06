package com.familyfood.ai.support;

import com.familyfood.common.Enums.SourceType;
import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class SourceTypeDetector {
    public SourceType detect(String url, String text, String imageUrl) {
        if (url != null && !url.isBlank()) {
            String host = host(url);
            if (host.contains("douyin.com") || host.contains("iesdouyin.com")) {
                return SourceType.DOUYIN;
            }
            if (host.contains("xiaohongshu.com") || host.contains("xhslink.com")) {
                return SourceType.XIAOHONGSHU;
            }
            if (host.contains("kuaishou.com") || host.contains("gifshow.com")) {
                return SourceType.KUAISHOU;
            }
            if (url.startsWith("http://") || url.startsWith("https://")) {
                return SourceType.WEB;
            }
            return SourceType.UNKNOWN;
        }
        if (imageUrl != null && !imageUrl.isBlank()) {
            return SourceType.IMAGE;
        }
        if (text != null && !text.isBlank()) {
            return SourceType.TEXT;
        }
        return SourceType.UNKNOWN;
    }

    private String host(String url) {
        try {
            URI uri = URI.create(url);
            return uri.getHost() == null ? "" : uri.getHost().toLowerCase();
        } catch (Exception ex) {
            return "";
        }
    }
}
