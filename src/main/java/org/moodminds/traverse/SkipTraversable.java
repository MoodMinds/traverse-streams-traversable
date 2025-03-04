package org.moodminds.traverse;

import org.moodminds.function.Testable1Throwing1;
import org.moodminds.valuable.Valuable;
import org.moodminds.valuable.Valuable.Long;

import static java.lang.String.format;
import static org.moodminds.function.Testable1Throwing1.anyway;
import static org.moodminds.valuable.Variable.var;
import static org.moodminds.valuable.Volatile.vol;

/**
 * A {@link Traversable} implementation that skips items until reaching the specified count of items.
 *
 * @param <V> the type of item values
 * @param <E> the type of traversal exception
 */
public class SkipTraversable<V, E extends Exception> extends FilterTraversable<V, E> {

    /**
     * The limit number of items' holder field.
     */
    private final long number;

    /**
     * Construct the object with the given source {@link TraverseSupport} and items to skip number.
     *
     * @param traversable the given source {@link TraverseSupport}
     * @param number the given items to skip number
     * @throws NullPointerException if the {@link TraverseSupport} source is {@code null}
     * @throws IllegalArgumentException if the given items to skip number less than 0
     */
    protected SkipTraversable(TraverseSupport<? extends V, ? extends E> traversable, long number) {
        super(traversable);
        if ((this.number = number) < 0L)
            throw new IllegalArgumentException(format("Negative skip number: %d.", number));
    }

    @Override
    protected Testable1Throwing1<? super V, ? extends E> filter(TraverseMethod method) throws E {

        Valuable<Testable1Throwing1<V, E>> skip; Long counter;

        if (method.isSequence()) {
            counter = var(0L); skip = var();
        } else { counter = vol(0L); skip = vol(); }

        skip.put(unused -> {
            if (counter.incr() <= number) return false;
            else { skip.put(anyway()); return true; }
        });

        return value -> skip.get().test(value);
    }


    /**
     * Return a {@link Traversable} implementation that skips items until reaching the specified count of items.
     *
     * @param traversable the given source {@link TraverseSupport} source
     * @param number the given items to skip number
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return a {@link Traversable} implementation that skips items until reaching the specified count of items
     * @throws NullPointerException if the {@link TraverseSupport} source is {@code null}
     * @throws IllegalArgumentException if the given items to skip number less than 0
     */
    public static <V, E extends Exception> Traversable<V, E> skip(TraverseSupport<? extends V, ? extends E> traversable, long number) {
        return new SkipTraversable<>(traversable, number);
    }
}
