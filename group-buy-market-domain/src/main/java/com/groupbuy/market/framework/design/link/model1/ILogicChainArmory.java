package com.groupbuy.market.framework.design.link.model1;

/**
 * Supports assembling a chain of logic nodes.
 */
public interface ILogicChainArmory<T, D, R> {

    ILogicLink<T, D, R> next();

    ILogicLink<T, D, R> appendNext(ILogicLink<T, D, R> next);

}
