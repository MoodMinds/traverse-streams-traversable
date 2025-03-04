package org.moodminds.traverse;

import org.moodminds.elemental.Association;
import org.moodminds.function.Evaluable1Throwing1;
import org.moodminds.function.Executable1Throwing1;
import org.moodminds.function.Executable1Throwing2;
import org.moodminds.function.Testable1Throwing1;
import org.moodminds.function.Testable1Throwing2;

import static java.util.Objects.requireNonNull;

/**
 * An implementation of the {@link Traversable} interface that transforms
 * source items using an {@link Evaluable1Throwing1} mapping function.
 *
 * @param <S> the type of source item values
 * @param <V> the type of target item values
 * @param <E> the type of traversal exception
 */
public class MapTraversable<S, V, E extends Exception> implements Traversable<V, E> {

    /**
     * The {@link TraverseSupport} source holder field.
     */
    private final TraverseSupport<? extends S, ? extends E> traversable;

    /**
     * The {@link Evaluable1Throwing1} mapping function holder field.
     */
    private final Evaluable1Throwing1<? super S, ? extends V, ? extends E> mapper;

    /**
     * Construct the object with the given {@link TraverseSupport} source
     * and the {@link Evaluable1Throwing1} mapping function.
     *
     * @param traversable the given {@link TraverseSupport} source
     * @param mapper the given {@link Evaluable1Throwing1} mapping function
     * @throws NullPointerException if the specified {@link TraverseSupport}
     * source or {@link Evaluable1Throwing1} mapping function is {@code null}
     */
    protected MapTraversable(TraverseSupport<? extends S, ? extends E> traversable, Evaluable1Throwing1<? super S, ? extends V, ? extends E> mapper) {
        this.traversable = requireNonNull(traversable); this.mapper = requireNonNull(mapper);
    }

    @Override
    public <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {
        return method.<S, E, H1, H2>traverse(traversable, traverser -> traverse.exec(new Traverser<>() {

            @Override public <X extends Exception> boolean next(Executable1Throwing1<? super V, ? extends X> consumer) throws E, X {
                return traverser.next(map(consumer, mapper)); }
            @Override public <X extends Exception> boolean next(long number, Executable1Throwing1<? super V, ? extends X> consumer) throws E, X {
                return traverser.next(number, map(consumer, mapper)); }
            @Override public <X extends Exception> boolean some(Testable1Throwing1<? super V, ? extends X> consumer) throws E, X {
                return traverser.some(map(consumer, mapper)); }
            @Override public <X extends Exception> void each(Executable1Throwing1<? super V, ? extends X> consumer) throws E, X {
                traverser.each(map(consumer, mapper)); }

        }), ctx);
    }

    private <X extends Exception> Executable1Throwing2<S, E, X> map(Executable1Throwing1<? super V, ? extends X> consumer, Evaluable1Throwing1<? super S, ? extends V, ? extends E> mapper) {
        requireNonNull(consumer); return value -> consumer.exec(mapper.eval(value));
    }

    private <X extends Exception> Testable1Throwing2<S, E, X> map(Testable1Throwing1<? super V, ? extends X> consumer, Evaluable1Throwing1<? super S, ? extends V, ? extends E> mapper) {
        requireNonNull(consumer); return value -> consumer.test(mapper.eval(value));
    }


    /**
     * Return an implementation of the {@link Traversable} interface that transforms
     * source items using an {@link Evaluable1Throwing1} mapping function.
     *
     * @param traversable the specified {@link TraverseSupport} source
     * @param mapper the specified {@link Evaluable1Throwing1} mapping function
     * @param <S> the type of source item values
     * @param <V> the type of target item values
     * @param <E> the type of traversal exception
     * @return an implementation of the {@link Traversable} interface that transforms
     * source items using an {@link Evaluable1Throwing1} mapping function
     * @throws NullPointerException if the specified {@link TraverseSupport}
     * source or {@link Evaluable1Throwing1} mapping function is {@code null}
     */
    public static <S, V, E extends Exception> Traversable<V, E> map(TraverseSupport<? extends S, E> traversable, Evaluable1Throwing1<? super S, ? extends V, ? extends E> mapper) {
        return new MapTraversable<>(traversable, mapper);
    }
}
