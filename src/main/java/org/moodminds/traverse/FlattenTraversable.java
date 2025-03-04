package org.moodminds.traverse;

import org.moodminds.elemental.Association;
import org.moodminds.function.Executable1Throwing3;
import org.moodminds.valuable.Variable.Boolean;
import org.moodminds.function.Evaluable1Throwing1;
import org.moodminds.function.Executable1Throwing1;
import org.moodminds.function.Testable1Throwing1;

import static java.util.Objects.requireNonNull;
import static org.moodminds.function.Executable1Throwing3.idle;
import static org.moodminds.traverse.Traversable.traverser;
import static org.moodminds.sneaky.Sneak.sneak;
import static org.moodminds.valuable.Variable.var;

/**
 * An implementation of the {@link Traversable} interface that transforms
 * source items using an {@link Evaluable1Throwing1} flattening function.
 *
 * @param <S> the type of source item values
 * @param <V> the type of target item values
 * @param <E> the type of traversal exception
 */
public class FlattenTraversable<S, V, E extends Exception> implements Traversable<V, E> {

    /**
     * The {@link TraverseSupport} source holder field.
     */
    private final TraverseSupport<? extends S, ? extends E> traversable;

    /**
     * The {@link Evaluable1Throwing1} flattening function holder field.
     */
    private final Evaluable1Throwing1<? super S, ? extends TraverseSupport<? extends V, ? extends E>, ? extends E> flattener;

    /**
     * Construct the object with the given {@link TraverseSupport} source
     * and the {@link Evaluable1Throwing1} flattening function.
     *
     * @param traversable the given {@link TraverseSupport} source
     * @param mapper the given {@link Evaluable1Throwing1} flattening function
     * @throws NullPointerException if the specified {@link TraverseSupport}
     * source or {@link Evaluable1Throwing1} flattening function is {@code null}
     */
    protected FlattenTraversable(TraverseSupport<? extends S, ? extends E> traversable, Evaluable1Throwing1<? super S, ? extends TraverseSupport<? extends V, ? extends E>, ? extends E> flattener) {
        this.traversable = requireNonNull(traversable); this.flattener = requireNonNull(flattener);
    }

    @Override
    public <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {

        return method.<S, E, E, H1, H2>traverse(traversable, traverse.complete(traverser()) ? method.isSequence()

                ? source -> source.<E, H1, H2>some(value -> method.traverse(flattener.eval(value), traverse, ctx))

                : new Executable1Throwing3<>() {

                    volatile boolean breach = false;

                    @Override
                    public void exec(TraverseSupport.Traverser<? extends S, ? extends E> source) throws E, H1, H2 {
                        try {
                            source.<E, H1, H2>some(value -> method.<V, E, H1, H2>traverse(flattener.eval(value), traverser -> traverse.exec(new Traverser<>() {

                                @Override public <H extends Exception> boolean next(Executable1Throwing1<? super V, ? extends H> consumer) throws E, H {
                                    requireNonNull(consumer); return !breach && traverser.next(consumer); }

                                @Override public <H extends Exception> boolean some(Testable1Throwing1<? super V, ? extends H> consumer) throws E, H {
                                    requireNonNull(consumer); Boolean next = var(false); return !breach && traverser.some(value ->
                                            (next.flg = false) || !breach && (next.flg = true) && consumer.test(value)) && next.flg; }

                            }), ctx) || !(breach = true));
                        } catch (Throwable ex) { breach = true; sneak(ex); }
                    }

                } : idle(), ctx);
    }


    /**
     * Return an implementation of the {@link Traversable} interface that transforms
     * source items using an {@link Evaluable1Throwing1} flattening function.
     *
     * @param traversable the specified {@link TraverseSupport} source
     * @param flattener the specified {@link Evaluable1Throwing1} flattening function
     * @return an implementation of the {@link Traversable} interface that transforms
     * source items using an {@link Evaluable1Throwing1} flattening function
     * @param <S> the type of source item values
     * @param <V> the type of target item values
     * @param <E> the type of traversal exception
     * @throws NullPointerException if the specified {@link TraverseSupport}
     * source or {@link Evaluable1Throwing1} flattening function is {@code null}
     */
    public static <S, V, E extends Exception> Traversable<V, E> flatten(TraverseSupport<? extends S, ? extends E> traversable,
                                                                        Evaluable1Throwing1<? super S, ? extends TraverseSupport<? extends V, ? extends E>, ? extends E> flattener) {
        return new FlattenTraversable<>(traversable, flattener);
    }
}
