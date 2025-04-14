package org.moodminds.traverse;

import org.moodminds.elemental.Association;
import org.moodminds.elemental.Container;
import org.moodminds.elemental.HashContainer;
import org.moodminds.elemental.KeyValue;
import org.moodminds.function.Evaluable1Throwing1;
import org.moodminds.function.Evaluable2Throwing1;
import org.moodminds.function.Executable1Throwing1;
import org.moodminds.function.Executable1Throwing2;
import org.moodminds.function.Executable1Throwing3;
import org.moodminds.function.Testable1Throwing1;
import org.moodminds.function.Testable1Throwing2;
import org.moodminds.function.Testable1Throwing3;
import org.moodminds.traverse.context.NestContext;
import org.moodminds.traverse.context.SnapContext;
import org.moodminds.valuable.Valuable;
import org.moodminds.valuable.Variable;
import org.moodminds.valuable.Volatile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static org.moodminds.elemental.HashContainer.container;
import static org.moodminds.elemental.Pair.pair;
import static org.moodminds.function.Evaluable1Throwing1.identity;
import static org.moodminds.sneaky.Cast.cast;
import static org.moodminds.sneaky.Sneak.sneak;
import static org.moodminds.traverse.TraverseMethod.*;
import static org.moodminds.valuable.Variable.var;
import static org.moodminds.function.Executable1.idle;
import static org.moodminds.valuable.Volatile.vol;

/**
 * A specialized extension of the {@link TraverseSupport} interface, this class provides
 * default implementations for most of the defined methods and introduces additional methods
 * to extend the API.
 *
 * @param <V> the type of item values
 * @param <E> the type of traversal exception
 */
public interface Traversable<V, E extends Exception> extends TraverseSupport<V, E> {

    /**
     * Try to traverse the Source completely explicitly sequentially with the given {@link KeyValue key-value} varargs context.
     * <p>
     * The specified {@link KeyValue key-values} must not contain {@code null} keys or values to avoid {@link NullPointerException}.
     *
     * @param ctx the given {@link KeyValue} varargs context
     * @throws E                    in case of the traversal error
     * @throws NullPointerException if the specified {@link KeyValue key-values} have {@code null} keys or values
     */
    default void sequence(KeyValue<?, ?>... ctx) throws E {
        sequence(each(idle()), ctx);
    }

    /**
     * Try to traverse the Source completely explicitly sequentially with the given {@link Association} context.
     * <p>
     * The specified {@link Association} must contain non-null keys and values, and its {@link Association#get(Object)}
     * method should raise a {@link NoSuchElementException} instead of returning {@code null} when no value is associated
     * with a key.
     *
     * @param ctx the given {@link Association} context
     * @throws E                    in case of the traversal error
     * @throws NullPointerException if the specified {@link Association} contains {@code null} keys or values
     */
    default void sequence(Association<?, ?, ?> ctx) throws E {
        sequence(each(idle()), ctx);
    }

    /**
     * Try to traverse the Source completely explicitly sequentially returning the collection result by the given {@link Collector}
     * and {@link KeyValue key-value} varargs context.
     * <p>
     * The specified {@link KeyValue key-values} must not contain {@code null} keys or values to avoid {@link NullPointerException}.
     *
     * @param collector the given {@link Collector} collector
     * @param ctx       the given {@link KeyValue} varargs context
     * @param <R>       the type of the collecting result
     * @return the collection result
     * @throws E                    in case of the traversal error
     * @throws NullPointerException if the specified {@link Collector} collector is {@code null} or {@link KeyValue key-values}
     *                              have {@code null} keys or values
     */
    default <R> R sequence(Collector<? super V, ?, ? extends R> collector, KeyValue<?, ?>... ctx) throws E {
        return this.<R, RuntimeException>sequence(reduce(collector), ctx);
    }

    /**
     * Try to traverse the Source completely explicitly sequentially returning the collection result by the given {@link Collector}
     * and {@link Association} context.
     * <p>
     * The specified {@link Association} must contain non-null keys and values, and its {@link Association#get(Object)}
     * method should raise a {@link NoSuchElementException} instead of returning {@code null} when no value is associated
     * with a key.
     *
     * @param collector the given {@link Collector} collector
     * @param ctx       the given {@link Association} context
     * @param <R>       the type of the collecting result
     * @return the collection result
     * @throws E                    in case of the traversal error
     * @throws NullPointerException if the specified {@link Collector} collector is {@code null}
     *                              or {@link Association} contains {@code null} keys or values
     */
    default <R> R sequence(Collector<? super V, ?, ? extends R> collector, Association<?, ?, ?> ctx) throws E {
        return this.<R, RuntimeException>sequence(reduce(collector), ctx);
    }

    /**
     * Try to traverse the Source completely explicitly sequentially returning the result
     * by the given {@link Resolver} and {@link KeyValue key-value} varargs context.
     * <p>
     * The specified {@link KeyValue key-values} must not contain {@code null} keys or values to avoid {@link NullPointerException}.
     *
     * @param resolver the given {@link Resolver} resolver
     * @param ctx      the given {@link KeyValue} varargs context
     * @param <R>      the type of the resolution result
     * @param <H>      the type of the resolution error
     * @return the resolution result
     * @throws E                    in case of the traversal error
     * @throws H                    in case of the {@link Resolver} resolver error
     * @throws NullPointerException if the specified {@link Resolver} resolver is {@code null} or {@link KeyValue key-values}
     *                              have {@code null} keys or values
     */
    default <R, H extends Exception> R sequence(Resolver<? super V, E, ? extends R, ? extends H> resolver, KeyValue<?, ?>... ctx) throws E, H {
        return sequence(resolver, context(ctx));
    }

    /**
     * Try to traverse the Source completely explicitly sequentially returning the result by the given {@link Resolver}
     * and {@link Association} context.
     * <p>
     * The specified {@link Association} must contain non-null keys and values, and its {@link Association#get(Object)}
     * method should raise a {@link NoSuchElementException} instead of returning {@code null} when no value is associated
     * with a key.
     *
     * @param resolver the given {@link Resolver} resolver
     * @param ctx      the given {@link Association} context
     * @param <R>      the type of the resolution result
     * @param <H>      the type of the resolution error
     * @return the resolution result
     * @throws E                    in case of the traversal error
     * @throws H                    in case of the {@link Resolver} resolver error
     * @throws NullPointerException if the specified {@link Resolver} resolver is {@code null}
     *                              or {@link Association} contains {@code null} keys or values
     */
    default <R, H extends Exception> R sequence(Resolver<? super V, E, ? extends R, ? extends H> resolver, Association<?, ?, ?> ctx) throws E, H {
        return resolver.resolve(SEQUENCE, this, ctx);
    }

    /**
     * Try to traverse the Source explicitly sequentially with the given {@link Traverse} segment traverse
     * function and {@link KeyValue key-value} varargs context.
     * <p>
     * The specified {@link KeyValue key-values} must not contain {@code null} keys or values to avoid {@link NullPointerException}.
     *
     * @param traverse the given {@link Traverse} segment traverse function
     * @param ctx      the given {@link KeyValue} varargs context
     * @param <H>      the possible exception type throwing by the traverse function
     * @return the completion flag indicating either the Source was completely traversed, or not
     * @throws E                    in case of the traversal error
     * @throws H                    in the case of traverse function error
     * @throws NullPointerException if the specified traverse function is {@code null} or {@link KeyValue key-values}
     *                              have {@code null} keys or values
     */
    default <H extends Exception> boolean sequence(Traverse<V, E, ? extends E, ? extends H> traverse, KeyValue<?, ?>... ctx) throws E, H {
        return traverse(SEQUENCE, traverse, ctx);
    }

    /**
     * Try to traverse the Source explicitly sequentially with the given {@link Traverse} segment traverse
     * function and {@link Association} context.
     * <p>
     * The specified {@link Association} must contain non-null keys and values, and its {@link Association#get(Object)}
     * method should raise a {@link NoSuchElementException} instead of returning {@code null} when no value is associated
     * with a key.
     *
     * @param traverse the given {@link Traverse} segment traverse function
     * @param ctx      the given {@link Association} context
     * @param <H>      the possible exception type throwing by the traverse function
     * @return the completion flag indicating either the Source was completely traversed, or not
     * @throws E                    in case of the traversal error
     * @throws H                    in the case of traverse function error
     * @throws NullPointerException if the specified traverse function is {@code null} or {@link Association}
     *                              contains {@code null} keys or values
     */
    default <H extends Exception> boolean sequence(Traverse<V, E, ? extends E, ? extends H> traverse, Association<?, ?, ?> ctx) throws E, H {
        return traverse(SEQUENCE, traverse, ctx);
    }


    /**
     * Try to traverse the Source completely either sequentially or in parallel depending on the Source mode with the given
     * {@link KeyValue key-value} varargs context.
     * <p>
     * The specified {@link KeyValue key-values} must not contain {@code null} keys or values to avoid {@link NullPointerException}.
     *
     * @param ctx the given {@link KeyValue} varargs context
     * @throws E                    in case of the traversal error
     * @throws NullPointerException if the specified {@link KeyValue key-values} have {@code null} keys or values
     */
    default void traverse(KeyValue<?, ?>... ctx) throws E {
        traverse(each(idle()), ctx);
    }

    /**
     * Try to traverse the Source completely either sequentially or in parallel depending on the Source mode with the given
     * {@link Association} context.
     * <p>
     * The specified {@link Association} must contain non-null keys and values, and its {@link Association#get(Object)}
     * method should raise a {@link NoSuchElementException} instead of returning {@code null} when no value is associated
     * with a key.
     *
     * @param ctx the given {@link Association} context
     * @throws E                    in case of the traversal error
     * @throws NullPointerException if the specified {@link Association} contains {@code null} keys or values
     */
    default void traverse(Association<?, ?, ?> ctx) throws E {
        traverse(each(idle()), ctx);
    }

    /**
     * Try to traverse the Source completely either sequentially or in parallel depending on the Source mode returning
     * the collection result by the given {@link Collector} and {@link KeyValue key-value} varargs context.
     * <p>
     * The specified {@link KeyValue key-values} must not contain {@code null} keys or values to avoid {@link NullPointerException}.
     *
     * @param collector the given {@link Collector} collector
     * @param ctx       the given {@link KeyValue} varargs context
     * @param <R>       the type of the collecting result
     * @return the collection result
     * @throws E                    in case of the traversal error
     * @throws NullPointerException if the specified {@link Collector} collector is {@code null} or {@link KeyValue key-values}
     *                              have {@code null} keys or values
     */
    default <R> R traverse(Collector<? super V, ?, ? extends R> collector, KeyValue<?, ?>... ctx) throws E {
        return this.<R, RuntimeException>traverse(reduce(collector), ctx);
    }

    /**
     * Try to traverse the Source completely either sequentially or in parallel depending on the Source mode returning
     * the collection result by the given {@link Collector} and {@link Association} context.
     * <p>
     * The specified {@link Association} must contain non-null keys and values, and its {@link Association#get(Object)}
     * method should raise a {@link NoSuchElementException} instead of returning {@code null} when no value is associated
     * with a key.
     *
     * @param collector the given {@link Collector} collector
     * @param ctx       the given {@link Association} context
     * @param <R>       the type of the collecting result
     * @return the collection result
     * @throws E                    in case of the traversal error
     * @throws NullPointerException if the specified {@link Collector} collector is {@code null}
     *                              or {@link Association} contains {@code null} keys or values
     */
    default <R> R traverse(Collector<? super V, ?, ? extends R> collector, Association<?, ?, ?> ctx) throws E {
        return this.<R, RuntimeException>traverse(reduce(collector), ctx);
    }

    /**
     * Try to traverse the Source completely either sequentially or in parallel depending on the Source mode returning
     * the result by the given {@link Resolver} and {@link KeyValue key-value} varargs context.
     * <p>
     * The specified {@link KeyValue key-values} must not contain {@code null} keys or values to avoid {@link NullPointerException}.
     *
     * @param resolver the given {@link Resolver} resolver
     * @param ctx      the given {@link KeyValue} varargs context
     * @param <R>      the type of the resolution result
     * @param <H>      the type of the resolution error
     * @return the resolution result
     * @throws E                    in case of the traversal error
     * @throws H                    in case of the {@link Resolver} resolver error
     * @throws NullPointerException if the specified {@link Resolver} resolver is {@code null} or {@link KeyValue key-values}
     *                              have {@code null} keys or values
     */
    default <R, H extends Exception> R traverse(Resolver<? super V, E, ? extends R, ? extends H> resolver, KeyValue<?, ?>... ctx) throws E, H {
        return traverse(resolver, context(ctx));
    }

    /**
     * Try to traverse the Source completely either sequentially or in parallel depending on the Source mode returning
     * the result by the given {@link Resolver} and {@link Association} context.
     * <p>
     * The specified {@link Association} must contain non-null keys and values, and its {@link Association#get(Object)}
     * method should raise a {@link NoSuchElementException} instead of returning {@code null} when no value is associated
     * with a key.
     *
     * @param resolver the given {@link Resolver} resolver
     * @param ctx      the given {@link Association} context
     * @param <R>      the type of the resolution result
     * @param <H>      the type of the resolution error
     * @return the resolution result
     * @throws E                    in case of the traversal error
     * @throws H                    in case of the {@link Resolver} resolver error
     * @throws NullPointerException if the specified {@link Resolver} resolver is {@code null}
     *                              or {@link Association} contains {@code null} keys or values
     */
    default <R, H extends Exception> R traverse(Resolver<? super V, E, ? extends R, ? extends H> resolver, Association<?, ?, ?> ctx) throws E, H {
        return resolver.resolve(TRAVERSE, this, ctx);
    }

    /**
     * Try to traverse the Source either sequentially or in parallel depending on the Source mode with the given
     * {@link Traverse} segment traverse function and {@link KeyValue key-value} varargs context.
     * <p>
     * The specified {@link KeyValue key-values} must not contain {@code null} keys or values to avoid {@link NullPointerException}.
     *
     * @param traverse the given {@link Traverse} segment traverse function
     * @param ctx      the given {@link KeyValue} varargs context
     * @param <H>      the possible exception type throwing by the traverse function
     * @return the completion flag indicating either the Source was completely traversed, or not
     * @throws E                    in case of the traversal error
     * @throws H                    in the case of traverse function error
     * @throws NullPointerException if the specified traverse function is {@code null} or {@link KeyValue key-values}
     *                              have {@code null} keys or values
     */
    default <H extends Exception> boolean traverse(Traverse<V, E, ? extends E, ? extends H> traverse, KeyValue<?, ?>... ctx) throws E, H {
        return traverse(TRAVERSE, traverse, ctx);
    }

    /**
     * Try to traverse the Source either sequentially or in parallel depending on the Source mode with the given
     * {@link Traverse} segment traverse function and {@link Association} context.
     * <p>
     * The specified {@link Association} must contain non-null keys and values, and its {@link Association#get(Object)}
     * method should raise a {@link NoSuchElementException} instead of returning {@code null} when no value is associated
     * with a key.
     *
     * @param traverse the given {@link Traverse} segment traverse function
     * @param ctx      the given {@link Association} context
     * @param <H>      the possible exception type throwing by the traverse function
     * @return the completion flag indicating either the Source was completely traversed, or not
     * @throws E                    in case of the traversal error
     * @throws H                    in the case of traverse function error
     * @throws NullPointerException if the specified traverse function is {@code null} or {@link Association}
     *                              contains {@code null} keys or values
     */
    default <H extends Exception> boolean traverse(Traverse<V, E, ? extends E, ? extends H> traverse, Association<?, ?, ?> ctx) throws E, H {
        return traverse(TRAVERSE, traverse, ctx);
    }


    /**
     * Try to traverse the Source completely explicitly in parallel (if the Source does support parallelism or sequentially,
     * otherwise) with the given {@link KeyValue key-value} varargs context.
     * <p>
     * The specified {@link KeyValue key-values} must not contain {@code null} keys or values to avoid {@link NullPointerException}.
     *
     * @param ctx the given {@link KeyValue} varargs context
     * @throws E                    in case of the traversal error
     * @throws NullPointerException if the specified {@link KeyValue key-values} have {@code null} keys or values
     */
    default void parallel(KeyValue<?, ?>... ctx) throws E {
        parallel(each(idle()), ctx);
    }

    /**
     * Try to traverse the Source completely explicitly in parallel (if the Source does support parallelism or sequentially,
     * otherwise) with the given {@link Association} context.
     * <p>
     * The specified {@link Association} must contain non-null keys and values, and its {@link Association#get(Object)}
     * method should raise a {@link NoSuchElementException} instead of returning {@code null} when no value is associated
     * with a key.
     *
     * @param ctx the given {@link Association} context
     * @throws E                    in case of the traversal error
     * @throws NullPointerException if the specified {@link Association} contains {@code null} keys or values
     */
    default void parallel(Association<?, ?, ?> ctx) throws E {
        parallel(each(idle()), ctx);
    }

    /**
     * Try to traverse the Source completely explicitly in parallel (if the Source does support parallelism or sequentially,
     * otherwise) returning the collection result by the given {@link Collector} and {@link KeyValue key-value} varargs context.
     * <p>
     * The specified {@link KeyValue key-values} must not contain {@code null} keys or values to avoid {@link NullPointerException}.
     *
     * @param collector the given {@link Collector} collector
     * @param ctx       the given {@link KeyValue} varargs context
     * @param <R>       the type of the collecting result
     * @return the collection result
     * @throws E                    in case of the traversal error
     * @throws NullPointerException if the specified {@link Collector} collector is {@code null} or {@link KeyValue key-values}
     *                              have {@code null} keys or values
     */
    default <R> R parallel(Collector<? super V, ?, ? extends R> collector, KeyValue<?, ?>... ctx) throws E {
        return this.<R, RuntimeException>parallel(reduce(collector), ctx);
    }

    /**
     * Try to traverse the Source completely explicitly in parallel (if the Source does support parallelism or sequentially,
     * otherwise) returning the collection result by the given {@link Collector} and {@link Association} context.
     * <p>
     * The specified {@link Association} must contain non-null keys and values, and its {@link Association#get(Object)}
     * method should raise a {@link NoSuchElementException} instead of returning {@code null} when no value is associated
     * with a key.
     *
     * @param collector the given {@link Collector} collector
     * @param ctx       the given {@link Association} context
     * @param <R>       the type of the collecting result
     * @return the collection result
     * @throws E                    in case of the traversal error
     * @throws NullPointerException if the specified {@link Collector} collector is {@code null}
     *                              or {@link Association} contains {@code null} keys or values
     */
    default <R> R parallel(Collector<? super V, ?, ? extends R> collector, Association<?, ?, ?> ctx) throws E {
        return this.<R, RuntimeException>parallel(reduce(collector), ctx);
    }

    /**
     * Try to traverse the Source completely explicitly in parallel (if the Source does support parallelism or sequentially,
     * otherwise) returning the result by the given {@link Resolver} and {@link KeyValue key-value} varargs context.
     * <p>
     * The specified {@link KeyValue key-values} must not contain {@code null} keys or values to avoid {@link NullPointerException}.
     *
     * @param resolver the given {@link Resolver} resolver
     * @param ctx      the given {@link KeyValue} varargs context
     * @param <R>      the type of the resolution result
     * @param <H>      the type of the resolution error
     * @return the resolution result
     * @throws E                    in case of the traversal error
     * @throws H                    in case of the {@link Resolver} resolver error
     * @throws NullPointerException if the specified {@link Resolver} resolver is {@code null}
     *                              or {@link Association} contains {@code null} keys or values
     */
    default <R, H extends Exception> R parallel(Resolver<? super V, E, ? extends R, ? extends H> resolver, KeyValue<?, ?>... ctx) throws E, H {
        return parallel(resolver, context(ctx));
    }

    /**
     * Try to traverse the Source completely explicitly in parallel (if the Source does support parallelism or sequentially,
     * otherwise) returning the result by the given {@link Resolver} and {@link Association} context.
     * <p>
     * The specified {@link Association} must contain non-null keys and values, and its {@link Association#get(Object)}
     * method should raise a {@link NoSuchElementException} instead of returning {@code null} when no value is associated
     * with a key.
     *
     * @param resolver the given {@link Resolver} resolver
     * @param ctx      the given {@link Association} context
     * @param <R>      the type of the resolution result
     * @param <H>      the type of the resolution error
     * @return the resolution result
     * @throws E                    in case of the traversal error
     * @throws H                    in case of the {@link Resolver} resolver error
     * @throws NullPointerException if the specified {@link Resolver} resolver is {@code null}
     *                              or {@link Association} contains {@code null} keys or values
     */
    default <R, H extends Exception> R parallel(Resolver<? super V, E, ? extends R, ? extends H> resolver, Association<?, ?, ?> ctx) throws E, H {
        return resolver.resolve(PARALLEL, this, ctx);
    }

    /**
     * Try to traverse the Source explicitly in parallel (if the Source does support parallelism or sequentially,
     * otherwise) with the given {@link Traverse} segment traverse function and {@link KeyValue key-value} varargs
     * context.
     * <p>
     * The specified {@link KeyValue key-values} must not contain {@code null} keys or values to avoid {@link NullPointerException}.
     *
     * @param traverse the given {@link Traverse} segment traverse function
     * @param ctx      the given {@link KeyValue} varargs context
     * @param <H>      the possible exception type throwing by the traverse function
     * @return the completion flag indicating either the Source was completely traversed, or not
     * @throws E                    in case of the traversal error
     * @throws H                    in the case of traverse function error
     * @throws NullPointerException if the specified traverse function is {@code null} or {@link KeyValue key-values}
     *                              have {@code null} keys or values
     */
    default <H extends Exception> boolean parallel(Traverse<V, E, ? extends E, ? extends H> traverse, KeyValue<?, ?>... ctx) throws E, H {
        return traverse(PARALLEL, traverse, ctx);
    }

    /**
     * Try to traverse the Source explicitly in parallel (if the Source does support parallelism or sequentially,
     * otherwise) with the given {@link Traverse} segment traverse function and {@link Association} context.
     * <p>
     * The specified {@link Association} must contain non-null keys and values, and its {@link Association#get(Object)}
     * method should raise a {@link NoSuchElementException} instead of returning {@code null} when no value is associated
     * with a key.
     *
     * @param traverse the given {@link Traverse} segment traverse function
     * @param ctx      the given {@link Association} context
     * @param <H>      the possible exception type throwing by the traverse function
     * @return the completion flag indicating either the Source was completely traversed, or not
     * @throws E                    in case of the traversal error
     * @throws H                    in the case of traverse function error
     * @throws NullPointerException if the specified traverse function is {@code null} or {@link Association}
     *                              contains {@code null} keys or values
     */
    default <H extends Exception> boolean parallel(Traverse<V, E, ? extends E, ? extends H> traverse, Association<?, ?, ?> ctx) throws E, H {
        return traverse(PARALLEL, traverse, ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @param traverse {@inheritDoc}
     * @param ctx      {@inheritDoc}
     * @param <H1>     {@inheritDoc}
     * @param <H2>     {@inheritDoc}
     * @return {@inheritDoc}
     * @throws E                    {@inheritDoc}
     * @throws H1                   {@inheritDoc}
     * @throws H2                   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    default <H1 extends Exception, H2 extends Exception> boolean sequence(Executable1Throwing2<? super TraverseSupport.Traverser<? extends V, ? extends E>, ? extends H1, ? extends H2> traverse, KeyValue<?, ?>... ctx) throws E, H1, H2 {
        return this.<H1, H2>traverse(SEQUENCE, traverse::exec, ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @param traverse {@inheritDoc}
     * @param ctx      {@inheritDoc}
     * @param <H1>     {@inheritDoc}
     * @param <H2>     {@inheritDoc}
     * @return {@inheritDoc}
     * @throws E                    {@inheritDoc}
     * @throws H1                   {@inheritDoc}
     * @throws H2                   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    default <H1 extends Exception, H2 extends Exception> boolean sequence(Executable1Throwing2<? super TraverseSupport.Traverser<? extends V, ? extends E>, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {
        return this.<H1, H2>traverse(SEQUENCE, traverse::exec, ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @param traverse {@inheritDoc}
     * @param ctx      {@inheritDoc}
     * @param <H1>     {@inheritDoc}
     * @param <H2>     {@inheritDoc}
     * @return {@inheritDoc}
     * @throws E                    {@inheritDoc}
     * @throws H1                   {@inheritDoc}
     * @throws H2                   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    default <H1 extends Exception, H2 extends Exception> boolean traverse(Executable1Throwing2<? super TraverseSupport.Traverser<? extends V, ? extends E>, ? extends H1, ? extends H2> traverse, KeyValue<?, ?>... ctx) throws E, H1, H2 {
        return this.<H1, H2>traverse(TRAVERSE, traverse::exec, ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @param traverse {@inheritDoc}
     * @param ctx      {@inheritDoc}
     * @param <H1>     {@inheritDoc}
     * @param <H2>     {@inheritDoc}
     * @return {@inheritDoc}
     * @throws E                    {@inheritDoc}
     * @throws H1                   {@inheritDoc}
     * @throws H2                   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    default <H1 extends Exception, H2 extends Exception> boolean traverse(Executable1Throwing2<? super TraverseSupport.Traverser<? extends V, ? extends E>, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {
        return this.<H1, H2>traverse(TRAVERSE, traverse::exec, ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @param traverse {@inheritDoc}
     * @param ctx      {@inheritDoc}
     * @param <H1>     {@inheritDoc}
     * @param <H2>     {@inheritDoc}
     * @return {@inheritDoc}
     * @throws E                    {@inheritDoc}
     * @throws H1                   {@inheritDoc}
     * @throws H2                   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    default <H1 extends Exception, H2 extends Exception> boolean parallel(Executable1Throwing2<? super TraverseSupport.Traverser<? extends V, ? extends E>, ? extends H1, ? extends H2> traverse, KeyValue<?, ?>... ctx) throws E, H1, H2 {
        return this.<H1, H2>traverse(PARALLEL, traverse::exec, ctx);
    }

    /**
     * {@inheritDoc}
     *
     * @param traverse {@inheritDoc}
     * @param ctx      {@inheritDoc}
     * @param <H1>     {@inheritDoc}
     * @param <H2>     {@inheritDoc}
     * @return {@inheritDoc}
     * @throws E                    {@inheritDoc}
     * @throws H1                   {@inheritDoc}
     * @throws H2                   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    default <H1 extends Exception, H2 extends Exception> boolean parallel(Executable1Throwing2<? super TraverseSupport.Traverser<? extends V, ? extends E>, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {
        return this.<H1, H2>traverse(PARALLEL, traverse::exec, ctx);
    }


    /**
     * Try to traverse the Source using the specified {@link TraverseMethod traverse method} with the given
     * {@link Traverse} segment traverse function and {@link KeyValue key-value} varargs context.
     * <p>
     * The specified {@link KeyValue key-values} must not contain {@code null} keys or values to avoid {@link NullPointerException}.
     *
     * @param method   the specified {@link TraverseMethod} traverse method
     * @param traverse the given {@link Traverse} segment traverse function
     * @param ctx      the given {@link KeyValue} varargs context
     * @param <H1>     the possible exception 1 type throwing by the traverse function
     * @param <H2>     the possible exception 2 type throwing by the traverse function
     * @return the completion flag indicating either the Source was completely traversed, or not
     * @throws E                    in case of the traversal error
     * @throws H1                   in the case of traverse function error 1
     * @throws H2                   in the case of traverse function error 2
     * @throws NullPointerException if the specified traverse method or traverse function is {@code null}
     *                              or {@link KeyValue key-values} have {@code null} keys or values
     */
    default <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, KeyValue<?, ?>... ctx) throws E, H1, H2 {
        return traverse(method, traverse, context(ctx));
    }

    /**
     * Try to traverse the Source using the specified {@link TraverseMethod traverse method} with the given
     * {@link Traverse} segment traverse function and {@link Association} context.
     * <p>
     * The specified {@link Association} must contain non-null keys and values, and its {@link Association#get(Object)}
     * method should raise a {@link NoSuchElementException} instead of returning {@code null} when no value is associated
     * with a key.
     *
     * @param method   the specified {@link TraverseMethod} traverse method
     * @param traverse the given {@link Traverse} segment traverse function
     * @param ctx      the given {@link KeyValue} varargs context
     * @param <H1>     the possible exception 1 type throwing by the traverse function
     * @param <H2>     the possible exception 2 type throwing by the traverse function
     * @return the completion flag indicating either the Source was completely traversed, or not
     * @throws E                    in case of the traversal error
     * @throws H1                   in the case of traverse function error 1
     * @throws H2                   in the case of traverse function error 2
     * @throws NullPointerException if the specified traverse method or traverse function is {@code null}
     *                              or {@link Association} contains {@code null} keys or values
     */
    <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2;


    /**
     * {@inheritDoc}
     *
     * @param traverse {@inheritDoc}
     * @param ctx      {@inheritDoc}
     * @param <H1>     {@inheritDoc}
     * @param <H2>     {@inheritDoc}
     * @param <H3>     {@inheritDoc}
     * @return {@inheritDoc}
     * @throws E                    {@inheritDoc}
     * @throws H1                   {@inheritDoc}
     * @throws H2                   {@inheritDoc}
     * @throws H3                   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    default <H1 extends Exception, H2 extends Exception, H3 extends Exception> boolean sequence(Executable1Throwing3<? super TraverseSupport.Traverser<? extends V, ? extends E>, ? extends H1, ? extends H2, ? extends H3> traverse, KeyValue<?, ?>... ctx) throws E, H1, H2, H3 {
        try { return traverse(SEQUENCE, traverse::exec, ctx); } catch (Exception ex) { return sneak(ex); } }

    /**
     * {@inheritDoc}
     *
     * @param traverse {@inheritDoc}
     * @param ctx      {@inheritDoc}
     * @param <H1>     {@inheritDoc}
     * @param <H2>     {@inheritDoc}
     * @param <H3>     {@inheritDoc}
     * @return {@inheritDoc}
     * @throws E                    {@inheritDoc}
     * @throws H1                   {@inheritDoc}
     * @throws H2                   {@inheritDoc}
     * @throws H3                   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    default <H1 extends Exception, H2 extends Exception, H3 extends Exception> boolean sequence(Executable1Throwing3<? super TraverseSupport.Traverser<? extends V, ? extends E>, ? extends H1, ? extends H2, ? extends H3> traverse, Association<?, ?, ?> ctx) throws E, H1, H2, H3 {
        try { return traverse(SEQUENCE, traverse::exec, ctx); } catch (Exception ex) { return sneak(ex); } }

    /**
     * {@inheritDoc}
     *
     * @param traverse {@inheritDoc}
     * @param ctx      {@inheritDoc}
     * @param <H1>     {@inheritDoc}
     * @param <H2>     {@inheritDoc}
     * @param <H3>     {@inheritDoc}
     * @return {@inheritDoc}
     * @throws E                    {@inheritDoc}
     * @throws H1                   {@inheritDoc}
     * @throws H2                   {@inheritDoc}
     * @throws H3                   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    default <H1 extends Exception, H2 extends Exception, H3 extends Exception> boolean traverse(Executable1Throwing3<? super TraverseSupport.Traverser<? extends V, ? extends E>, ? extends H1, ? extends H2, ? extends H3> traverse, KeyValue<?, ?>... ctx) throws E, H1, H2, H3 {
        try { return traverse(TRAVERSE, traverse::exec, ctx); } catch (Exception ex) { return sneak(ex); } }

    /**
     * {@inheritDoc}
     *
     * @param traverse {@inheritDoc}
     * @param ctx      {@inheritDoc}
     * @param <H1>     {@inheritDoc}
     * @param <H2>     {@inheritDoc}
     * @param <H3>     {@inheritDoc}
     * @return {@inheritDoc}
     * @throws E                    {@inheritDoc}
     * @throws H1                   {@inheritDoc}
     * @throws H2                   {@inheritDoc}
     * @throws H3                   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    default <H1 extends Exception, H2 extends Exception, H3 extends Exception> boolean traverse(Executable1Throwing3<? super TraverseSupport.Traverser<? extends V, ? extends E>, ? extends H1, ? extends H2, ? extends H3> traverse, Association<?, ?, ?> ctx) throws E, H1, H2, H3 {
        try { return traverse(TRAVERSE, traverse::exec, ctx); } catch (Exception ex) { return sneak(ex); } }

    /**
     * {@inheritDoc}
     *
     * @param traverse {@inheritDoc}
     * @param ctx      {@inheritDoc}
     * @param <H1>     {@inheritDoc}
     * @param <H2>     {@inheritDoc}
     * @param <H3>     {@inheritDoc}
     * @return {@inheritDoc}
     * @throws E                    {@inheritDoc}
     * @throws H1                   {@inheritDoc}
     * @throws H2                   {@inheritDoc}
     * @throws H3                   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    default <H1 extends Exception, H2 extends Exception, H3 extends Exception> boolean parallel(Executable1Throwing3<? super TraverseSupport.Traverser<? extends V, ? extends E>, ? extends H1, ? extends H2, ? extends H3> traverse, KeyValue<?, ?>... ctx) throws E, H1, H2, H3 {
        try { return traverse(PARALLEL, traverse::exec, ctx); } catch (Exception ex) { return sneak(ex); } }

    /**
     * {@inheritDoc}
     *
     * @param traverse {@inheritDoc}
     * @param ctx      {@inheritDoc}
     * @param <H1>     {@inheritDoc}
     * @param <H2>     {@inheritDoc}
     * @param <H3>     {@inheritDoc}
     * @return {@inheritDoc}
     * @throws E                    {@inheritDoc}
     * @throws H1                   {@inheritDoc}
     * @throws H2                   {@inheritDoc}
     * @throws H3                   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    default <H1 extends Exception, H2 extends Exception, H3 extends Exception> boolean parallel(Executable1Throwing3<? super TraverseSupport.Traverser<? extends V, ? extends E>, ? extends H1, ? extends H2, ? extends H3> traverse, Association<?, ?, ?> ctx) throws E, H1, H2, H3 {
        try { return traverse(PARALLEL, traverse::exec, ctx); } catch (Exception ex) { return sneak(ex); } }


    /**
     * Extension of the {@link Executable1Throwing2} segment traverse function, offering the
     * {@link #complete(TraverseSupport.Traverser)} method for signaling the completion
     * result, indicating whether the segment was fully traversed or not.
     *
     * @param <V>  the type of item values
     * @param <E>  the type of traversal exception
     * @param <H1> the possible exception 1 type throwing by the traverse function
     * @param <H2> the possible exception 2 type throwing by the traverse function
     */
    @FunctionalInterface
    interface Traverse<V, E extends Exception, H1 extends Exception, H2 extends Exception>
            extends Executable1Throwing2<TraverseSupport.Traverser<? extends V, ? extends E>, H1, H2> {

        /**
         * Traverse the Source segment represented by the given {@link TraverseSupport.Traverser} traverser.
         *
         * @param traverser the given {@link TraverseSupport.Traverser} traverser
         * @return the traversal completion result for the segment represented by this Traverser
         * @throws H1 in the case of traverse function error 1
         * @throws H2 in the case of traverse function error 2
         */
        default boolean complete(TraverseSupport.Traverser<? extends V, ? extends E> traverser) throws H1, H2 {
            Variable.Boolean complete = var(false); exec(new Traverser<>() {
                public <X extends Exception> boolean next(Executable1Throwing1<? super V, ? extends X> consumer) throws E, X {
                    return !(complete.flg = !traverser.next(consumer)); }
                public <X extends Exception> boolean next(long number, Executable1Throwing1<? super V, ? extends X> consumer) throws E, X {
                    return !(complete.flg = !traverser.next(number, consumer)); }
                public <X extends Exception> boolean some(Testable1Throwing1<? super V, ? extends X> consumer) throws E, X {
                    return !(complete.flg = !traverser.some(consumer)); }
                public <X extends Exception> void each(Executable1Throwing1<? super V, ? extends X> consumer) throws E, X {
                    traverser.each(consumer); complete.flg = true; }
            }); return complete.flg;
        }
    }


    /**
     * An implementation of the {@link TraverseSupport.Traverser} interface that defines
     * all methods using the individual traversal method {@link #next(Executable1Throwing1)}.
     *
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     */
    interface Traverser<V, E extends Exception> extends TraverseSupport.Traverser<V, E> {

        /**
         * {@inheritDoc}
         *
         * @param number   {@inheritDoc}
         * @param consumer {@inheritDoc}
         * @param <H>      {@inheritDoc}
         * @return {@inheritDoc}
         * @throws E                    {@inheritDoc}
         * @throws H                    {@inheritDoc}
         * @throws NullPointerException {@inheritDoc}
         */
        @Override
        default <H extends Exception> boolean next(long number, Executable1Throwing1<? super V, ? extends H> consumer) throws E, H {
            if (number < 0L)
                throw new IllegalArgumentException(format("Negative demand: %d.", number));
            requireNonNull(consumer);
            if (number == 0L)
                return true;
            Variable.Long count = var(number);
            return some(value -> {
                consumer.exec(value); return count.decr() > 0L;
            });
        }

        /**
         * {@inheritDoc}
         *
         * @param consumer {@inheritDoc}
         * @param <H>      {@inheritDoc}
         * @return {@inheritDoc}
         * @throws E                    {@inheritDoc}
         * @throws H                    {@inheritDoc}
         * @throws NullPointerException {@inheritDoc}
         */
        @Override
        default <H extends Exception> boolean some(Testable1Throwing1<? super V, ? extends H> consumer) throws E, H {
            requireNonNull(consumer);
            Variable.Boolean next = var(true);
            while (next.flg)
                if (!next(value -> next.flg = consumer.test(value)))
                    return false;
            return true;
        }

        /**
         * {@inheritDoc}
         *
         * @param consumer {@inheritDoc}
         * @param <H>      {@inheritDoc}
         * @throws E                    {@inheritDoc}
         * @throws H                    {@inheritDoc}
         * @throws NullPointerException {@inheritDoc}
         */
        @Override
        default <H extends Exception> void each(Executable1Throwing1<? super V, ? extends H> consumer) throws E, H {
            requireNonNull(consumer);
            some(value -> {
                consumer.exec(value); return true;
            });
        }

        /**
         * {@inheritDoc}
         *
         * @param consumer {@inheritDoc}
         * @param <H1>     {@inheritDoc}
         * @param <H2>     {@inheritDoc}
         * @return {@inheritDoc}
         * @throws E                    {@inheritDoc}
         * @throws H1                   {@inheritDoc}
         * @throws H2                   {@inheritDoc}
         * @throws NullPointerException {@inheritDoc}
         */
        @Override
        default <H1 extends Exception, H2 extends Exception> boolean next(Executable1Throwing2<? super V, ? extends H1, ? extends H2> consumer) throws E, H1, H2 {
            try { return next(consumer::exec); } catch (Exception ex) { return sneak(ex); } }

        /**
         * {@inheritDoc}
         *
         * @param number   {@inheritDoc}
         * @param consumer {@inheritDoc}
         * @param <H1>     {@inheritDoc}
         * @param <H2>     {@inheritDoc}
         * @return {@inheritDoc}
         * @throws E                    {@inheritDoc}
         * @throws H1                   {@inheritDoc}
         * @throws H2                   {@inheritDoc}
         * @throws NullPointerException {@inheritDoc}
         */
        @Override
        default <H1 extends Exception, H2 extends Exception> boolean next(long number, Executable1Throwing2<? super V, ? extends H1, ? extends H2> consumer) throws E, H1, H2 {
            try { return next(number, consumer::exec); } catch (Exception ex) { return sneak(ex); } }

        /**
         * {@inheritDoc}
         *
         * @param consumer {@inheritDoc}
         * @param <H1>     {@inheritDoc}
         * @param <H2>     {@inheritDoc}
         * @return {@inheritDoc}
         * @throws E                    {@inheritDoc}
         * @throws H1                   {@inheritDoc}
         * @throws H2                   {@inheritDoc}
         * @throws NullPointerException {@inheritDoc}
         */
        @Override
        default <H1 extends Exception, H2 extends Exception> boolean some(Testable1Throwing2<? super V, ? extends H1, ? extends H2> consumer) throws E, H1, H2 {
            try { return some(consumer::test); } catch (Exception ex) { return sneak(ex); } }

        /**
         * {@inheritDoc}
         *
         * @param consumer {@inheritDoc}
         * @param <H1>     {@inheritDoc}
         * @param <H2>     {@inheritDoc}
         * @throws E                    {@inheritDoc}
         * @throws H1                   {@inheritDoc}
         * @throws H2                   {@inheritDoc}
         * @throws NullPointerException {@inheritDoc}
         */
        @Override
        default <H1 extends Exception, H2 extends Exception> void each(Executable1Throwing2<? super V, ? extends H1, ? extends H2> consumer) throws E, H1, H2 {
            try { each(consumer::exec); } catch (Exception ex) { sneak(ex); } }


        /**
         * {@inheritDoc}
         *
         * @param consumer {@inheritDoc}
         * @param <H1>     {@inheritDoc}
         * @param <H2>     {@inheritDoc}
         * @param <H3>     {@inheritDoc}
         * @return {@inheritDoc}
         * @throws E                    {@inheritDoc}
         * @throws H1                   {@inheritDoc}
         * @throws H2                   {@inheritDoc}
         * @throws H3                   {@inheritDoc}
         * @throws NullPointerException {@inheritDoc}
         */
        @Override
        default <H1 extends Exception, H2 extends Exception, H3 extends Exception> boolean next(Executable1Throwing3<? super V, ? extends H1, ? extends H2, ? extends H3> consumer) throws E, H1, H2, H3 {
            try { return next(consumer::exec); } catch (Exception ex) { return sneak(ex); } }

        /**
         * {@inheritDoc}
         *
         * @param number   {@inheritDoc}
         * @param consumer {@inheritDoc}
         * @param <H1>     {@inheritDoc}
         * @param <H2>     {@inheritDoc}
         * @param <H3>     {@inheritDoc}
         * @return {@inheritDoc}
         * @throws E                    {@inheritDoc}
         * @throws H1                   {@inheritDoc}
         * @throws H2                   {@inheritDoc}
         * @throws H3                   {@inheritDoc}
         * @throws NullPointerException {@inheritDoc}
         */
        @Override
        default <H1 extends Exception, H2 extends Exception, H3 extends Exception> boolean next(long number, Executable1Throwing3<? super V, ? extends H1, ? extends H2, ? extends H3> consumer) throws E, H1, H2, H3 {
            try { return next(number, consumer::exec); } catch (Exception ex) { return sneak(ex); } }

        /**
         * {@inheritDoc}
         *
         * @param consumer {@inheritDoc}
         * @param <H1>     {@inheritDoc}
         * @param <H2>     {@inheritDoc}
         * @param <H3>     {@inheritDoc}
         * @return {@inheritDoc}
         * @throws E                    {@inheritDoc}
         * @throws H1                   {@inheritDoc}
         * @throws H2                   {@inheritDoc}
         * @throws H3                   {@inheritDoc}
         * @throws NullPointerException {@inheritDoc}
         */
        @Override
        default <H1 extends Exception, H2 extends Exception, H3 extends Exception> boolean some(Testable1Throwing3<? super V, ? extends H1, ? extends H2, ? extends H3> consumer) throws E, H1, H2, H3 {
            try { return some(consumer::test); } catch (Exception ex) { return sneak(ex); } }

        /**
         * {@inheritDoc}
         *
         * @param consumer {@inheritDoc}
         * @param <H1>     {@inheritDoc}
         * @param <H2>     {@inheritDoc}
         * @param <H3>     {@inheritDoc}
         * @throws E                    {@inheritDoc}
         * @throws H1                   {@inheritDoc}
         * @throws H2                   {@inheritDoc}
         * @throws H3                   {@inheritDoc}
         * @throws NullPointerException {@inheritDoc}
         */
        @Override
        default <H1 extends Exception, H2 extends Exception, H3 extends Exception> void each(Executable1Throwing3<? super V, ? extends H1, ? extends H2, ? extends H3> consumer) throws E, H1, H2, H3 {
            try { each(consumer::exec); } catch (Exception ex) { sneak(ex); } }


        /**
         * Traverse this Traverser with the specified {@link Traverse} segment traverse function
         * and return the traversal completion result for the segment represented by this Traverser.
         *
         * @param traverse the specified {@link Traverse} segment traverse function
         * @param <H1>     the possible exception 1 type throwing by the traverse function
         * @param <H2>     the possible exception 2 type throwing by the traverse function
         * @return the traversal completion result for the segment represented by this Traverser
         * @throws H1 in the case of traverse function error 1
         * @throws H2 in the case of traverse function error 2
         */
        default <H1 extends Exception, H2 extends Exception> boolean complete(Traverse<? super V, ? super E, ? extends H1, ? extends H2> traverse) throws H1, H2 {
            return traverse.complete(this);
        }
    }


    /**
     * Represents a resolution operation that yields a result from traversing a {@link TraverseSupport} source.
     * <p>
     * This may include short-circuiting operations such as element search and reduction operations,
     * like accumulations using a {@link Collector} or reduction functions.
     *
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @param <R> the type of the resolution result
     * @param <H> the type of resolution exception
     */
    @FunctionalInterface
    interface Resolver<V, E extends Exception, R, H extends Exception> {

        /**
         * Resolve the provided {@link TraverseSupport} source with the given {@link Association} context
         * using the specified {@link TraverseMethod} traverse method and return the resolution result.
         *
         * @param method      the specified {@link TraverseMethod} traverse method
         * @param traversable the {@link TraverseSupport} source to be resolved
         * @param ctx         the given {@link Association} context
         * @return the resolution result
         * @throws E in case of the traversal error
         * @throws H in case of the resolution error
         */
        R resolve(TraverseMethod method, TraverseSupport<? extends V, ? extends E> traversable, Association<?, ?, ?> ctx) throws E, H;
    }


    /**
     * Return an idle {@link Traverser}, returning no elements.
     *
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return an idle {@link Traverser}, returning no elements
     */
    static <V, E extends Exception> Traverser<V, E> traverser() {
        return new Traverser<>() {
            public <H extends Exception> boolean next(Executable1Throwing1<? super V, ? extends H> consumer) throws E, H {
                requireNonNull(consumer); return false; }
        };
    }


    /**
     * Return a single element traverse function by the given {@link Executable1Throwing1} consumer.
     *
     * @param consumer the given {@link Executable1Throwing1} consumer
     * @param <V>      the type of item values
     * @param <E>      the type of possible traverse exception
     * @param <H>      the type of possible consumption exception
     * @return a single element traverse function by the given {@link Executable1Throwing1} consumer
     * @throws NullPointerException if the given {@link Executable1Throwing1} consumer is {@code null}
     */
    static <V, E extends Exception, H extends Exception> Executable1Throwing2<TraverseSupport.Traverser<? extends V, ? extends E>, ? extends E, ? extends H> next(Executable1Throwing1<? super V, ? extends H> consumer) {
        requireNonNull(consumer); return traverser -> traverser.next(consumer);
    }

    /**
     * Return elements' portion traverse function by the given {@link Testable1Throwing1} consumer.
     *
     * @param consumer the given {@link Testable1Throwing1} consumer
     * @param <V>      the type of item values
     * @param <E>      the type of possible traverse exception
     * @param <H>      the type of possible consumption exception
     * @return elements' portion traverse function by the given {@link Testable1Throwing1} consumer
     * @throws NullPointerException if the given {@link Testable1Throwing1} consumer is {@code null}
     */
    static <V, E extends Exception, H extends Exception> Executable1Throwing2<TraverseSupport.Traverser<? extends V, ? extends E>, ? extends E, ? extends H> some(Testable1Throwing1<? super V, ? extends H> consumer) {
        requireNonNull(consumer); return traverser -> traverser.some(consumer);
    }

    /**
     * Return each element traverse function by the given {@link Executable1Throwing1} consumer.
     *
     * @param consumer the given {@link Executable1Throwing1} consumer
     * @param <V>      the type of item values
     * @param <E>      the type of possible traverse exception
     * @param <H>      the type of possible consumption exception
     * @return each element traverse function by the given {@link Executable1Throwing1} consumer
     * @throws NullPointerException if the given {@link Executable1Throwing1} consumer is {@code null}
     */
    static <V, E extends Exception, H extends Exception> Executable1Throwing2<TraverseSupport.Traverser<? extends V, ? extends E>, ? extends E, ? extends H> each(Executable1Throwing1<? super V, ? extends H> consumer) {
        requireNonNull(consumer); return traverser -> traverser.each(consumer);
    }


    /**
     * Return a single element traverse function by the given {@link Executable1Throwing2} consumer.
     *
     * @param consumer the given {@link Executable1Throwing2} consumer
     * @param <V>      the type of item values
     * @param <E>      the type of possible traverse exception
     * @param <H1>     the type of possible consumption exception 1
     * @param <H2>     the type of possible consumption exception 2
     * @return a single element traverse function by the given {@link Executable1Throwing2} consumer
     * @throws NullPointerException if the given {@link Executable1Throwing2} consumer is {@code null}
     */
    static <V, E extends Exception, H1 extends Exception, H2 extends Exception> Executable1Throwing3<TraverseSupport.Traverser<? extends V, ? extends E>, ? extends E, ? extends H1, ? extends H2> next(Executable1Throwing2<? super V, ? extends H1, ? extends H2> consumer) {
        requireNonNull(consumer); return traverser -> traverser.next(consumer);
    }

    /**
     * Return elements' portion traverse function by the given {@link Testable1Throwing2} consumer.
     *
     * @param consumer the given {@link Testable1Throwing2} consumer
     * @param <V>      the type of item values
     * @param <E>      the type of possible traverse exception
     * @param <H1>     the type of possible consumption exception 1
     * @param <H2>     the type of possible consumption exception 2
     * @return elements' portion traverse function by the given {@link Testable1Throwing2} consumer
     * @throws NullPointerException if the given {@link Testable1Throwing2} consumer is {@code null}
     */
    static <V, E extends Exception, H1 extends Exception, H2 extends Exception> Executable1Throwing3<TraverseSupport.Traverser<? extends V, ? extends E>, ? extends E, ? extends H1, ? extends H2> some(Testable1Throwing2<? super V, ? extends H1, ? extends H2> consumer) {
        requireNonNull(consumer); return traverser -> traverser.some(consumer);
    }

    /**
     * Return each element traverse function by the given {@link Executable1Throwing2} consumer.
     *
     * @param consumer the given {@link Executable1Throwing2} consumer
     * @param <V>      the type of item values
     * @param <E>      the type of possible traverse exception
     * @param <H1>     the type of possible consumption exception 1
     * @param <H2>     the type of possible consumption exception 2
     * @return each element traverse function by the given {@link Executable1Throwing2} consumer
     * @throws NullPointerException if the given {@link Executable1Throwing2} consumer is {@code null}
     */
    static <V, E extends Exception, H1 extends Exception, H2 extends Exception> Executable1Throwing3<TraverseSupport.Traverser<? extends V, ? extends E>, ? extends E, ? extends H1, ? extends H2> each(Executable1Throwing2<? super V, ? extends H1, ? extends H2> consumer) {
        requireNonNull(consumer); return traverser -> traverser.each(consumer);
    }

    /**
     * Return a {@link Resolver} that identifies the first element matching the provided {@link Testable1Throwing1} predicate
     * and stores it in a {@link Container}.
     * <p>
     * This is a short-circuiting resolution operation.
     *
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @param <H> the type of resolution exception
     * @return a {@link Resolver} that finds the first matching element and places it in a {@link Container}
     * @throws NullPointerException if the provided {@link Testable1Throwing1} predicate is {@code null}
     */
    static <V, E extends Exception, H extends Exception> Resolver<V, E, Container<V>, H> any(Testable1Throwing1<? super V, ? extends H> predicate) {
        requireNonNull(predicate); return (method, traversable, ctx) -> {
            Valuable<Container<V>> any = method.isSequence() ? var() : vol();
            method.traverse(traversable, some(value -> {
                if (!predicate.test(value)) return true;
                any.let(null, container(value)); return false;
            }), ctx);
            return requireNonNullElseGet(any.get(), HashContainer::container);
        };
    }

    /**
     * Return a {@link Resolver} returning {@link Boolean} indicating whether any elements of
     * a {@link TraverseSupport} source match the provided {@link Testable1Throwing1} predicate.
     * <p>
     * This is a short-circuiting resolution operation.
     *
     * @param predicate the given {@link Testable1Throwing1} predicate
     * @param <V>       the type of item values
     * @param <E>       the type of traversal exception
     * @param <H>       the type of the predicate error
     * @return a {@link Resolver} returning {@link Boolean} indicating whether any elements of
     * a {@link TraverseSupport} source match the provided {@link Testable1Throwing1} predicate
     * @throws NullPointerException if the given {@link Testable1Throwing1} predicate is {@code null}
     */
    static <V, E extends Exception, H extends Exception> Resolver<V, E, Boolean, H> anyMatch(Testable1Throwing1<? super V, ? extends H> predicate) {
        requireNonNull(predicate); return (method, traversable, ctx) -> !method.traverse(traversable, some(predicate.not()), ctx);
    }

    /**
     * Return a {@link Resolver} returning {@link Boolean} indicating whether all elements of
     * a {@link TraverseSupport} source match the provided {@link Testable1Throwing1} predicate.
     * <p>
     * This is a short-circuiting resolution operation.
     *
     * @param predicate the given {@link Testable1Throwing1} predicate
     * @param <V>       the type of item values
     * @param <E>       the type of traversal exception
     * @param <H>       the type of the predicate error
     * @return a {@link Resolver} returning {@link Boolean} indicating whether all elements of
     * a {@link TraverseSupport} source match the provided {@link Testable1Throwing1} predicate
     * @throws NullPointerException if the given {@link Testable1Throwing1} predicate is {@code null}
     */
    static <V, E extends Exception, H extends Exception> Resolver<V, E, Boolean, H> allMatch(Testable1Throwing1<? super V, ? extends H> predicate) {
        requireNonNull(predicate); return (method, traversable, ctx) -> method.traverse(traversable, some(predicate), ctx);
    }

    /**
     * Return a {@link Resolver} returning {@link Boolean} indicating whether no elements of
     * a {@link TraverseSupport} source match the provided {@link Testable1Throwing1} predicate.
     * <p>
     * This is a short-circuiting resolution operation.
     *
     * @param predicate the given {@link Testable1Throwing1} predicate
     * @param <V>       the type of item values
     * @param <E>       the type of traversal exception
     * @param <H>       the type of the resolution error
     * @return a {@link Resolver} returning {@link Boolean} indicating whether no elements of
     * a {@link TraverseSupport} source match the provided {@link Testable1Throwing1} predicate
     * @throws NullPointerException if the given {@link Testable1Throwing1} predicate is {@code null}
     */
    static <V, E extends Exception, H extends Exception> Resolver<V, E, Boolean, H> noneMatch(Testable1Throwing1<? super V, ? extends H> predicate) {
        return allMatch(predicate.not());
    }

    /**
     * Return a {@link Resolver} that reduces the elements using the specified {@link Collector}.
     * <p>
     * When the traversal is explicitly sequential or the {@link Collector} has the
     * {@link Collector.Characteristics#CONCURRENT CONCURRENT} characteristic, the reduction
     * proceeds as expected. Otherwise, during parallel traversal, intermediate results are
     * accumulated per worker {@link Thread} and then combined. The combination order follows
     * thread completion, prioritizing results from the last completed thread. This increases
     * the likelihood that the last combined result corresponds to the last emitted element,
     * which is beneficial for operations requiring the last-seen element.
     *
     * @param collector the {@link Collector} used to accumulate and reduce elements
     * @param <V>       the type of item values
     * @param <E>       the type of traversal exception
     * @param <R>       the type of the collecting result
     * @param <H>       the type of resolution exception
     * @return a {@link Resolver} that applies the specified {@link Collector}
     * @throws NullPointerException if {@code collector} is {@code null}
     */
    static <V, E extends Exception, R, H extends Exception> Resolver<V, E, R, H> reduce(Collector<? super V, ?, ? extends R> collector) {
        requireNonNull(collector); return new Object() {
            private <A> Resolver<V, E, R, H> reducing(Collector<? super V, A, ? extends R> collector) {
                return (method, traversable, ctx) -> {

                    Supplier<A> supplier = requireNonNull(collector.supplier());
                    BinaryOperator<A> combiner = requireNonNull(collector.combiner());
                    Function<A, ? extends R> finisher = requireNonNull(collector.finisher());
                    BiConsumer<A, ? super V> accumulator = requireNonNull(collector.accumulator());

                    if (method.isSequence() || collector.characteristics().contains(CONCURRENT)) {
                        A result = supplier.get(); method.traverse(traversable, each(value -> accumulator.accept(result, value)), ctx);
                        return finisher.apply(result);
                    } else {
                        Volatile.Int place = vol(0);

                        ConcurrentMap<Thread, A> results = new ConcurrentHashMap<>();
                        ConcurrentMap<Thread, Integer> places = new ConcurrentHashMap<>();

                        method.traverse(traversable, traverser -> {
                            Thread thread = currentThread();
                            A result = results.computeIfAbsent(thread, unused -> supplier.get());
                            traverser.each(value -> accumulator.accept(result, value));
                            places.put(thread, place.incr());
                        }, ctx);

                        // any of the parallel threads' writes to intermediate accumulation
                        // holder is flushed into the main memory i.e., happens-before we get here
                        return finisher.apply(results.entrySet().stream().sorted(comparing(e -> places.get(e.getKey())))
                                .map(Entry::getValue).reduce(combiner).orElseGet(supplier));
                    }
                };
            };
        }.reducing(collector);
    }

    /**
     * Return a {@link Resolver} that performs a reduction operation using the specified
     * {@link Evaluable2Throwing1} binary function and stores the result value in a {@link Container}.
     *
     * @param reducer the specified {@link Evaluable2Throwing1} binary function
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @param <H> the type of resolution exception
     * @return a {@link Resolver} that applies a reduction operation with the specified
     * {@link Evaluable2Throwing1} binary function and stores the result values in a {@link Container}
     * @throws NullPointerException if the provided {@link Evaluable2Throwing1} binary function is {@code null}
     */
    static <V, E extends Exception, H extends Exception> Resolver<V, E, Container<V>, H> reduce(Evaluable2Throwing1<? super V, ? super V, ? extends V, ? extends H> reducer) {
        requireNonNull(reducer); return reduce(new Object() {

            Collector<V, List<V>, Container<V>> collector() {
                return Collector.of(ArrayList::new, this::add, this::set, this::finish); }

            List<V> set(List<V> list1, List<V> list2) {
                if (!list2.isEmpty())
                    add(list1, list2.get(0));
                return list1; }

            void add(List<V> list, V value) {
                if (list.isEmpty()) list.add(value);
                else try { list.set(0, reducer.eval(list.get(0), value)); }
                    catch (Exception e) { sneak(e); } }

            Container<V> finish(List<V> l) {
                return Optional.of(l).map(List::iterator).filter(Iterator::hasNext)
                        .map(Iterator::next).map(HashContainer<V>::new)
                        .orElseGet(HashContainer<V>::new); }

        }.collector());
    }

    /**
     * Return a {@link Resolver} that performs a reduction of input elements
     * using the specified {@link Evaluable2Throwing1} binary function.
     *
     * @param init    the initial value for the reduction (also the value returned when there are no input elements)
     * @param reducer the specified {@link Evaluable2Throwing1} binary function
     * @param <V>     the type of target item values
     * @param <E>     the type of traversal exception
     * @param <H>     the type of resolution exception
     * @return a {@link Resolver} that performs a reduction of input elements using
     * the specified {@link Evaluable2Throwing1} binary function
     * @throws NullPointerException if the specified {@link Evaluable2Throwing1} binary function is {@code null}
     */
    static <V, E extends Exception, H extends Exception> Resolver<V, E, V, H> reduce(V init, Evaluable2Throwing1<? super V, ? super V, ? extends V, ? extends H> reducer) {
        return reduce(init, identity(), reducer);
    }

    /**
     * Return a {@link Resolver} that performs a reduction of input elements using a specified
     * {@link Evaluable1Throwing1} mapping function and {@link Evaluable2Throwing1} binary function.
     *
     * @param init    the initial value for the reduction (also the value returned when there are no input elements)
     * @param mapper  the specified {@link Evaluable1Throwing1} mapping function
     * @param reducer the specified {@link Evaluable2Throwing1} binary function
     * @param <S>     the type of source item values
     * @param <V>     the type of target item values
     * @param <E>     the type of traversal exception
     * @param <H>     the type of resolution exception
     * @return a {@link Resolver} that performs a reduction of input elements using a specified
     * {@link Evaluable1Throwing1} mapping function and {@link Evaluable2Throwing1} binary function
     * @throws NullPointerException if the specified {@link Evaluable1Throwing1} mapping function
     *                              or {@link Evaluable2Throwing1} binary function is {@code null}
     */
    static <S, V, E extends Exception, H extends Exception> Resolver<S, E, V, H> reduce(V init, Evaluable1Throwing1<? super S, ? extends V, ? extends H> mapper,
                                                                                        Evaluable2Throwing1<? super V, ? super V, ? extends V, ? extends H> reducer) {
        requireNonNull(mapper); requireNonNull(reducer); return reduce(new Object() {

            Collector<S, Object[], V> collector() {
                return Collector.of(this::init, this::add, this::set, this::finish); }

            Object[] init() {
                return new Object[]{init}; }

            void add(Object[] array, S value) {
                try { put(array, mapper.eval(value)); }
                catch (Exception e) { sneak(e); } }

            Object[] set(Object[] array1, Object[] array2) {
                put(array1, cast(array2[0])); return array1; }

            void put(Object[] array, V value) {
                try { array[0] = reducer.eval(cast(array[0]), value); }
                catch (Exception e) { sneak(e); } }

            V finish(Object[] array) {
                return cast(array[0]); }

        }.collector());
    }

    /**
     * Return explicitly sequential Traversable by the given {@link TraverseSupport} source.
     *
     * @param traverseSupport the given {@link TraverseSupport}
     * @param <V>             the type of item values
     * @param <E>             the type of traversal exception
     * @return explicitly sequential Traversable by the given {@link TraverseSupport}  source
     * @throws NullPointerException if the given {@link TraverseSupport} source is {@code null}
     */
    static <V, E extends Exception> Traversable<V, E> sequence(TraverseSupport<? extends V, ? extends E> traverseSupport) {
        requireNonNull(traverseSupport); return new Traversable<>() {
            public <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {
                return (method.isTraverse() ? SEQUENCE : method).traverse(traverseSupport, traverse, ctx); }
        };
    }

    /**
     * Return explicitly parallel Traversable by the given {@link TraverseSupport}  source.
     *
     * @param traverseSupport the given {@link TraverseSupport}
     * @param <V>             the type of item values
     * @param <E>             the type of traversal exception
     * @return explicitly parallel Traversable by the given {@link TraverseSupport} source
     * @throws NullPointerException if the given {@link TraverseSupport} source is {@code null}
     */
    static <V, E extends Exception> Traversable<V, E> parallel(TraverseSupport<? extends V, ? extends E> traverseSupport) {
        requireNonNull(traverseSupport); return new Traversable<>() {
            public <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {
                return (method.isTraverse() ? PARALLEL : method).traverse(traverseSupport, traverse, ctx); }
        };
    }


    /**
     * Return an idle {@link Traversable}, returning no elements and executing nothing.
     *
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return an idle {@link Traversable}, returning no elements and executing nothing
     */
    static <V, E extends Exception> Traversable<V, E> traversable() {
        return new Traversable<>() {
            public <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {
                return traverse.complete(traverser()); }
        };
    }

    /**
     * Return a Traversable by the given {@link TraverseSupport}  source.
     *
     * @param traverseSupport the given {@link TraverseSupport} source
     * @param <V>             the type of item values
     * @param <E>             the type of traversal exception
     * @return a Traversable by the given {@link TraverseSupport} source
     * @throws NullPointerException if the given {@link TraverseSupport} source is {@code null}
     */
    static <V, E extends Exception> Traversable<V, E> traversable(TraverseSupport<? extends V, ? extends E> traverseSupport) {
        requireNonNull(traverseSupport); return new Traversable<>() {
            public <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {
                return method.traverse(traverseSupport, traverse, ctx); }
        };
    }

    /**
     * Create a new {@link Association} context using the specified key-value pair.
     *
     * @param key   the key for the association
     * @param value the value for the association
     * @return a new {@link Association} context based on the given key and value
     * @throws NullPointerException if the provided key or value is {@code null}
     */
    static Association<Object, Object, ?> context(Object key, Object value) {
        return context(pair(key, value));
    }

    /**
     * Create a new {@link Association} context based on the provided {@link KeyValue} vararg entries.
     *
     * @param kvs the {@link KeyValue} vararg entries for the context
     * @return a new {@link Association} context created from the specified {@link KeyValue} entries
     * @throws NullPointerException if the key or value in any of the specified entries is {@code null}
     */
    static Association<Object, Object, ?> context(KeyValue<?, ?>... kvs) {
        if (kvs.length > 8)
            return SnapContext.context(kvs);

        NestContext context = NestContext.context();
        for (KeyValue<?, ?> kv : kvs)
            context = NestContext.context(context, kv.getKey(), kv.getValue());

        return context;
    }

    /**
     * Return a new {@link Association} context by adding a new entry to the specified one.
     *
     * @param context the specified {@link Association} context
     * @param key     the key of the new entry
     * @param value   the value to set for the new entry
     * @return a new {@link Association} context with the added entry
     * @throws NullPointerException if the given {@link Association} context, key, or value is {@code null}
     */
    static Association<Object, Object, ?> put(Association<?, ?, ?> context, Object key, Object value) {
        return context.size() < 8 ? NestContext.context(context, key, value)
                : SnapContext.context(context, key, value);
    }

    /**
     * Return a new {@link Association} context by deleting the entry with the specified key.
     *
     * @param context the specified {@link Association} context
     * @param key     the key of the entry to delete
     * @return a new {@link Association} context with the entry deleted based on the given key
     * @throws NullPointerException if the given {@link Association} context or key is {@code null}
     */
    static Association<Object, Object, ?> remove(Association<?, ?, ?> context, Object key) {
        return context.size() < 8 ? NestContext.context(context, key)
                : SnapContext.context(context, key);
    }
}
