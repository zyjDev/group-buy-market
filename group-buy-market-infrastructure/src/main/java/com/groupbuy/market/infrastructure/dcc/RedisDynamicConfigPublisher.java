package com.groupbuy.market.infrastructure.dcc;

import com.groupbuy.market.framework.dynamic.config.domain.model.valobj.AttributeVO;
import com.groupbuy.market.framework.dynamic.config.types.DynamicConfigChangeEvent;
import com.groupbuy.market.framework.dynamic.config.types.DynamicConfigPublisher;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Propagates dynamic configuration changes through Redis and local events.
 */
@Slf4j
@Component
public class RedisDynamicConfigPublisher implements DynamicConfigPublisher, DisposableBean {

    private static final String TOPIC_NAME = "group_buy_market_dynamic_config_topic";

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    private RTopic topic;
    private int listenerId;

    @PostConstruct
    public void init() {
        try {
            topic = redissonClient.getTopic(TOPIC_NAME);
            listenerId = topic.addListener(AttributeVO.class, (channel, attribute) ->
                    applicationEventPublisher.publishEvent(
                            new DynamicConfigChangeEvent(attribute.getKey(), attribute.getValue())));
        } catch (Exception e) {
            log.warn("动态配置 Redis 订阅初始化失败，将仅支持当前实例更新", e);
        }
    }

    @Override
    public void publish(String key, String value) {
        if (topic != null) {
            topic.publish(new AttributeVO(key, value));
        }
        applicationEventPublisher.publishEvent(new DynamicConfigChangeEvent(key, value));
    }

    @Override
    public void destroy() {
        if (topic != null && listenerId > 0) {
            topic.removeListener(listenerId);
        }
    }

}
