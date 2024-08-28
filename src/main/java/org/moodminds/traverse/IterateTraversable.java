package org.moodminds.traverse;

import org.moodminds.elemental.Association;
import org.moodminds.function.Evaluable1Throwing1;
import org.moodminds.function.Testable1Throwing1;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.moodminds.sneaky.Sneak.sneak;

/**
 * An implementation of the {@link Traversable} interface that generates items
 * through iterative application of an {@link Evaluable1Throwing1} function to
 * an initial element while matching a {@link Testable1Throwing1} predicate.
 *
 * @param <V> the type of item value
 * @param <E> the type of traversal exception
 */
public class IterateTraversable<V, E extends Exception> extends StreamTraversable<V, E> {

    /**
     * The initial item holder field.
     */
    private final V init;

    /**
     * The {@link Evaluable1Throwing1} generating function holder field.
     */
    private final Evaluable1Throwing1<? super V, ? extends V, ? extends E> seed;

    /**
     * The item values {@link Testable1Throwing1} predicate holder field.
     */
    private final Testable1Throwing1<? super V, ? extends E> predicate;

    /**
     * Construct the object with the given initial item, generating {@link Evaluable1Throwing1}
     * function and item values {@link Testable1Throwing1} predicate.
     *
     * @param init the given initial item value
     * @param seed the given generating {@link Evaluable1Throwing1} function
     * @throws NullPointerException if the specified generating {@link Evaluable1Throwing1} function is {@code null}
     */
    protected IterateTraversable(V init, Evaluable1Throwing1<? super V, ? extends V, ? extends E> seed) {
        this.init = init; this.seed = requireNonNull(seed); this.predicate = null;
    }

    /**
     * Construct the object with the given initial item, generating {@link Evaluable1Throwing1}
     * function and item values {@link Testable1Throwing1} predicate.
     *
     * @param init the given initial item value
     * @param seed the given generating {@link Evaluable1Throwing1} function
     * @param predicate the given item values {@link Testable1Throwing1} predicate
     * @throws NullPointerException if the specified generating {@link Evaluable1Throwing1}
     * function or item values {@link Testable1Throwing1} predicate is {@code null}
     */
    protected IterateTraversable(V init, Evaluable1Throwing1<? super V, ? extends V, ? extends E> seed, Testable1Throwing1<? super V, ? extends E> predicate) {
        this.init = init; this.seed = requireNonNull(seed); this.predicate = requireNonNull(predicate);
    }

    @Override
    protected Stream<V> stream(TraverseMethod method, Association<?, ?, ?> ctx) throws E {
        return predicate == null ? Stream.iterate(init, seed(seed))
                : Stream.iterate(init, predicate(predicate), seed(seed));
    }

    private UnaryOperator<V> seed(Evaluable1Throwing1<? super V, ? extends V, ? extends E> seed) {
        return v -> {
            try { return seed.eval(v); }
            catch (Exception ex) { return sneak(ex); }
        };
    }

    private Predicate<V> predicate(Testable1Throwing1<? super V, ? extends E> predicate) {
        return v -> {
            try { return predicate.test(v); }
            catch (Exception ex) { return sneak(ex); }
        };
    }


    /**
     * Return an implementation of the {@link Traversable} interface that infinitely
     * generates items through iterative application of an {@link Evaluable1Throwing1}
     * function to an initial element.
     *
     * @param init the given initial item value
     * @param seed the given generating {@link Evaluable1Throwing1} function
     * @param <V> the type of item value
     * @param <E> the type of traversal exception
     * @return an implementation of the {@link Traversable} interface that generates items
     * through iterative application of an {@link Evaluable1Throwing1} function
     * @throws NullPointerException if the specified generating {@link Evaluable1Throwing1} function is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> iterate(V init, Evaluable1Throwing1<? super V, ? extends V, ? extends E> seed) {
        return new IterateTraversable<>(init, seed);
    }

    /**
     * Return an implementation of the {@link Traversable} interface that generates items
     * through iterative application of an {@link Evaluable1Throwing1} function to
     * an initial element while matching a {@link Testable1Throwing1} predicate
     *
     * @param init the given initial item value
     * @param seed the given generating {@link Evaluable1Throwing1} function
     * @param predicate the given item values {@link Testable1Throwing1} predicate
     * @param <V> the type of item value
     * @param <E> the type of traversal exception
     * @return an implementation of the {@link Traversable} interface that generates items
     * through iterative application of an {@link Evaluable1Throwing1} function
     * @throws NullPointerException if the specified generating {@link Evaluable1Throwing1}
     * function or item values {@link Testable1Throwing1} predicate is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> iterate(V init, Evaluable1Throwing1<? super V, ? extends V, ? extends E> seed, Testable1Throwing1<? super V, ? extends E> predicate) {
        return new IterateTraversable<>(init, seed, predicate);
    }
}
