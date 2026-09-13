package com.groupbuy.market.domain.trade.service.refund.factory;

import com.groupbuy.market.domain.trade.model.entity.*;
import com.groupbuy.market.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
import com.groupbuy.market.domain.trade.service.lock.filter.ActivityUsabilityRuleFilter;
import com.groupbuy.market.domain.trade.service.lock.filter.TeamStockOccupyRuleFilter;
import com.groupbuy.market.domain.trade.service.lock.filter.UserTakeLimitRuleFilter;
import com.groupbuy.market.domain.trade.service.refund.filter.DataNodeFilter;
import com.groupbuy.market.domain.trade.service.refund.filter.RefundOrderNodeFilter;
import com.groupbuy.market.domain.trade.service.refund.filter.UniqueRefundNodeFilter;
import com.groupbuy.market.framework.design.link.model2.LinkArmory;
import com.groupbuy.market.framework.design.link.model2.chain.BusinessLinkedList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 * 交易退单工程
 *
 * 2025/7/30 09:58
 */
@Slf4j
@Service
public class TradeRefundRuleFilterFactory {

    @Bean("tradeRefundRuleFilter")
    public BusinessLinkedList<TradeRefundCommandEntity, TradeRefundRuleFilterFactory.DynamicContext, TradeRefundBehaviorEntity> tradeRefundRuleFilter(
            DataNodeFilter dataNodeFilter,
            UniqueRefundNodeFilter uniqueRefundNodeFilter,
            RefundOrderNodeFilter refundOrderNodeFilter) {

        // 组装链
        LinkArmory<TradeRefundCommandEntity, TradeRefundRuleFilterFactory.DynamicContext, TradeRefundBehaviorEntity> linkArmory =
                new LinkArmory<>("退单规则过滤链",
                        dataNodeFilter,
                        uniqueRefundNodeFilter,
                        refundOrderNodeFilter);

        // 链对象
        return linkArmory.getLogicLink();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        private MarketPayOrderEntity marketPayOrderEntity;

        private GroupBuyTeamEntity groupBuyTeamEntity;

    }

}
