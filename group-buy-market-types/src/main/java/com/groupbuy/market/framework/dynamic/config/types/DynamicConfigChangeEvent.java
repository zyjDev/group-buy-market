package com.groupbuy.market.framework.dynamic.config.types;

import org.springframework.context.ApplicationEvent;

/**
 * Local event emitted when a dynamic configuration value changes.
 */
public class DynamicConfigChangeEvent extends ApplicationEvent {

    private final String key;
    private final String value;

    public DynamicConfigChangeEvent(String key, String value) {
        super(key);
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

}
