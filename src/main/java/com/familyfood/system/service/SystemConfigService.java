package com.familyfood.system.service;

import com.familyfood.system.api.SystemConfigApi;
import com.familyfood.system.dto.ConfigResponse;
import com.familyfood.system.dto.UpdateRequest;
import java.util.List;

public interface SystemConfigService extends SystemConfigApi {
    List<ConfigResponse> list();

    List<ConfigResponse> update(UpdateRequest request);

    boolean bool(String key, boolean defaultValue);

    String value(String key, String defaultValue);

    String secretValue(String key, String defaultValue);
}
