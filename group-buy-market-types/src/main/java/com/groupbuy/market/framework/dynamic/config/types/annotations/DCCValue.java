package com.groupbuy.market.framework.dynamic.config.types.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a field to a dynamically updatable configuration key.
 * The value format is "key:defaultValue".
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DCCValue {

    String value();

}
