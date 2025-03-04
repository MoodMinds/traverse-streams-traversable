package org.moodminds.traverse;

import org.moodminds.elemental.Association;
import org.moodminds.function.EvaluableThrowing1;

import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.LongStream.range;
import static org.moodminds.sneaky.Sneak.sneak;

/**
 * A {@link Traversable} implementation that supplies items using a {@link EvaluableThrowing1} supplier.
 *
 * @param <V> the type of item values
 * @param <E> the type of traversal exception
 */
public class SupplyTraversable<V, E extends Exception> extends StreamTraversable<V, E> {

    /**
     * The {@link EvaluableThrowing1} value supplier holder field.
     */
    private final EvaluableThrowing1<? extends V, ? extends E> evaluable;

    /**
     * The supplying times number holder field.
     */
    private final Long times;

    /**
     * Construct the object with given the given {@link EvaluableThrowing1} supplier.
     *
     * @param evaluable the given {@link EvaluableThrowing1} supplier
     * @throws NullPointerException if the given {@link EvaluableThrowing1} supplier is {@code null}
     */
    protected SupplyTraversable(EvaluableThrowing1<? extends V, ? extends E> evaluable) {
        this.evaluable = requireNonNull(evaluable); this.times = null;
    }

    /**
     * Construct the object with given the given {@link EvaluableThrowing1} supplier and a specified number of times.
     *
     * @param evaluable the supplier to obtain item values
     * @param times the given number of times to supply
     * @throws NullPointerException if the given {@link EvaluableThrowing1} supplier is {@code null}
     * @throws IllegalArgumentException if the given times number is less than 0
     */
    protected SupplyTraversable(EvaluableThrowing1<? extends V, ? extends E> evaluable, long times) {
        this.evaluable = requireNonNull(evaluable);
        if ((this.times = times) < 0L)
            throw new IllegalArgumentException(format("Negative times number: %d.", times));
    }

    @Override
    protected Stream<V> stream(TraverseMethod method, Association<?, ?, ?> ctx) throws E {
        return times == null ? Stream.generate(() -> {
            try { return evaluable.eval(); }
            catch (Exception ex) { return sneak(ex); }
        }) : range(0, times).mapToObj(unused -> {
            try { return evaluable.eval(); }
            catch (Exception ex) { return sneak(ex); }
        });
    }


    /**
     * Return a {@link Traversable} that provides items using the given {@link EvaluableThrowing1} supplier.
     *
     * @param evaluable the supplier to obtain item values
     * @param <V> the type of item value
     * @param <E> the type of traversal exception
     * @return a {@link Traversable} backed by the specified {@link EvaluableThrowing1} supplier
     * @throws NullPointerException if the given {@link EvaluableThrowing1} supplier is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> supply(EvaluableThrowing1<? extends V, ? extends E> evaluable) {
        return new SupplyTraversable<>(evaluable);
    }

    /**
     * Return a {@link Traversable} that provides items using the given {@link EvaluableThrowing1} supplier n-times.
     *
     * @param evaluable the supplier to obtain item values
     * @param times the given times number to supply
     * @param <V> the type of item value
     * @param <E> the type of traversal exception
     * @return a {@link Traversable} backed by the specified {@link EvaluableThrowing1} supplier
     * @throws NullPointerException if the given {@link EvaluableThrowing1} supplier is {@code null}
     * @throws IllegalArgumentException if the given times number less than 0
     */
    public static <V, E extends Exception> Traversable<V, E> supply(EvaluableThrowing1<? extends V, ? extends E> evaluable, long times) {
        return new SupplyTraversable<>(evaluable, times);
    }
}
