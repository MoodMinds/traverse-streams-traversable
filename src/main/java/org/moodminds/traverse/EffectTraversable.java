package org.moodminds.traverse;

import org.moodminds.elemental.Association;
import org.moodminds.function.ExecutableThrowing1;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.moodminds.function.Testable1.noways;
import static org.moodminds.sneaky.Sneak.sneak;

/**
 * An implementation of the {@link Traversable} interface
 * that performs an {@link ExecutableThrowing1} on traversal.
 *
 * @param <V> the type of item values
 * @param <E> the type of traversal exception
 */
public class EffectTraversable<V, E extends Exception> extends StreamTraversable<V, E> {

    /**
     * The {@link ExecutableThrowing1} execution holder field.
     */
    private final ExecutableThrowing1<? extends E> executable;

    /**
     * Construct the object with the specified {@link ExecutableThrowing1} execution.
     *
     * @param executable the specified {@link ExecutableThrowing1} execution
     * @throws NullPointerException if the given {@link ExecutableThrowing1} execution is {@code null}
     */
    protected EffectTraversable(ExecutableThrowing1<? extends E> executable) {
        this.executable = requireNonNull(executable);
    }

    @Override
    protected Stream<V> stream(TraverseMethod method, Association<?, ?, ?> ctx) throws E {
        return Stream.of(executable).<V>map(e -> {
            try { e.exec(); return null; }
            catch (Exception ex) { return sneak(ex); }
        }).filter(noways());
    }


    /**
     * Return a {@link Traversable} implementation that performs an {@link ExecutableThrowing1} on traversal.
     *
     * @param executable the specified {@link ExecutableThrowing1} execution
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return a {@link Traversable} implementation that performs an {@link ExecutableThrowing1} on traversal
     * @throws NullPointerException if the given {@link ExecutableThrowing1} execution is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> effect(ExecutableThrowing1<? extends E> executable) {
        return new EffectTraversable<>(executable);
    }
}
