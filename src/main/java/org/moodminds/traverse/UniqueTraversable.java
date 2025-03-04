package org.moodminds.traverse;

import org.moodminds.function.Testable1Throwing1;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.newSetFromMap;
import static java.util.Objects.requireNonNullElse;

/**
 * A {@link Traversable} implementation that ensures uniqueness among emitted items.
 *
 * @param <V> the type of item value
 * @param <E> the type of traversal exception
 */
public class UniqueTraversable<V, E extends Exception> extends FilterTraversable<V, E> {

    /**
     * Masking {@code null} value holder field.
     */
    protected static final Object NULL = new Object();

    /**
     * Construct the object with the given {@link TraverseSupport} source.
     *
     * @param traversable the given {@link TraverseSupport} source
     * @throws NullPointerException if the given {@link TraverseSupport} source is {@code null}
     */
    protected UniqueTraversable(TraverseSupport<? extends V, ? extends E> traversable) {
        super(traversable);
    }

    @Override
    protected Testable1Throwing1<? super V, ? extends E> filter(TraverseMethod method) {
        Set<Object> seen = method.isSequence() ? new HashSet<>()
                : newSetFromMap(new ConcurrentHashMap<>());
        return value -> seen.add(requireNonNullElse(value, NULL));
    }


    /**
     * Return a {@link Traversable} that ensures uniqueness among emitted items.
     *
     * @param traversable the source traversable
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return a new {@link Traversable} that ensures uniqueness among emitted items
     * @throws NullPointerException if the given {@link TraverseSupport} source is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> unique(TraverseSupport<? extends V, ? extends E> traversable) {
        return new UniqueTraversable<>(traversable);
    }
}
