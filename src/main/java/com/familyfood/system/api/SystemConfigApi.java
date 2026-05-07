package com.familyfood.system.api;

public interface SystemConfigApi {
    boolean bool(String key, boolean defaultValue);

    String value(String key, String defaultValue);

    String secretValue(String key, String defaultValue);
}
