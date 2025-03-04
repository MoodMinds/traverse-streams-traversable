package org.moodminds.traverse;

import org.moodminds.elemental.Association;

import static java.util.Objects.requireNonNull;
import static org.moodminds.function.Executable1.idle;
import static org.moodminds.traverse.Traversable.each;

/**
 * A composite {@link Traversable} that first applies the traverse function to the items from the initial
 * {@link TraverseSupport} source and then performs a complete traversal of the second {@link TraverseSupport} source.
 *
 * @param <V> the type of item values
 * @param <E> the type of traversal exception
 */
public class BeforeTraversable<V, E extends Exception> implements Traversable<V, E> {

    /**
     * The first {@link TraverseSupport} source holder field.
     */
    private final TraverseSupport<? extends V, ? extends E> before;

    /**
     * The second {@link TraverseSupport} source holder field.
     */
    private final TraverseSupport<?, ? extends E> after;

    /**
     * Construct the object with the given {@link TraverseSupport} sources.
     *
     * @param before the given first {@link TraverseSupport} source
     * @param after the given second {@link TraverseSupport} source
     * @throws NullPointerException if any of the given {@link TraverseSupport} sources is {@code null}
     */
    protected BeforeTraversable(TraverseSupport<? extends V, ? extends E> before, TraverseSupport<?, ? extends E> after) {
        this.before = requireNonNull(before); this.after = requireNonNull(after);
    }

    @Override
    public <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {
        boolean complete = method.traverse(before, traverse, ctx); method.traverse(after, each(idle()), ctx); return complete;
    }

    /**
     * Return a composite {@link Traversable} that first applies the traverse function to the items from the initial
     * {@link TraverseSupport} source and then performs a complete traversal of the second {@link TraverseSupport} source.
     *
     * @param before the given first {@link TraverseSupport} source
     * @param after the given second {@link TraverseSupport} source
     * @return a composite {@link Traversable} that first applies the traverse function to the items from the initial
     * {@link TraverseSupport} source and then performs a complete traversal of the second {@link TraverseSupport} source
     * @throws NullPointerException if any of the given {@link TraverseSupport} sources is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> before(TraverseSupport<? extends V, ? extends E> before, TraverseSupport<?, ? extends E> after) {
        return new BeforeTraversable<>(before, after);
    }
}
