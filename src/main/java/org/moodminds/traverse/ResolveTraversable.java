package org.moodminds.traverse;

import org.moodminds.elemental.Association;

import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.moodminds.sneaky.Sneak.sneak;
import static org.moodminds.traverse.Traversable.reduce;

/**
 * An implementation of the {@link Traversable} interface that resolves
 * the {@link TraverseSupport} source using the provided {@link Resolver} resolver.
 *
 * @param <V> the type of item values
 * @param <E> the type of traversal exception
 * @param <R> the type of the resolution result
 */
public class ResolveTraversable<V, E extends Exception, R> extends StreamTraversable<R, E> {

    /**
     * The {@link TraverseSupport} source holder field.
     */
    private final TraverseSupport<? extends V, ? extends E> traversable;

    /**
     * The {@link Resolver} resolver holder field.
     */
    private final Resolver<? super V, E, ? extends R, ? extends E> resolver;

    /**
     * Construct the object with the given {@link TraverseSupport} source and {@link Resolver} resolver.
     *
     * @param traversable the given {@link TraverseSupport} source
     * @param resolver the given {@link Resolver} resolver
     * @throws NullPointerException if the {@link TraverseSupport} source
     * or {@link Resolver} resolver is {@code null}
     */
    public ResolveTraversable(TraverseSupport<? extends V, ? extends E> traversable, Resolver<? super V, E, ? extends R, ? extends E> resolver) {
        this.traversable = requireNonNull(traversable); this.resolver = requireNonNull(resolver);
    }

    @Override
    protected Stream<R> stream(TraverseMethod method, Association<?, ?, ?> ctx) throws E {
        return Stream.of(traversable).map(traversable -> {
            try { return resolver.resolve(method, traversable, ctx); }
            catch (Exception ex) { return sneak(ex); }
        });
    }

    /**
     * Return a {@link Traversable} implementation that resolves the given
     * {@link TraverseSupport} source using the provided {@link Resolver} resolver.
     *
     * @param traversable the given {@link TraverseSupport} source
     * @param resolver the provided {@link Resolver} resolver
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @param <R> the type of the resolution result
     * @return a {@link Traversable} implementation that resolves the given
     * {@link TraverseSupport} source using the provided {@link Resolver} resolver
     * @throws NullPointerException if the {@link TraverseSupport} source
     * or {@link Resolver} resolver is {@code null}
     */
    public static <V, E extends Exception, R> Traversable<R, E> resolve(TraverseSupport<? extends V, ? extends E> traversable,
                                                                        Resolver<? super V, E, ? extends R, ? extends E> resolver) {
        return new ResolveTraversable<>(traversable, resolver);
    }

    /**
     * Return a {@link Traversable} implementation that resolves the given
     * {@link TraverseSupport} source using the provided {@link Collector} collector.
     *
     * @param traversable the given {@link TraverseSupport} source
     * @param collector the provided {@link Collector} collector
     * @param <V> the type of item values
     * @param <E> the type of traversal exception
     * @param <R> the type of the resolution result
     * @return a {@link Traversable} implementation that resolves the given
     * {@link TraverseSupport} source using the provided {@link Collector} collector
     * @throws NullPointerException if the {@link TraverseSupport} source
     * or {@link Collector} collector is {@code null}
     */
    public static <V, E extends Exception, R> Traversable<R, E> resolve(TraverseSupport<? extends V, ? extends E> traversable,
                                                                        Collector<? super V, ?, ? extends R> collector) {
        return resolve(traversable, reduce(collector));
    }
}
