package com.groupbuy.market.framework.design.link.model2.handler;

import com.groupbuy.market.framework.design.link.model2.chain.LinkContext;

/**
 * A handler in an ordered business rule chain.
 */
public interface ILogicHandler<REQUEST, CONTEXT, RESPONSE> {

    RESPONSE apply(REQUEST requestParameter, CONTEXT dynamicContext) throws Exception;

    default RESPONSE next(REQUEST requestParameter, CONTEXT dynamicContext) throws Exception {
        return LinkContext.next(requestParameter, dynamicContext);
    }

}
