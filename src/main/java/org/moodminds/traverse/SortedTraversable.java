package org.moodminds.traverse;

import org.moodminds.elemental.Association;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * An implementation of the {@link Traversable} interface that gathers and arranges items from
 * the {@link TraverseSupport} using a specified {@link Comparator} source prior to the traversal.
 *
 * @param <V> the type of item values
 * @param <E> the type of traversal exception
 */
public class SortedTraversable<V, E extends Exception> extends StreamTraversable<V, E> {

    /**
     * The {@link TraverseSupport} source holder field.
     */
    private final TraverseSupport<? extends V, ? extends E> traversable;

    /**
     * The {@link Comparator} comparator holder field.
     */
    private final Comparator<? super V> comparator;

    /**
     * Construct the object with the given {@link TraverseSupport}
     * source and {@link Comparator} comparator.
     *
     * @param traversable the given {@link TraverseSupport} source
     * @param comparator the given {@link Comparator} comparator
     * @throws NullPointerException if the given {@link TraverseSupport} source
     * or {@link Comparator} comparator is {@code null}
     */
    protected SortedTraversable(TraverseSupport<? extends V, ? extends E> traversable, Comparator<? super V> comparator) {
        this.traversable = requireNonNull(traversable); this.comparator = requireNonNull(comparator);
    }

    @Override
    protected Stream<V> stream(TraverseMethod method, Association<?, ?, ?> ctx) throws E {
        List<V> collection = Traversable.<V, E, List<V>, E>reduce(toList())
                .resolve(method, traversable, ctx);
        collection.sort(comparator); return collection.stream();
    }

    /**
     * Return a {@link Traversable} implementation that sorts {@link Comparable} items
     * of the {@link TraverseSupport} source before consumption using.
     *
     * @param traversable the given {@link TraverseSupport} source
     * @param <V> the type of {@link Comparable} item values
     * @param <E> the type of traversal exception
     * @return a {@link Traversable} implementation that sorts {@link Comparable} items
     * of the {@link TraverseSupport} source before consumption using a {@link Comparator} comparator
     * @throws NullPointerException if the given {@link TraverseSupport} source is {@code null}
     */
    public static <V extends Comparable<V>, E extends Exception> Traversable<V, E> sorted(TraverseSupport<? extends V, ? extends E> traversable) {
        return sorted(traversable, naturalOrder());
    }

    /**
     * Return a {@link Traversable} implementation that gathers and arranges items from
     * the {@link TraverseSupport} using a specified {@link Comparator} source prior to the traversal.
     *
     * @param traversable the given {@link TraverseSupport} source
     * @param comparator the given {@link Comparator} comparator
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return a {@link Traversable} implementation that gathers and arranges items from
     * the {@link TraverseSupport} using a specified {@link Comparator} source prior to the traversal
     * @throws NullPointerException if the given {@link TraverseSupport} source
     * or {@link Comparator} comparator is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> sorted(TraverseSupport<? extends V, ? extends E> traversable, Comparator<? super V> comparator) {
        return new SortedTraversable<>(traversable, comparator);
    }
}
