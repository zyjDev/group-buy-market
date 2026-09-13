package com.groupbuy.market.framework.dynamic.config.types;

/**
 * Publishes dynamic configuration changes to all application instances.
 */
public interface DynamicConfigPublisher {

    void publish(String key, String value);

}
