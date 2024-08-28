package org.moodminds.traverse;

import org.moodminds.elemental.Association;
import org.moodminds.function.Evaluable1Throwing1;
import org.moodminds.function.EvaluableThrowing1;

import static java.util.Objects.requireNonNull;

/**
 * An implementation of the {@link Traversable} that acquires the actual
 * {@link TraverseSupport} source using a retrieval supplier.
 *
 * @param <V> the type of item values
 * @param <E> the type of traversal exception
 */
public class DeferTraversable<V, E extends Exception> implements Traversable<V, E> {

    /**
     * The actual {@link TraverseSupport} source supplier holder field.
     */
    private final Evaluable1Throwing1<? super Association<?, ?, ?>, ? extends TraverseSupport<? extends V, ? extends E>, ? extends E> defer;

    /**
     * Construct the object with the given {@link EvaluableThrowing1}
     * supplier of an actual {@link TraverseSupport} source.
     *
     * @param defer the given {@link EvaluableThrowing1} supplier of an actual {@link TraverseSupport} source
     * @throws NullPointerException if  the given {@link EvaluableThrowing1} supplier is {@code null}
     */
    protected DeferTraversable(EvaluableThrowing1<? extends TraverseSupport<? extends V, ? extends E>, ? extends E> defer) {
        this(context -> defer.eval());
    }

    /**
     * Construct the object with the given {@link Evaluable1Throwing1}
     * supplier of an actual {@link TraverseSupport} source.
     *
     * @param defer the given {@link Evaluable1Throwing1} supplier of an actual {@link TraverseSupport} source
     * @throws NullPointerException if the given {@link Evaluable1Throwing1} supplier is {@code null}
     */
    protected DeferTraversable(Evaluable1Throwing1<? super Association<?, ?, ?>, ? extends TraverseSupport<? extends V, ? extends E>, ? extends E> defer) {
        this.defer = requireNonNull(defer);
    }

    @Override
    public <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {
        return method.traverse(defer.eval(ctx), traverse, ctx);
    }


    /**
     * Return an implementation of the {@link Traversable} that acquires the actual
     * {@link TraverseSupport} source using a retrieval {@link EvaluableThrowing1} supplier.
     *
     * @param defer the given {@link EvaluableThrowing1} supplier of an actual {@link TraverseSupport} source
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return an implementation of the {@link Traversable} that acquires the actual
     * {@link TraverseSupport} source using a retrieval {@link EvaluableThrowing1} supplier
     * @throws NullPointerException if the given {@link EvaluableThrowing1} supplier is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> defer(EvaluableThrowing1<? extends TraverseSupport<? extends V, ? extends E>, ? extends E> defer) {
        return new DeferTraversable<>(defer);
    }

    /**
     * Return an implementation of the {@link Traversable} that acquires the actual
     * {@link TraverseSupport} source using a retrieval {@link Evaluable1Throwing1} supplier.
     *
     * @param defer the given {@link Evaluable1Throwing1} supplier of an actual {@link TraverseSupport} source
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return an implementation of the {@link Traversable} that acquires the actual
     * {@link TraverseSupport} source using a retrieval {@link Evaluable1Throwing1} supplier
     * @throws NullPointerException if the given {@link Evaluable1Throwing1} supplier is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> defer(Evaluable1Throwing1<? super Association<?, ?, ?>, ? extends TraverseSupport<? extends V, ? extends E>, ? extends E> defer) {
        return new DeferTraversable<>(defer);
    }
}
