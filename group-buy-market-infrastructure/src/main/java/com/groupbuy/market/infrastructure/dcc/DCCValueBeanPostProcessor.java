package com.groupbuy.market.infrastructure.dcc;

import com.groupbuy.market.framework.dynamic.config.types.DynamicConfigChangeEvent;
import com.groupbuy.market.framework.dynamic.config.types.annotations.DCCValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Applies defaults and runtime updates to fields annotated with DCCValue.
 */
@Slf4j
@Component
public class DCCValueBeanPostProcessor
        implements BeanPostProcessor, ApplicationListener<DynamicConfigChangeEvent> {

    private final Map<String, List<FieldBinding>> bindings = new ConcurrentHashMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        while (targetClass != null && targetClass != Object.class) {
            for (Field field : targetClass.getDeclaredFields()) {
                DCCValue annotation = field.getAnnotation(DCCValue.class);
                if (annotation == null) {
                    continue;
                }

                ConfigValue configValue = ConfigValue.parse(annotation.value());
                field.setAccessible(true);
                setField(bean, field, configValue.getDefaultValue());
                bindings.computeIfAbsent(configValue.getKey(), key -> new CopyOnWriteArrayList<>())
                        .add(new FieldBinding(bean, field));
            }
            targetClass = targetClass.getSuperclass();
        }
        return bean;
    }

    @Override
    public void onApplicationEvent(DynamicConfigChangeEvent event) {
        List<FieldBinding> fieldBindings = bindings.get(event.getKey());
        if (fieldBindings == null) {
            return;
        }

        for (FieldBinding binding : fieldBindings) {
            setField(binding.bean, binding.field, event.getValue());
        }
    }

    private void setField(Object bean, Field field, String value) {
        try {
            field.set(bean, value);
        } catch (IllegalAccessException e) {
            log.error("动态配置字段写入失败 {}#{}", bean.getClass().getName(), field.getName(), e);
        }
    }

    private static final class FieldBinding {

        private final Object bean;
        private final Field field;

        private FieldBinding(Object bean, Field field) {
            this.bean = bean;
            this.field = field;
        }

    }

    private static final class ConfigValue {

        private final String key;
        private final String defaultValue;

        private ConfigValue(String key, String defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        private static ConfigValue parse(String expression) {
            int separator = expression.indexOf(':');
            if (separator < 0) {
                return new ConfigValue(expression, null);
            }
            return new ConfigValue(expression.substring(0, separator), expression.substring(separator + 1));
        }

        private String getKey() {
            return key;
        }

        private String getDefaultValue() {
            return defaultValue;
        }

    }

}
