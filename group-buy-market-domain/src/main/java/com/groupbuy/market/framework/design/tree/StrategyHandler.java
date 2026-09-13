package com.groupbuy.market.framework.design.tree;

/**
 * A node in a request processing strategy tree.
 */
@FunctionalInterface
public interface StrategyHandler<REQUEST, CONTEXT, RESPONSE> {

    RESPONSE apply(REQUEST requestParameter, CONTEXT dynamicContext) throws Exception;

}
