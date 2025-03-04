package org.moodminds.traverse;

import org.moodminds.elemental.Association;
import org.moodminds.function.Executable1Throwing1;
import org.moodminds.function.Executable1Throwing2;
import org.moodminds.function.Testable1Throwing1;
import org.moodminds.function.Testable1Throwing2;

import static java.util.Objects.requireNonNull;

/**
 * An implementation of the {@link Traversable} interface that executes the specified
 * {@link Executable1Throwing1} action on each element as items are consumed.
 *
 * @param <V> the type of item values
 * @param <E> the type of traversal exception
 */
public class PeekTraversable<V, E extends Exception> implements Traversable<V, E> {

    /**
     * The {@link TraverseSupport} source holder field.
     */
    private final TraverseSupport<? extends V, ? extends E> traversable;

    /**
     * The {@link Executable1Throwing1} action holder field.
     */
    private final Executable1Throwing1<? super V, ? extends E> peeker;

    /**
     * Construct the object with the given {@link TraverseSupport}
     * source and the {@link Executable1Throwing1} action.
     *
     * @param traversable the given {@link TraverseSupport} source
     * @param peeker the given {@link Executable1Throwing1} action
     * @throws NullPointerException if the specified {@link TraverseSupport}
     * source or {@link Executable1Throwing1} action is {@code null}
     */
    protected PeekTraversable(TraverseSupport<? extends V, ? extends E> traversable, Executable1Throwing1<? super V, ? extends E> peeker) {
        this.traversable = requireNonNull(traversable); this.peeker = requireNonNull(peeker);
    }

    @Override
    public <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {
        return method.<V, E, H1, H2>traverse(traversable, traverser -> traverse.exec(new Traverser<>() {

            @Override public <X extends Exception> boolean next(Executable1Throwing1<? super V, ? extends X> consumer) throws E, X {
                return traverser.next(peek(consumer, peeker)); }
            @Override public <X extends Exception> boolean next(long number, Executable1Throwing1<? super V, ? extends X> consumer) throws E, X {
                return traverser.next(number, peek(consumer, peeker)); }
            @Override public <X extends Exception> boolean some(Testable1Throwing1<? super V, ? extends X> consumer) throws E, X {
                return traverser.some(peek(consumer, peeker)); }
            @Override public <X extends Exception> void each(Executable1Throwing1<? super V, ? extends X> consumer) throws E, X {
                traverser.each(peek(consumer, peeker)); }

        }), ctx);
    }

    private <X extends Exception> Executable1Throwing2<V, E, X> peek(Executable1Throwing1<? super V, ? extends X> consumer, Executable1Throwing1<? super V, ? extends E> peeker) {
        requireNonNull(consumer); return value -> { peeker.exec(value); consumer.exec(value); };
    }

    private <X extends Exception> Testable1Throwing2<V, E, X> peek(Testable1Throwing1<? super V, ? extends X> consumer, Executable1Throwing1<? super V, ? extends E> peeker) {
        requireNonNull(consumer); return value -> { peeker.exec(value); return consumer.test(value); };
    }

    /**
     * Return a {@link Traversable} implementation that executes the specified {@link Executable1Throwing1}
     * action on each element as items are consumed from the given {@link TraverseSupport} source.
     *
     * @param traversable the given {@link TraverseSupport} source
     * @param peeker the given {@link Executable1Throwing1} action
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return a {@link Traversable} implementation that executes the specified {@link Executable1Throwing1}
     * action on each element as items are consumed from the given {@link TraverseSupport} source
     * @throws NullPointerException if the specified {@link TraverseSupport}
     * source or {@link Executable1Throwing1} action is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> peek(TraverseSupport<? extends V, ? extends E> traversable, Executable1Throwing1<? super V, ? extends E> peeker) {
        return new PeekTraversable<>(traversable, peeker);
    }
}
