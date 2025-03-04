package org.moodminds.traverse;

import org.moodminds.elemental.Association;
import org.moodminds.function.EvaluableThrowing1;
import org.moodminds.function.Executable1Throwing1;
import org.moodminds.valuable.Variable.Boolean;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.moodminds.sneaky.Sneak.sneak;
import static org.moodminds.valuable.Variable.var;

/**
 * An implementation of the {@link Traversable} interface that throws
 * an exception (specified by a {@link EvaluableThrowing1} supplier) on traversal.
 *
 * @param <V> the type of item values
 * @param <E> the type of traversal exception
 */
public class ExceptTraversable<V, E extends Exception> extends StreamTraversable<V, E> {

    /**
     * The {@link EvaluableThrowing1} exception supplier holder field.
     */
    private final EvaluableThrowing1<? extends E, ? extends E> exceptable;

    /**
     * Construct the object with the specified {@link EvaluableThrowing1} exception supplier.
     *
     * @param exceptable the specified {@link EvaluableThrowing1} exception supplier
     * @throws NullPointerException if the given {@link EvaluableThrowing1} exception supplier is {@code null}
     */
    protected ExceptTraversable(EvaluableThrowing1<? extends E, ? extends E> exceptable) {
        this.exceptable = requireNonNull(exceptable);
    }

    @Override
    public <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {

        Boolean thrown = var(false);

        boolean complete = super.<H1, H2>traverse(method, traverser -> traverse.exec(new Traverser<V, E>() {

            @Override public <H extends Exception> boolean next(Executable1Throwing1<? super V, ? extends H> consumer) throws E, H {
                requireNonNull(consumer); try {
                    return traverser.next(consumer); }
                catch (Throwable e) {
                    thrown.flg = true; return sneak(e); } }

        }), ctx);

        if (!thrown.flg)
            throw exceptable.eval();
        else return complete;
    }

    @Override
    protected Stream<V> stream(TraverseMethod method, Association<?, ?, ?> ctx) throws E {
        return Stream.of(exceptable).map(e -> {
            try { throw e.eval(); }
            catch (Exception ex) { return sneak(ex); }
        });
    }


    /**
     * Return a {@link Traversable} implementation that throws an exception
     * (specified by a {@link EvaluableThrowing1} supplier) on traversal.
     *
     * @param exceptable the specified {@link EvaluableThrowing1} exception supplier
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return a {@link Traversable} implementation that throws an exception
     * (specified by a {@link EvaluableThrowing1} supplier) on traversal
     * @throws NullPointerException if the given {@link EvaluableThrowing1} exception supplier is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> except(EvaluableThrowing1<? extends E, ? extends E> exceptable) {
        return new ExceptTraversable<>(exceptable);
    }
}
