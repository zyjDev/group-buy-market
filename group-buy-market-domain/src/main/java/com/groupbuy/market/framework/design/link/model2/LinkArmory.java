package com.groupbuy.market.framework.design.link.model2;

import com.groupbuy.market.framework.design.link.model2.chain.BusinessLinkedList;
import com.groupbuy.market.framework.design.link.model2.handler.ILogicHandler;

import java.util.Arrays;

/**
 * Builder for a business rule chain.
 */
public class LinkArmory<REQUEST, CONTEXT, RESPONSE> {

    private final String name;
    private final ILogicHandler<REQUEST, CONTEXT, RESPONSE>[] handlers;

    @SafeVarargs
    public LinkArmory(String name, ILogicHandler<REQUEST, CONTEXT, RESPONSE>... handlers) {
        this.name = name;
        this.handlers = handlers;
    }

    public BusinessLinkedList<REQUEST, CONTEXT, RESPONSE> getLogicLink() {
        return new BusinessLinkedList<>(name, Arrays.asList(handlers));
    }

}
