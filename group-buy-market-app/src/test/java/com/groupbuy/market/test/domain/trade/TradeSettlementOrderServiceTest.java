package com.groupbuy.market.test.domain.trade;

import com.groupbuy.market.domain.trade.model.entity.TradePaySettlementEntity;
import com.groupbuy.market.domain.trade.model.entity.TradePaySuccessEntity;
import com.groupbuy.market.domain.trade.service.ITradeSettlementOrderService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @description 拼团交易结算服务测试
 * @create 2025-01-26 18:59
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class TradeSettlementOrderServiceTest {

    @Resource
    private ITradeSettlementOrderService tradeSettlementOrderService;

    @Test
    public void test_settlementMarketPayOrder() throws Exception {
        TradePaySuccessEntity tradePaySuccessEntity = new TradePaySuccessEntity();
        tradePaySuccessEntity.setSource("s01");
        tradePaySuccessEntity.setChannel("c01");
        tradePaySuccessEntity.setUserId("GROUP_BUY03");
        tradePaySuccessEntity.setOutTradeNo("769515763172");
        tradePaySuccessEntity.setOutTradeTime(Date.from(LocalDateTime.of(2025, 4, 5, 14, 55)
                .atZone(ZoneId.systemDefault()).toInstant()));
        TradePaySettlementEntity tradePaySettlementEntity = tradeSettlementOrderService.settlementMarketPayOrder(tradePaySuccessEntity);
        log.info("请求参数:{}", JSON.toJSONString(tradePaySuccessEntity));
        log.info("测试结果:{}", JSON.toJSONString(tradePaySettlementEntity));

        // 暂停，等待MQ消息。处理完后，手动关闭程序
        // 为异步消息处理留出短暂窗口，避免自动化测试永久等待
        new CountDownLatch(1).await(3, TimeUnit.SECONDS);
    }

}
