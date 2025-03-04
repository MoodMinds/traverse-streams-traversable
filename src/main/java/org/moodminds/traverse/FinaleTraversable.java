package org.moodminds.traverse;

import org.moodminds.elemental.Association;

import static java.util.Objects.requireNonNull;
import static org.moodminds.function.Executable1.idle;
import static org.moodminds.traverse.Traversable.each;
import static org.moodminds.traverse.Traversable.traversable;

/**
 * A composite {@link Traversable} that first applies the traverse function to the items from the initial
 * {@link TraverseSupport} source and finally performs a complete traversal of the second {@link TraverseSupport} source.
 *
 * @param <V> the type of item values
 * @param <E> the type of traversal exception
 */
public class FinaleTraversable<V, E extends Exception> implements Traversable<V, E> {

    /**
     * The {@link TraverseSupport} source holder field.
     */
    private final TraverseSupport<? extends V, ? extends E> traversable;

    /**
     * The final {@link TraverseSupport} source holder field.
     */
    private final Traversable<?, ? extends E> finale;

    /**
     * Construct the object with the given {@link TraverseSupport} sources.
     *
     * @param traversable the given first {@link TraverseSupport} source
     * @param finale the given final {@link TraverseSupport} source
     * @throws NullPointerException if any of the given {@link TraverseSupport} sources is {@code null}
     */
    public FinaleTraversable(TraverseSupport<? extends V, ? extends E> traversable, TraverseSupport<?, ? extends E> finale) {
        this.traversable = requireNonNull(traversable); this.finale = traversable(finale);
    }

    @Override
    public <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {
        try { return method.traverse(traversable, traverse, ctx); } finally { finale.traverse(each(idle())); }
    }


    /**
     * Return a composite {@link Traversable} that first applies the traverse function to the items from the initial
     * {@link TraverseSupport} source and finally performs a complete traversal of the second {@link TraverseSupport} source.
     *
     * @param traversable the given first {@link TraverseSupport} source
     * @param finale the given final {@link TraverseSupport} source
     * @return a composite {@link Traversable} that first applies the traverse function to the items from the initial
     * {@link TraverseSupport} source and finally performs a complete traversal of the second {@link TraverseSupport} source
     * @throws NullPointerException if any of the given {@link TraverseSupport} sources is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> finale(TraverseSupport<? extends V, ? extends E> traversable, Traversable<?, ? extends E> finale) {
        return new FinaleTraversable<>(traversable, finale);
    }
}
