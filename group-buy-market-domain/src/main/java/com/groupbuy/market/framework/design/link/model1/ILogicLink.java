package com.groupbuy.market.framework.design.link.model1;

/**
 * A node in an ordered logic chain.
 */
public interface ILogicLink<T, D, R> extends ILogicChainArmory<T, D, R> {

    R apply(T requestParameter, D dynamicContext) throws Exception;

}
