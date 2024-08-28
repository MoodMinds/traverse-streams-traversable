package org.moodminds.traverse;

import org.moodminds.elemental.Association;
import org.moodminds.function.Executable1Throwing1;
import org.moodminds.function.Testable1Throwing1;

import static java.util.Objects.requireNonNull;

/**
 * Represents a {@link Traversable} implementation that filters
 * items based on a {@link Testable1Throwing1} predicate.
 *
 * @param <V> the type of item values
 * @param <E> the type of traversal exception
 */
public abstract class FilterTraversable<V, E extends Exception> implements Traversable<V, E> {

    /**
     * The {@link TraverseSupport} source holder field.
     */
    private final TraverseSupport<? extends V, ? extends E> traversable;

    /**
     * Construct the object with the given {@link TraverseSupport} source.
     *
     * @param traversable the given {@link TraverseSupport} source
     * @throws NullPointerException if the given {@link TraverseSupport} source is {@code null}
     */
    protected FilterTraversable(TraverseSupport<? extends V, ? extends E> traversable) {
        this.traversable = requireNonNull(traversable);
    }

    @Override
    public <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {

        Testable1Throwing1<? super V, ? extends E> filter = filter(method);

        return method.<V, E, H1, H2>traverse(traversable, traverser -> traverse.exec(new Traverser<>() {

            @Override public <X extends Exception> boolean next(Executable1Throwing1<? super V, ? extends X> consumer) throws E, X {
                requireNonNull(consumer); return traverser.<E, X>some(value -> {
                    if (!filter.test(value)) return true;
                    consumer.exec(value); return false;
                }); }

            @Override public <X extends Exception> boolean some(Testable1Throwing1<? super V, ? extends X> consumer) throws E, X {
                 requireNonNull(consumer); return traverser.<E, X>some(value ->
                        !filter.test(value) || consumer.test(value)); }

            @Override public <X extends Exception> void each(Executable1Throwing1<? super V, ? extends X> consumer) throws E, X {
                requireNonNull(consumer); traverser.<E, X>each(value -> {
                    if (filter.test(value)) consumer.exec(value);
                }); }

        }), ctx);
    }

    protected abstract Testable1Throwing1<? super V, ? extends E> filter(TraverseMethod method) throws E;


    /**
     * Return a {@link Traversable} that filters items based on the given {@link Testable1Throwing1} predicate.
     *
     * @param traversable the given {@link TraverseSupport} source
     * @param filter the given {@link Testable1Throwing1} predicate
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return a new {@link Traversable} emitting items that satisfy the specified predicate
     * @throws NullPointerException if the given {@link TraverseSupport} source
     * or {@link Testable1Throwing1} predicate is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> filter(TraverseSupport<? extends V, ? extends E> traversable, Testable1Throwing1<? super V, ? extends E> filter) {
        requireNonNull(filter); return new FilterTraversable<>(traversable) {
            @Override protected Testable1Throwing1<? super V, ? extends E> filter(TraverseMethod method) {
                return filter; }
        };
    }
}
