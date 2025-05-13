package org.moodminds.traverse;

import org.moodminds.elemental.Association;
import org.moodminds.function.Evaluable1Throwing1;
import org.moodminds.function.Executable1Throwing1;
import org.moodminds.function.Testable1Throwing1;
import org.moodminds.function.Testable1Throwing3;
import org.moodminds.function.TestableThrowing1;
import org.moodminds.sneaky.Sneak;
import org.moodminds.valuable.Variable;
import org.moodminds.valuable.Variable.Boolean;
import org.moodminds.valuable.Variable.Long;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.moodminds.function.TestableThrowing1.anyway;
import static org.moodminds.sneaky.Sneak.sneak;
import static org.moodminds.valuable.Variable.var;

/**
 * A {@link Traversable} implementation that catches traversal exceptions and handles them using a specified handler.
 *
 * @param <V> the type of item values
 * @param <C> the type of catching exception
 * @param <E> the type of traversal exception
 */
public abstract class CatchTraversable<V, C extends Exception, E extends Exception> implements Traversable<V, E> {

    /**
     * The {@link TraverseSupport} source holder field.
     */
    private final TraverseSupport<? extends V, ? extends C> traversable;

    /**
     * Construct the object with the given {@link TraverseSupport} source.
     *
     * @param traversable the given {@link TraverseSupport} source
     * @throws NullPointerException if the specified {@link TraverseSupport} source is {@code null}
     */
    protected CatchTraversable(TraverseSupport<? extends V, ? extends C> traversable) {
        this.traversable = requireNonNull(traversable);
    }

    @Override
    public <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {
        return traverse(new Traversal<>() {
            @Override
            public <X1 extends Exception, X2 extends Exception> boolean handle(Testable1Throwing3<? super Throwable, ? extends E, ? extends X1, ? extends X2> catcher) throws E, X1, X2 {

                Boolean ignore = var(false); Variable<Throwable> thrown = var();

                try {
                    return method.<V, C, C, X1, X2>traverse(traversable, traverser -> {

                        Variable<Throwable> caught = var();

                        try {
                            traverse.exec(new Traverser<V, E>() {

                                @Override public <H extends Exception> boolean next(Executable1Throwing1<? super V, ? extends H> consumer) throws E, H {
                                    requireNonNull(consumer); Boolean next = var(false); try {
                                        return traverser.<H, E>next(value -> {
                                            next.flg = true; consumer.exec(value); });
                                    } catch (Throwable ex) { return handle(!next.flg, ex); } }

                                @Override public <H extends Exception> boolean next(long number, Executable1Throwing1<? super V, ? extends H> consumer) throws E, H {
                                    requireNonNull(consumer); Boolean next = var(false); try {
                                        return traverser.<H, E>next(number, value -> {
                                            next.flg = true; consumer.exec(value); next.flg = false; });
                                    } catch (Throwable ex) { return handle(!next.flg, ex); } }

                                @Override public <H extends Exception> boolean some(Testable1Throwing1<? super V, ? extends H> consumer) throws E, H {
                                    requireNonNull(consumer); Boolean next = var(false); try {
                                        return traverser.<H, E>some(value -> {
                                            next.flg = true; boolean some = consumer.test(value); next.flg = false; return some; });
                                    } catch (Throwable ex) { return handle(!next.flg, ex); } }

                                @Override public <H extends Exception> void each(Executable1Throwing1<? super V, ? extends H> consumer) throws E, H {
                                    requireNonNull(consumer); Boolean next = var(false); try {
                                        traverser.<H, E>each(value -> {
                                            next.flg = true; consumer.exec(value); next.flg = false; });
                                    } catch (Throwable ex) { handle(!next.flg, ex); } }

                                private <H extends Exception> boolean handle(boolean handle, Throwable ex) throws E, H {
                                    if (handle) {
                                        caught.val = ex; thrown.val = ex; return false;
                                    } else return sneak(ex);
                                }

                            });

                        } catch (Throwable ex) {
                            sneak(ex instanceof Error ? ex : ofNullable(caught.val).orElseGet(() -> {
                                ignore.flg = true; return ex;
                            }));
                        }

                        ofNullable(caught.val).ifPresent(Sneak::sneak);

                    }, ctx);
                } catch (Throwable ex) {
                    return ex instanceof Error || ex != thrown.val && ignore.flg ? sneak(ex) : catcher.test(ex);
                }
            }
        }, method, traverse, ctx);
    }

    protected abstract <H1 extends Exception, H2 extends Exception> boolean traverse(Traversal<V, E> traversal, TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2;


    /**
     * Interface for executing the traversal of a {@link TraverseSupport} source, with the ability to apply
     * a specified {@link Testable1Throwing3} as a handler of the potential traversal exception.
     *
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     */
    protected interface Traversal<V, E extends Exception> {

        /**
         * Perform the traversal, applying the provided {@link Testable1Throwing3} exception handler.
         *
         * @param handle the specified {@link Testable1Throwing3} exception handler
         * @return a completion flag indicating whether the source was completely traversed or not
         * @param <H1> the possible exception type thrown by the exception handler
         * @param <H2> another possible exception type thrown by the exception handler
         * @throws E in case of a traversal error
         * @throws H1 in case of an error in the first exception handler
         * @throws H2 in case of an error in the second exception handler
         * @throws NullPointerException if the specified {@link Testable1Throwing3} exception handler is {@code null}
         */
        <H1 extends Exception, H2 extends Exception> boolean handle(Testable1Throwing3<? super Throwable, ? extends E, ? extends H1, ? extends H2> handle) throws E, H1, H2;
    }


    /**
     * An implementation of the {@link CatchTraversable} that retries the execution until some condition.
     *
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     */
    protected static abstract class RetryTraversable<V, E extends Exception>
            extends CatchTraversable<V, E, E> {

        protected RetryTraversable(TraverseSupport<? extends V, ? extends E> traversable) {
            super(traversable);
        }

        @Override
        protected <H1 extends Exception, H2 extends Exception> boolean traverse(Traversal<V, E> traversal, TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {

            boolean result; TestableThrowing1<? extends E> retrial = retrial(); Boolean retry = var(false);

            do {
                retry.flg = false; result = traversal.handle(caught -> retrial.test() ? (retry.flg = true)
                        : Sneak.<java.lang.Boolean, E>sneak(caught));
            } while (retry.flg);

            return result;
        }

        protected abstract TestableThrowing1<? extends E> retrial() throws E;
    }


    /**
     * Return a {@link Traversable} implementation that catches traversal exceptions in the given {@link TraverseSupport}
     * source and handles them using the specified {@link Evaluable1Throwing1} exception handler.
     *
     * @param traversable the given {@link TraverseSupport} source
     * @param handler the specified {@link Evaluable1Throwing1} exception handler
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return a {@link Traversable} implementation that catches traversal exceptions in the given {@link TraverseSupport}
     * source and handles them using the specified {@link Evaluable1Throwing1} exception handler
     * @throws NullPointerException if the specified {@link TraverseSupport} source
     * or {@link Evaluable1Throwing1} exception handler is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> resume(TraverseSupport<? extends V, ?> traversable,
                                                                    Evaluable1Throwing1<? super Throwable, ? extends TraverseSupport<? extends V, ? extends E>, ? extends E> handler) {
        requireNonNull(handler); return new CatchTraversable<V, Exception, E>(traversable) {
            @Override protected <H1 extends Exception, H2 extends Exception> boolean traverse(Traversal<V, E> traversal, TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {
                return traversal.<H1, H2>handle(caught -> method.traverse(handler.eval(caught), traverse, ctx)); }
        };
    }

    /**
     * Return a {@link Traversable} implementation that catches traversal exceptions
     * in the given {@link TraverseSupport} source and retries infinitely until success.
     *
     * @param traversable the given {@link TraverseSupport} source
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return a {@link Traversable} implementation that catches traversal exceptions
     * in the given {@link TraverseSupport} source and retries infinitely until success
     * @throws NullPointerException if the specified {@link TraverseSupport} source is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> retry(TraverseSupport<? extends V, ? extends E> traversable) {
        return new RetryTraversable<>(traversable) {
            @Override protected TestableThrowing1<? extends E> retrial() throws E { return anyway(); } };
    }

    /**
     * Return a {@link Traversable} implementation that catches traversal exceptions in the
     * given {@link TraverseSupport} source and retries the given number of times until success.
     *
     * @param traversable the given {@link TraverseSupport} source
     * @param retries the given number of retries
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return a {@link Traversable} implementation that catches traversal exceptions
     * in the given {@link TraverseSupport} source and retries infinitely until success
     * @throws NullPointerException if the specified {@link TraverseSupport} source is {@code null}
     * @throws IllegalArgumentException if the specified retry number is less than 0
     */
    public static <V, E extends Exception> Traversable<V, E> retry(TraverseSupport<? extends V, ? extends E> traversable, long retries) {
        if (retries < 0L)
            throw new IllegalArgumentException(format("Negative retry number: %d.", retries));
        return new RetryTraversable<V, E>(traversable) {
            @Override protected TestableThrowing1<? extends E> retrial() throws E {
                Long tries = var(0L); return () -> ++tries.num <= retries; }
        };
    }
}
