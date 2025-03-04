package org.moodminds.traverse;

import org.moodminds.elemental.Association;
import org.moodminds.function.Executable1Throwing1;
import org.moodminds.function.Testable1Throwing1;
import org.moodminds.valuable.Valuable.Long;
import org.moodminds.valuable.Variable.Boolean;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.moodminds.valuable.Variable.var;
import static org.moodminds.valuable.Volatile.vol;

/**
 * Represents a {@link Traversable} implementation that immediately terminates
 * the entire traversal upon reaching the specified limit on the number of items.
 *
 * @param <V> the type of item values
 * @param <E> the type of traversal exception
 */
public class LimitTraversable<V, E extends Exception> extends BreachTraversable<V, E> {

    /**
     * The items limit number holder field.
     */
    private final long number;

    /**
     * Construct the object with the given source {@link TraverseSupport} and items limit number.
     *
     * @param traversable the given source {@link TraverseSupport}
     * @param number the given items limit number
     * @throws NullPointerException if the source {@link TraverseSupport} is {@code null}
     * @throws IllegalArgumentException if the given limit number less than 0
     */
    protected LimitTraversable(TraverseSupport<? extends V, ? extends E> traversable, long number) {
        super(traversable);
        if ((this.number = number) < 0L)
            throw new IllegalArgumentException(format("Negative limit number: %d.", number));
    }

    @Override
    protected <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, TraverseSupport<? extends V, ? extends E> traversable, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {

        Long counter = method.isSequence() ? var(0L) : vol(0L);

        return method.<V, E, H1, H2>traverse(traversable, traverser -> traverse.exec(new Traverser<>() {

            @Override public <H extends Exception> boolean next(Executable1Throwing1<? super V, ? extends H> consumer) throws E, H {
                requireNonNull(consumer); Boolean fulfilled = var(false); return counter.get() < number && traverser.<E, H>next(value -> {
                    if (counter.incr() <= number) {
                        fulfilled.flg = true; consumer.exec(value); }
                }) && fulfilled.flg; }

            @Override public <H extends Exception> boolean some(Testable1Throwing1<? super V, ? extends H> consumer) throws E, H {
                requireNonNull(consumer); Boolean fulfilled = var(false); return counter.get() < number && traverser.<E, H>some(value -> {
                    long count = counter.incr();
                    if (count < number)
                        return (fulfilled.flg = true) && consumer.test(value);
                    if (count == number) {
                        fulfilled.flg = !consumer.test(value); return false;
                    } return fulfilled.flg = false;
                }) && fulfilled.flg; }

        }), ctx);
    }

    /**
     * Return a {@link Traversable} implementation that emits items from the given
     * {@link TraverseSupport} source while not reaching the given items limit.
     *
     * @param traversable the given source {@link TraverseSupport}
     * @param number the given items limit number
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return a {@link Traversable} implementation that emits items from the given
     * {@link TraverseSupport} source while not reaching the given items limit
     * @throws NullPointerException if the given source {@link TraverseSupport} is {@code null}
     * @throws IllegalArgumentException if the given limit number less than 0
     */
    public static <V, E extends Exception> Traversable<V, E> limit(TraverseSupport<? extends V, ? extends E> traversable, long number) {
        return new LimitTraversable<>(traversable, number);
    }
}
