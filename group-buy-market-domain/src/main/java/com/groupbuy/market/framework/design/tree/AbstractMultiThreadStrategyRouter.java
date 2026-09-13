package com.groupbuy.market.framework.design.tree;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Base strategy router with an optional data loading stage before each node.
 */
public abstract class AbstractMultiThreadStrategyRouter<REQUEST, CONTEXT, RESPONSE>
        implements StrategyHandler<REQUEST, CONTEXT, RESPONSE> {

    protected final StrategyHandler<REQUEST, CONTEXT, RESPONSE> defaultStrategyHandler =
            (requestParameter, dynamicContext) -> null;

    @Override
    public final RESPONSE apply(REQUEST requestParameter, CONTEXT dynamicContext) throws Exception {
        multiThread(requestParameter, dynamicContext);
        return doApply(requestParameter, dynamicContext);
    }

    protected void multiThread(REQUEST requestParameter, CONTEXT dynamicContext)
            throws ExecutionException, InterruptedException, TimeoutException {
        // Optional hook for nodes that preload data in parallel.
    }

    protected abstract RESPONSE doApply(REQUEST requestParameter, CONTEXT dynamicContext) throws Exception;

    protected RESPONSE router(REQUEST requestParameter, CONTEXT dynamicContext) throws Exception {
        return get(requestParameter, dynamicContext).apply(requestParameter, dynamicContext);
    }

    public abstract StrategyHandler<REQUEST, CONTEXT, RESPONSE> get(REQUEST requestParameter, CONTEXT dynamicContext) throws Exception;

}
