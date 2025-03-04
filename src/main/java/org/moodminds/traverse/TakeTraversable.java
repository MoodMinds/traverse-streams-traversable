package org.moodminds.traverse;

import org.moodminds.elemental.Association;
import org.moodminds.function.Executable1Throwing1;
import org.moodminds.function.Testable1Throwing1;
import org.moodminds.function.TestableThrowing1;
import org.moodminds.valuable.Valuable;
import org.moodminds.valuable.Variable.Boolean;

import static java.util.Objects.requireNonNull;
import static org.moodminds.function.TestableThrowing1.anyway;
import static org.moodminds.valuable.Variable.var;
import static org.moodminds.valuable.Volatile.vol;

/**
 * Represents a {@link Traversable} implementation that immediately
 * terminates the entire traversal upon encountering the first predicate mismatch.
 *
 * @param <V> the type of item values
 * @param <E> the type of traversal exception
 */
public class TakeTraversable<V, E extends Exception> extends BreachTraversable<V, E> {

    /**
     * The before traversal {@link TestableThrowing1} predicate holder field.
     */
    private final TestableThrowing1<? extends E> predicate;

    /**
     * The item values {@link Testable1Throwing1} predicate holder field.
     */
    private final Testable1Throwing1<? super V, ? extends E> valuePredicate;

    /**
     * The first mismatch inclusion flag holder field.
     */
    private final boolean inclusive;

    /**
     * Construct the object with the given source {@link TraverseSupport}
     * predicates, and first mismatch inclusion flag.
     *
     * @param traversable the given source {@link TraverseSupport}
     * @param predicate the given before traversal predicate
     * @param valuePredicate the given item values predicate
     * @param inclusive the given first mismatch inclusion flag
     * @throws NullPointerException if the source {@link TraverseSupport} or any of the predicates is {@code null}
     */
    protected TakeTraversable(TraverseSupport<? extends V, ? extends E> traversable, TestableThrowing1<? extends E> predicate,
                              Testable1Throwing1<? super V, ? extends E> valuePredicate, boolean inclusive) {
        super(traversable);
        this.predicate = requireNonNull(predicate);
        this.valuePredicate = requireNonNull(valuePredicate);
        this.inclusive = inclusive;
    }

    @Override
    protected <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, TraverseSupport<? extends V, ? extends E> traversable, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {

        Valuable.Boolean taken = method.isSequence() ? var(!predicate.test()) : vol(!predicate.test());

        return method.<V, E, H1, H2>traverse(traversable, traverser -> traverse.exec(new Traverser<>() {

            @Override public <H extends Exception> boolean next(Executable1Throwing1<? super V, ? extends H> consumer) throws E, H {
                requireNonNull(consumer); Boolean fulfilled = var(false); return !taken.get() && traverser.<E, H>next(value -> {
                    if (valuePredicate.test(value) || !taken.set(true) && inclusive) {
                        fulfilled.flg = true; consumer.exec(value); }
                }) && fulfilled.flg; }

            @Override public <H extends Exception> boolean some(Testable1Throwing1<? super V, ? extends H> consumer) throws E, H {
                requireNonNull(consumer); Boolean fulfilled = var(false); return !taken.get() && traverser.<E, H>some(value -> {
                    if (valuePredicate.test(value))
                        return taken.get() ? (fulfilled.flg = false) : (fulfilled.flg = true) && consumer.test(value);
                    if (!taken.set(true) && inclusive) {
                        fulfilled.flg = !consumer.test(value); return false;
                    } return fulfilled.flg = false;
                }) && fulfilled.flg; }

        }), ctx);
    }

    /**
     * Return a {@link Traversable} implementation that emits items from
     * the given {@link TraverseSupport} source while matching the given predicate.
     *
     * @param traversable the given source {@link TraverseSupport}
     * @param predicate the given traversal predicate
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return a {@link Traversable} implementation that emits items from
     * the given {@link TraverseSupport} source while matching the given predicate
     * @throws NullPointerException if the source {@link TraverseSupport} or the predicate is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> take(TraverseSupport<? extends V, ? extends E> traversable, TestableThrowing1<? extends E> predicate) {
        return take(traversable, predicate, unused -> predicate.test(), false);
    }

    /**
     * Return a {@link Traversable} implementation that emits items from
     * the given {@link TraverseSupport} source while matching the given predicate.
     *
     * @param traversable the given source {@link TraverseSupport}
     * @param predicate the given item values predicate
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return a {@link Traversable} implementation that emits items from
     * the given {@link TraverseSupport} source while matching the given predicate
     * @throws NullPointerException if the source {@link TraverseSupport} or the predicate is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> take(TraverseSupport<? extends V, ? extends E> traversable, Testable1Throwing1<? super V, ? extends E> predicate) {
        return take(traversable, predicate, false);
    }

    /**
     * Return a {@link Traversable} implementation that emits items from the given {@link TraverseSupport}
     * source while matching the given predicate and considering the first mismatch inclusion flag.
     *
     * @param traversable the given source {@link TraverseSupport}
     * @param predicate the given item values predicate
     * @param inclusive the given first mismatch inclusion flag
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return a {@link Traversable} implementation that emits items from the given {@link TraverseSupport}
     * source while matching the given predicate and considering the first mismatch inclusion flag
     * @throws NullPointerException if the source {@link TraverseSupport} or the predicate is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> take(TraverseSupport<? extends V, ? extends E> traversable, Testable1Throwing1<? super V, ? extends E> predicate, boolean inclusive) {
        return take(traversable, anyway(), predicate, inclusive);
    }

    /**
     * Return a {@link Traversable} implementation that emits items from the given
     * {@link TraverseSupport} source while matching the given predicates.
     *
     * @param traversable the given source {@link TraverseSupport}
     * @param predicate the given before traversal predicate
     * @param valuePredicate the given item values predicate
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return a {@link Traversable} implementation that emits items from
     * the given {@link TraverseSupport} source while matching the given predicates
     * @throws NullPointerException if the source {@link TraverseSupport} or any of the predicates is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> take(TraverseSupport<? extends V, ? extends E> traversable, TestableThrowing1<? extends E> predicate, Testable1Throwing1<? super V, ? extends E> valuePredicate) {
        return take(traversable, predicate, valuePredicate, false);
    }

    /**
     * Return a {@link Traversable} implementation that emits items from the given {@link TraverseSupport}
     * source while matching the given predicates and considering the first mismatch inclusion flag.
     *
     * @param traversable the given source {@link TraverseSupport}
     * @param predicate the given before traversal predicate
     * @param valuePredicate the given item values predicate
     * @param inclusive the given first mismatch inclusion flag
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return a {@link Traversable} implementation that emits items from the given {@link TraverseSupport}
     * source while matching the given predicates and considering the first mismatch inclusion flag
     * @throws NullPointerException if the source {@link TraverseSupport} or any of the predicates is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> take(TraverseSupport<? extends V, ? extends E> traversable, TestableThrowing1<? extends E> predicate, Testable1Throwing1<? super V, ? extends E> valuePredicate, boolean inclusive) {
        return new TakeTraversable<>(traversable, predicate, valuePredicate, inclusive);
    }
}
