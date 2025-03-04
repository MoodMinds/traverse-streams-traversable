package org.moodminds.traverse;

import org.moodminds.elemental.Association;

import java.lang.reflect.Array;

import static java.lang.System.arraycopy;
import static java.util.Objects.requireNonNull;
import static org.moodminds.sneaky.Cast.cast;

/**
 * A concatenating {@link Traversable} that emits items from each subsequent
 * {@link TraverseSupport} source following the emission of the previous source.
 *
 * @param <V> the type of item values
 * @param <E> the type of traversal exception
 */
public class ConcatTraversable<V, E extends Exception> implements Traversable<V, E> {

    /**
     * The {@link TraverseSupport} sources holder field.
     */
    private final TraverseSupport<? extends V, ? extends E>[] traversables;

    /**
     * Construct the object by the given {@link TraverseSupport} sources.
     *
     * @param traversable1 the given {@link TraverseSupport} source 1
     * @param traversable2 the given {@link TraverseSupport} source 2
     * @param traversables the given {@link TraverseSupport} sources
     * @throws NullPointerException if any of the {@link TraverseSupport} sources is {@code null}
     */
    @SafeVarargs
    protected ConcatTraversable(TraverseSupport<? extends V, ? extends E> traversable1,
                                TraverseSupport<? extends V, ? extends E> traversable2,
                                TraverseSupport<? extends V, ? extends E>... traversables) {
        this.traversables = cast(Array.newInstance(TraverseSupport.class, traversables.length + 2));
        this.traversables[0] = requireNonNull(traversable1);
        this.traversables[1] = requireNonNull(traversable2);
        arraycopy(traversables, 0, this.traversables, 2, traversables.length);
    }

    @Override
    public <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {
        for (TraverseSupport<? extends V, ? extends E> traversable : traversables)
            if (!method.traverse(traversable, traverse, ctx))
                return false;
        return true;
    }

    /**
     * Return a concatenating {@link Traversable} that emits items from each subsequent
     * {@link TraverseSupport} source following the emission of the previous source.
     *
     * @param traversable1 the given {@link TraverseSupport} source 1
     * @param traversable2 the given {@link TraverseSupport} source 2
     * @param traversables the given {@link TraverseSupport} sources
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return a concatenating {@link Traversable} that emits items from each subsequent
     * {@link TraverseSupport} source following the emission of the previous source
     * @throws NullPointerException if any of the {@link TraverseSupport} sources is {@code null}
     */
    @SafeVarargs
    public static <V, E extends Exception> Traversable<V, E> concat(TraverseSupport<? extends V, ? extends E> traversable1,
                                                                    TraverseSupport<? extends V, ? extends E> traversable2,
                                                                    TraverseSupport<? extends V, ? extends E>... traversables) {
        return new ConcatTraversable<>(traversable1, traversable2, traversables);
    }
}
