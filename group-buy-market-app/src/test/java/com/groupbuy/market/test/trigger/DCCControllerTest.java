package com.groupbuy.market.test.trigger;

import com.groupbuy.market.api.IDCCService;
import com.groupbuy.market.domain.activity.model.entity.MarketProductEntity;
import com.groupbuy.market.domain.activity.model.entity.TrialBalanceEntity;
import com.groupbuy.market.domain.activity.service.IIndexGroupBuyMarketService;
import com.groupbuy.market.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @description 动态配置管理测试
 * @create 2025-01-03 19:43
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class DCCControllerTest {

    @Resource
    private IDCCService dccService;

    @Resource
    private IIndexGroupBuyMarketService indexGroupBuyMarketService;

    @Test
    public void test_updateConfig() {
        // 动态调整配置
        try {
            dccService.updateConfig("downgradeSwitch", "1");
        } finally {
            dccService.updateConfig("downgradeSwitch", "0");
        }
    }

    @Test
    public void test_updateConfig2indexMarketTrial() throws Exception {
        // 动态调整配置
        try {
            dccService.updateConfig("downgradeSwitch", "1");
        // 超时等待异步
            Thread.sleep(1000);

        // 营销验证
        MarketProductEntity marketProductEntity = new MarketProductEntity();
        marketProductEntity.setUserId("user01");
        marketProductEntity.setSource("s01");
        marketProductEntity.setChannel("c01");
        marketProductEntity.setGoodsId("9890001");

            try {
                indexGroupBuyMarketService.indexMarketTrial(marketProductEntity);
                fail("降级开关开启时不应继续执行拼团试算");
            } catch (AppException e) {
                assertEquals("E0003", e.getCode());
            }
        } finally {
            dccService.updateConfig("downgradeSwitch", "0");
        }
    }


}
