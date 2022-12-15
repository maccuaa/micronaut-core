package io.micronaut.http.filter;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.execution.ExecutionFlow;
import io.micronaut.core.order.Ordered;
import io.micronaut.core.type.Executable;
import io.micronaut.core.util.Toggleable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import org.reactivestreams.Publisher;
import reactor.util.context.Context;

import java.util.concurrent.Executor;

/**
 * Base interface for different filter types. Note that while the base interface is exposed, so you
 * can pass around instances of these filters, the different implementations are internal only.
 * Only the framework should construct or call instances of this interface. The exception is the
 * {@link Terminal terminal filter}.
 *
 * @since 4.0.0
 * @author Jonas Konrad
 */
public sealed interface GenericHttpFilter {
    /**
     * Method annotated with {@link io.micronaut.http.annotation.RequestFilter}. Contrary to the
     * name, such a method may also accept a {@link FilterContinuation} that will also intercept
     * the response.
     *
     * @param bean The filter bean
     * @param method The filter method
     * @param order The filter order
     * @param <T> The filter bean type
     */
    @Internal
    record Before<T>(
        T bean,
        Executable<T, ?> method,
        FilterOrder order
    ) implements GenericHttpFilter, Ordered {
        @Override
        public int getOrder() {
            return order.getOrder(bean);
        }
    }

    /**
     * Method annotated with {@link io.micronaut.http.annotation.ResponseFilter}.
     *
     * @param bean The filter bean
     * @param method The filter method
     * @param order The filter order
     * @param <T> The filter bean type
     */
    @Internal
    record After<T>(
        T bean,
        Executable<T, ?> method,
        FilterOrder order
    ) implements GenericHttpFilter, Ordered {
        @Override
        public int getOrder() {
            return order.getOrder(bean);
        }
    }

    /**
     * Wrapper around a filter that signifies the filter should be run asynchronously on the given
     * executor. Usually from an {@link io.micronaut.scheduling.annotation.ExecuteOn} annotation.
     */
    @Internal
    record Async(
        GenericHttpFilter actual,
        Executor executor
    ) implements GenericHttpFilter, Ordered {
        @Override
        public int getOrder() {
            return ((Ordered) actual).getOrder();
        }
    }

    /**
     * "Legacy" filter, i.e. filter bean that implements {@link HttpFilter}.
     *
     * @param bean The filter bean
     * @param order The filter order
     */
    @Internal
    record AroundLegacy(
        HttpFilter bean,
        FilterOrder order
    ) implements GenericHttpFilter, Ordered {
        public boolean isEnabled() {
            return !(bean instanceof Toggleable t) || t.isEnabled();
        }

        @Override
        public int getOrder() {
            return order.getOrder(bean);
        }
    }

    /**
     * Terminal filter that accepts a reactive type. Used as a temporary solution for the http
     * client, until that is un-reactified.
     *
     * @param responsePublisher The response publisher
     */
    @Internal
    record TerminalReactive(Publisher<? extends HttpResponse<?>> responsePublisher) implements GenericHttpFilter {}

    /**
     * Like {@link Terminal}, with an additional parameter for the reactive context.
     */
    @Internal
    @FunctionalInterface
    non-sealed interface TerminalWithReactorContext extends GenericHttpFilter {
        ExecutionFlow<? extends HttpResponse<?>> execute(HttpRequest<?> request, Context context) throws Exception;
    }

    /**
     * Last item in a filter chain, called when all other filters are done. Basically, this runs
     * the actual request.
     */
    @FunctionalInterface
    non-sealed interface Terminal extends GenericHttpFilter {
        ExecutionFlow<? extends HttpResponse<?>> execute(HttpRequest<?> request) throws Exception;
    }
}