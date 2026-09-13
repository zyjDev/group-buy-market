package com.groupbuy.market.api;

import com.groupbuy.market.api.response.Response;

/**
 * @description DCC 动态配置中心
 * @create 2025-01-03 19:16
 */
public interface IDCCService {

    Response<Boolean> updateConfig(String key, String value);

}
