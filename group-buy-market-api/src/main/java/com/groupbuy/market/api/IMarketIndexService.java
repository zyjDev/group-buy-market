package com.groupbuy.market.api;

import com.groupbuy.market.api.dto.GoodsMarketRequestDTO;
import com.groupbuy.market.api.dto.GoodsMarketResponseDTO;
import com.groupbuy.market.api.response.Response;

/**
 * @description 营销首页服务接口
 * @create 2025-02-02 16:02
 */
public interface IMarketIndexService {

    /**
     * 查询拼团营销配置
     *
     * @param goodsMarketRequestDTO 营销商品信息
     * @return 营销配置信息
     */
    Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfig(GoodsMarketRequestDTO goodsMarketRequestDTO);

}
