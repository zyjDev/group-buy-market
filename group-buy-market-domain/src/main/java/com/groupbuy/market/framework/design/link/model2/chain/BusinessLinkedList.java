package com.groupbuy.market.framework.design.link.model2.chain;

import com.groupbuy.market.framework.design.link.model2.handler.ILogicHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable ordered business rule chain.
 */
public class BusinessLinkedList<REQUEST, CONTEXT, RESPONSE> {

    private final String name;
    private final List<ILogicHandler<REQUEST, CONTEXT, RESPONSE>> handlers;

    public BusinessLinkedList(String name, List<ILogicHandler<REQUEST, CONTEXT, RESPONSE>> handlers) {
        this.name = name;
        this.handlers = Collections.unmodifiableList(new ArrayList<>(handlers));
    }

    public RESPONSE apply(REQUEST requestParameter, CONTEXT dynamicContext) throws Exception {
        if (handlers.isEmpty()) {
            return null;
        }
        return LinkContext.apply(handlers, requestParameter, dynamicContext);
    }

    public String getName() {
        return name;
    }

    public List<ILogicHandler<REQUEST, CONTEXT, RESPONSE>> getHandlers() {
        return handlers;
    }

}
