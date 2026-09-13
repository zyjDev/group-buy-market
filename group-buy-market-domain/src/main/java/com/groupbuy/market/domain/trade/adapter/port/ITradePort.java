package com.groupbuy.market.domain.trade.adapter.port;

import com.groupbuy.market.domain.trade.model.entity.NotifyTaskEntity;

/**
 * @description 交易接口服务接口
 * @create 2025-01-31 10:38
 */
public interface ITradePort {

    String groupBuyNotify(NotifyTaskEntity notifyTask) throws Exception;

}
