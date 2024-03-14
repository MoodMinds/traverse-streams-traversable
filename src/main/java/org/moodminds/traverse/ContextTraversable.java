package org.moodminds.traverse;

import org.moodminds.elemental.Association;
import org.moodminds.valuable.Variable;

import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;
import static org.moodminds.valuable.Variable.var;

/**
 * An implementation of the {@link Traversable} that modifies the {@link Association}
 * context before traversing the {@link TraverseSupport} source.
 *
 * @param <V> the type of item values
 * @param <E> the type of traversal exception
 */
public class ContextTraversable<V, E extends Exception> implements Traversable<V, E> {

    /**
     * The {@link TraverseSupport} source holder field.
     */
    private final TraverseSupport<? extends V, ? extends E> traversable;

    /**
     * The {@link Association} traversal context modification function holder field.
     */
    private final BiConsumer<Association<Object, Object, ?>, Context> contextWrite;

    /**
     * Construct the object with the given {@link TraverseSupport} source
     * and the {@link Association} traversal context modification function.
     *
     * @param traversable the given {@link TraverseSupport} source
     * @param contextWrite the given {@link Association} traversal context modification function
     * @throws NullPointerException if the given {@link TraverseSupport} source
     * or the {@link Association} traversal context modification function is {@code null}
     */
    protected ContextTraversable(TraverseSupport<? extends V, ? extends E> traversable, BiConsumer<Association<Object, Object, ?>, Context> contextWrite) {
        this.traversable = requireNonNull(traversable); this.contextWrite = requireNonNull(contextWrite);
    }

    @Override
    public <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<Object, Object, ?> ctx) throws E, H1, H2 {

        Variable<Association<Object, Object, ?>> context = var(ctx); contextWrite.accept(ctx, new Context() {

            @Override public Context set(Object key, Object value) {
                context.val = Traversable.set(context.val, key, value); return this; }

            @Override public Context delete(Object key) {
                context.val = Traversable.delete(context.val, key); return this; }

        }); return method.traverse(traversable, traverse, context.val);
    }


    /**
     * The {@link Association} context modification interface.
     */
    public interface Context {

        /**
         * Set the given variable value to the context.
         *
         * @param key the given key of the value
         * @param value the given value to set
         * @return the self-instance
         * @throws NullPointerException if the given key or value is {@code null}
         */
        Context set(Object key, Object value);

        /**
         * Remove the given variable from the context.
         *
         * @param key the given key of the value
         * @return the self-instance
         * @throws NullPointerException if the given key is {@code null}
         */
        Context delete(Object key);
    }


    /**
     * Return an implementation of the {@link Traversable} that modifies
     * the {@link Association} context before traversing the {@link TraverseSupport} source.
     *
     * @param traversable the given {@link TraverseSupport} source
     * @param contextWrite the given {@link Association} traversal context modification function
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @return an implementation of the {@link Traversable} that modifies
     * the {@link Association} context before traversing the {@link TraverseSupport} source
     * @throws NullPointerException if the given {@link TraverseSupport} source
     * or the {@link Association} traversal context modification function is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> context(TraverseSupport<? extends V, ? extends E> traversable, BiConsumer<Association<Object, Object, ?>, Context> contextWrite) {
        return new ContextTraversable<>(traversable, contextWrite);
    }
}
