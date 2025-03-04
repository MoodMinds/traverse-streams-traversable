package org.moodminds.traverse;

import org.moodminds.elemental.Association;
import org.moodminds.valuable.Variable.Boolean;

import static java.util.Objects.requireNonNull;
import static org.moodminds.valuable.Variable.var;

/**
 * A template implementation of the {@link Traversable} interface that allows for
 * an early termination, which is still considered as completion of the traversal.
 *
 * @param <V> the type of item values
 * @param <E> the type of traversal exception
 */
abstract class BreachTraversable<V, E extends Exception> implements Traversable<V, E> {

    /**
     * The source {@link TraverseSupport} holder field.
     */
    private final TraverseSupport<? extends V, ? extends E> traversable;

    /**
     * Construct the object with the given source {@link TraverseSupport}.
     *
     * @param traversable the given source {@link TraverseSupport}
     * @throws NullPointerException if the source {@link TraverseSupport} is {@code null}
     */
    protected BreachTraversable(TraverseSupport<? extends V, ? extends E> traversable) {
        this.traversable = requireNonNull(traversable);
    }

    @Override
    public <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {
        Boolean breached = var(false); return this.<H1, H2>traverse(method, traversable, traverser -> {
            if (!traverse.complete(traverser)) breached.flg = true;
        }, ctx) || !breached.flg;
    }

    protected abstract <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, TraverseSupport<? extends V, ? extends E> traversable,
                                                                                     Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2;
}
