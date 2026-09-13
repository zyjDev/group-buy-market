package com.groupbuy.market.framework.dynamic.config.domain.model.valobj;

import java.io.Serializable;

/**
 * Dynamic configuration change payload.
 */
public class AttributeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String key;
    private String value;

    public AttributeVO() {
    }

    public AttributeVO(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
