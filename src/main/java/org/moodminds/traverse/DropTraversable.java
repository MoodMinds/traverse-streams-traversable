package org.moodminds.traverse;

import org.moodminds.function.Testable1Throwing1;
import org.moodminds.valuable.Valuable;

import static java.util.Objects.requireNonNull;
import static org.moodminds.function.Testable1Throwing1.anyway;
import static org.moodminds.valuable.Variable.var;
import static org.moodminds.valuable.Volatile.vol;

/**
 * A {@link Traversable} implementation that excludes items upon encountering the first predicate match.
 *
 * @param <V> the type of item values
 * @param <E> the type of traversal exception
 */
public class DropTraversable<V, E extends Exception> extends FilterTraversable<V, E> {

    /**
     * The item values predicate holder field.
     */
    private final Testable1Throwing1<? super V, ? extends E> predicate;

    /**
     * Construct the object with the given {@link TraverseSupport}
     * source and {@link Testable1Throwing1} predicate.
     *
     * @param traversable the given source {@link TraverseSupport}
     * @param predicate the given {@link Testable1Throwing1} predicate
     * @throws NullPointerException if the source {@link TraverseSupport}
     * or the {@link Testable1Throwing1} predicate is {@code null}
     */
    protected DropTraversable(TraverseSupport<? extends V, ? extends E> traversable, Testable1Throwing1<? super V, ? extends E> predicate) {
        super(traversable); this.predicate = requireNonNull(predicate);
    }

    @Override
    protected Testable1Throwing1<? super V, ? extends E> filter(TraverseMethod method) throws E {

        Valuable<Testable1Throwing1<V, E>> drop = method.isSequence() ? var() : vol();

        drop.put(value -> {
            if (predicate.test(value)) return false;
            else { drop.put(anyway()); return true; }
        });

        return value -> drop.get().test(value);
    }


    /**
     * Return a {@link Traversable} implementation that excludes items
     * upon encountering the first {@link Testable1Throwing1} predicate match.
     *
     * @param traversable the given {@link TraverseSupport} source
     * @param predicate the given item values {@link Testable1Throwing1} predicate
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return a {@link Traversable} implementation that excludes items from
     * the given {@link TraverseSupport} source until matching the given {@link Testable1Throwing1} predicate
     * @throws NullPointerException if the source {@link TraverseSupport}
     * or the {@link Testable1Throwing1} predicate is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> drop(TraverseSupport<? extends V, ? extends E> traversable, Testable1Throwing1<? super V, ? extends E> predicate) {
        return new DropTraversable<>(traversable, predicate);
    }
}
