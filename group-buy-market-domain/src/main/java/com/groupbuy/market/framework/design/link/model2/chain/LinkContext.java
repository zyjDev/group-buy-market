package com.groupbuy.market.framework.design.link.model2.chain;

import com.groupbuy.market.framework.design.link.model2.handler.ILogicHandler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Keeps the active chain on the current thread so handlers can invoke next().
 */
public final class LinkContext {

    private static final ThreadLocal<Deque<Invocation>> INVOCATIONS =
            ThreadLocal.withInitial(ArrayDeque::new);

    private LinkContext() {
    }

    static <REQUEST, CONTEXT, RESPONSE> RESPONSE apply(
            List<ILogicHandler<REQUEST, CONTEXT, RESPONSE>> handlers,
            REQUEST requestParameter,
            CONTEXT dynamicContext) throws Exception {
        Deque<Invocation> invocations = INVOCATIONS.get();
        invocations.push(new Invocation(handlers));
        try {
            return invoke(handlers, 0, requestParameter, dynamicContext);
        } finally {
            invocations.pop();
            if (invocations.isEmpty()) {
                INVOCATIONS.remove();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <REQUEST, CONTEXT, RESPONSE> RESPONSE next(
            REQUEST requestParameter,
            CONTEXT dynamicContext) throws Exception {
        Deque<Invocation> invocations = INVOCATIONS.get();
        Invocation invocation = invocations.peek();
        if (invocation == null) {
            throw new IllegalStateException("No active business rule chain");
        }

        int nextIndex = ++invocation.index;
        if (nextIndex >= invocation.handlers.size()) {
            throw new IllegalStateException("No next logic handler in business rule chain");
        }

        return (RESPONSE) invokeRaw(invocation.handlers, nextIndex, requestParameter, dynamicContext);
    }

    private static <REQUEST, CONTEXT, RESPONSE> RESPONSE invoke(
            List<ILogicHandler<REQUEST, CONTEXT, RESPONSE>> handlers,
            int index,
            REQUEST requestParameter,
            CONTEXT dynamicContext) throws Exception {
        return handlers.get(index).apply(requestParameter, dynamicContext);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object invokeRaw(
            List<?> handlers,
            int index,
            Object requestParameter,
            Object dynamicContext) throws Exception {
        return ((ILogicHandler) handlers.get(index)).apply(requestParameter, dynamicContext);
    }

    private static final class Invocation {

        private final List<?> handlers;
        private int index;

        private Invocation(List<?> handlers) {
            this.handlers = handlers;
        }

    }

}
