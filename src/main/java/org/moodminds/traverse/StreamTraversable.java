package org.moodminds.traverse;

import org.moodminds.elemental.Association;
import org.moodminds.function.EvaluableThrowing1;
import org.moodminds.function.Executable1;
import org.moodminds.function.Executable1Throwing1;
import org.moodminds.function.Executable1Throwing2;
import org.moodminds.valuable.Variable;
import org.moodminds.valuable.Volatile;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.BaseStream;
import java.util.stream.StreamSupport;

import static java.util.Collections.synchronizedSet;
import static java.util.Objects.requireNonNull;
import static org.moodminds.function.Executable1.idle;
import static org.moodminds.sneaky.Sneak.sneak;
import static org.moodminds.valuable.Variable.var;
import static org.moodminds.valuable.Volatile.vol;

/**
 * Represents a {@link Traversable} implementation backed by a {@link BaseStream stream} of values.
 * <p>
 * Preserves the parallelism of the supplying {@link BaseStream stream}.
 *
 * @param <V> the type of item values
 * @param <E> the type of traversal exception
 */
public abstract class StreamTraversable<V, E extends Exception> implements Traversable<V, E> {

    /**
     * Construct the object.
     */
    protected StreamTraversable() {}

    @Override
    public <H1 extends Exception, H2 extends Exception> boolean traverse(TraverseMethod method, Traverse<V, E, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {

        Set<Throwable> thrown = null;

        try (BaseStream<V, ?> stream = method.isSequence() ? stream(method, ctx).sequential()
                : method.isParallel() ? stream(method, ctx).parallel() : stream(method, ctx)) {

            thrown = stream.isParallel() ? synchronizedSet(new HashSet<>()) : new HashSet<>();

            return stream.isParallel() ? parallel(stream.spliterator(), traverse, thrown::add)
                    : sequence(stream.spliterator(), traverse, thrown::add);

        } catch (Throwable caught) {
            Throwable cause = caught.getCause(); return sneak(thrown != null
                    && thrown.contains(cause) ? cause : caught);
        }
    }

    protected abstract BaseStream<V, ?> stream(TraverseMethod method, Association<?, ?, ?> ctx) throws E;

    private <H1 extends Exception, H2 extends Exception> boolean sequence(Spliterator<V> spliterator, Traverse<V, E, ? extends H1, ? extends H2> traverse,
                                                                          Executable1<? super Throwable> thrown) throws H1, H2 {

        Variable.Boolean traversed = var(false);

        traverse(spliterator(spliterator, split -> traversed.flg = traversed.flg || !traverse.complete(new Traverser<>() {

            @Override
            public <H extends Exception> boolean next(Executable1Throwing1<? super V, ? extends H> consumer) throws E, H {
                requireNonNull(consumer); try {
                    return !traversed.flg && split.tryAdvance(consumer(consumer));
                } catch (Throwable ex) { traversed.flg = true; return sneak(ex); }
            }

            @Override
            public <H extends Exception> void each(Executable1Throwing1<? super V, ? extends H> consumer) throws E, H {
                requireNonNull(consumer); if (!traversed.flg) try {
                    split.forEachRemaining(consumer(consumer));
                } catch (Throwable ex) { traversed.flg = true; sneak(ex); }
            }

        }), thrown), false);

        return !traversed.set(true);
    }

    private <H1 extends Exception, H2 extends Exception> boolean parallel(Spliterator<V> spliterator, Traverse<V, E, ? extends H1, ? extends H2> traverse,
                                                                          Executable1<? super Throwable> thrown) throws H1, H2 {

        Volatile.Boolean traversed = vol(false);

        traverse(spliterator(spliterator, split -> {
            if (!traversed.flg && !traverse.complete(new Traverser<>() {

                @Override
                public <H extends Exception> boolean next(Executable1Throwing1<? super V, ? extends H> consumer) throws E, H {
                    requireNonNull(consumer); try {
                        return !traversed.flg && split.tryAdvance(consumer(consumer));
                    } catch (Throwable ex) { return !traversed.set(true) ? sneak(ex) : false; }
                }

            })) traversed.flg = true;
        }, thrown), true);

        return !traversed.set(true);
    }

    private void traverse(Spliterator<V> spliterator, boolean parallel) {
        StreamSupport.stream(spliterator, parallel).forEach(idle());
    }

    private Consumer<V> consumer(Executable1Throwing1<? super V, ?> consumer) {
        return value -> {
            try { consumer.exec(value); }
            catch (Exception ex) { sneak(ex); }
        };
    }

    private <H1 extends Exception, H2 extends Exception> Spliterator<V> spliterator(Spliterator<V> spliterator, Executable1Throwing2<? super Spliterator<V>, ? extends H1, ? extends H2> traverse,
                                                                                    Executable1<? super Throwable> thrown) {
        return spliterator == null ? null : new Spliterator<V>() {
            @Override public boolean tryAdvance(Consumer<? super V> action) {
                throw new Error("Unexpected 'tryAdvance' call."); }
            @Override public void forEachRemaining(Consumer<? super V> action) {
                try { traverse.exec(spliterator); } catch (Exception ex) { thrown.exec(ex); sneak(ex); } }
            @Override public Spliterator<V> trySplit() {
                return spliterator(spliterator.trySplit(), traverse, thrown); }
            @Override public long estimateSize() {
                return spliterator.estimateSize(); }
            @Override public long getExactSizeIfKnown() {
                return spliterator.getExactSizeIfKnown(); }
            @Override public int characteristics() {
                return spliterator.characteristics(); }
            @Override public boolean hasCharacteristics(int characteristics) {
                return spliterator.hasCharacteristics(characteristics); }
            @Override public Comparator<? super V> getComparator() {
                return spliterator.getComparator(); }
        };
    }

    /**
     * Return a {@link Traversable} that provides item values using the given
     * {@link EvaluableThrowing1} supplier of {@link BaseStream stream} values.
     *
     * @param streamable the given {@link EvaluableThrowing1} supplier of {@link BaseStream stream} values
     * @param <V> the type of item value
     * @param <E> the type of traversal exception
     * @return a {@link Traversable} backed by a {@link BaseStream stream} of values
     * @throws NullPointerException if the given {@link EvaluableThrowing1} supplier is {@code null}
     */
    public static <V, E extends Exception> Traversable<V, E> stream(EvaluableThrowing1<? extends BaseStream<V, ?>, ? extends E> streamable) {
        requireNonNull(streamable); return new StreamTraversable<>() {
            @Override protected BaseStream<V, ?> stream(TraverseMethod method, Association<?, ?, ?> ctx) throws E {
                return streamable.eval(); }
        };
    }
}
