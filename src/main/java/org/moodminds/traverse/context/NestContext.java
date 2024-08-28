package org.moodminds.traverse.context;

import org.moodminds.elemental.AbstractAssociation;
import org.moodminds.elemental.Association;
import org.moodminds.elemental.Container;
import org.moodminds.elemental.EmptyIterator;
import org.moodminds.elemental.KeyValue;
import org.moodminds.elemental.RandomMatch;
import org.moodminds.elemental.SingleIterator;
import org.moodminds.sneaky.Cast;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.moodminds.elemental.Pair.pair;
import static org.moodminds.sneaky.Cast.cast;

/**
 * Implementation of the {@link Association} context, incorporating additional changes (delta) to the parent context.
 *
 * <p>This implementation utilizes an internal {@link Delta} to manage the addition or removal of key-value pairs
 * within the context.
 */
public class NestContext extends AbstractAssociation<Object, Object, KeyValue<Object, Object>>
        implements Serializable {

    private static final long serialVersionUID = 2128953923580688463L;

    public static final int NEST_MAX_SIZE = 8;

    /**
     * A {@link Delta} holder field.
     */
    private final Delta delta;

    /**
     * A context size holder field.
     */
    private Integer size;

    /**
     * Construct the object.
     */
    public NestContext() {
        this.delta = null;
    }

    /**
     * Construct the object with the given parent context and adding key and value.
     *
     * @param context the given parent context
     * @param key the given adding key
     * @param value the given adding value
     */
    public NestContext(Association<?, ?, ?> context, Object key, Object value) {
        this.delta = new Add(context, key, value);
    }

    /**
     * Construct the object with the given parent context and removal key.
     *
     * @param context the given parent context
     * @param removeKey the given remove key
     */
    public NestContext(Association<?, ?, ?> context, Object removeKey) {
        this.delta = new Remove(context, removeKey);
    }


    /**
     * A context change delta handling the addition or removal operation.
     */
    private static abstract class Delta implements Serializable {

        private static final long serialVersionUID = 2634109493516195575L;

        protected final Association<?, ?, ?> parent;

        protected final Object key;

        Delta(Association<?, ?, ?> parent, Object key) {
            this.parent = requireNonNull(parent); this.key = requireNonNull(key);
        }

        abstract int size();

        abstract <R> R get(Object key);

        abstract boolean contains(Object key);

        Stream<KeyValue<Object, Object>> stream() {
            return stream(parent.stream().filter(entry -> !this.key.equals(entry.getKey()))).map(Cast::cast);
        }

        abstract Stream<? extends KeyValue<?, ?>> stream(Stream<? extends KeyValue<?, ?>> stream);
    }

    /**
     * A context change delta handling the addition operation.
     */
    private static class Add extends Delta {

        private static final long serialVersionUID = -239133332967152802L;

        protected final Object value;

        Add(Association<?, ?, ?> parent, Object key, Object value) {
            super(parent, key); this.value = requireNonNull(value);
        }

        @Override int size() {
            return parent.size() + (parent.containsKey(key) ? 0 : 1); }
        @Override <R> R get(Object key) {
            return cast(this.key.equals(key) ? value : parent.get(key)); }
        @Override boolean contains(Object key) {
            return this.key.equals(key) || parent.containsKey(key); }
        @Override Stream<? extends KeyValue<?, ?>> stream(Stream<? extends KeyValue<?, ?>> stream) {
            return concat(stream, of(pair(key, value))); }
    }

    /**
     * A context change delta handling the removal operation.
     */
    private static class Remove extends Delta {

        private static final long serialVersionUID = 2039980157143939910L;

        Remove(Association<?, ?, ?> parent, Object key) {
            super(parent, key);
        }

        @Override int size() {
            return parent.size() + (parent.containsKey(key) ? -1 : 0); }
        @Override <R> R get(Object key) {
            if (this.key.equals(key))
                throw new NoSuchElementException("No value in context for key: " + key);
            return cast(parent.get(key)); }
        @Override boolean contains(Object key) {
            return !this.key.equals(key) && parent.containsKey(key); }
        @Override Stream<? extends KeyValue<?, ?>> stream(Stream<? extends KeyValue<?, ?>> stream) {
            return stream; }
    }

    /**
     * {@inheritDoc}
     *
     * @param key {@inheritDoc}
     * @return the non-null value to which the specified key is associated
     * @throws ClassCastException     {@inheritDoc}
     * @throws NullPointerException   if the specified key is {@code null}
     * @throws NoSuchElementException {@inheritDoc}
     * @param <R> {@inheritDoc}
     */
    @Override
    public <R> R get(Object key) {
        if (delta != null)
            return delta.get(key);
        throw new NoSuchElementException("No value in context for key: " + key);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public int size() {
        return size == null ? (size = (delta != null ? delta.size() : 0)) : size;
    }

    /**
     * {@inheritDoc}
     *
     * @param key {@inheritDoc}
     * @return {@inheritDoc}
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public boolean containsKey(Object key) {
        return delta != null && delta.contains(key);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Container<Object> keys() {
        return new KeysContainer();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Container<Object> values() {
        return new ValuesContainer();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Iterator<KeyValue<Object, Object>> iterator() {
        return stream().iterator();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Spliterator<KeyValue<Object, Object>> spliterator() {
        return stream().spliterator();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Stream<KeyValue<Object, Object>> stream() {
        return ofNullable(delta).map(Delta::stream).orElseGet(Stream::empty);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Stream<KeyValue<Object, Object>> parallelStream() {
        return stream().parallel();
    }

    /**
     * {@inheritDoc}
     *
     * @param key {@inheritDoc}
     * @param value {@inheritDoc}
     * @param hasEntry {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected Iterator<KeyValue<Object, Object>> iterator(Object key, Object value, boolean hasEntry) {
        return hasEntry ? SingleIterator.iterator(pair(key, value)) : EmptyIterator.iterator();
    }


    /**
     * Nest Context keys Container.
     */
    protected class KeysContainer extends AbstractKeysContainer {

        @Override public Spliterator<Object> spliterator() {
            return NestContext.this.stream().parallel().map(KeyValue::getKey).spliterator(); }
    }

    /**
     * Nest Context values Container.
     */
    protected class ValuesContainer extends AbstractValuesContainer {

        @Override public Spliterator<Object> spliterator() {
            return NestContext.this.stream().parallel().map(KeyValue::getValue).spliterator(); }
    }


    /**
     * {@link RandomMatch} {@link NestContext} implementation.
     */
    protected static class RandomMatchContext extends NestContext implements RandomMatch {

        private static final long serialVersionUID = 488653308872268132L;

        /**
         * Construct the object.
         */
        public RandomMatchContext() {}

        /**
         * Construct the object with the given parent context and adding key and value.
         *
         * @param context the given parent context
         * @param key the given adding key
         * @param value the given adding value
         */
        public RandomMatchContext(Association<?, ?, ?> context, Object key, Object value) {
            super(random(context), key, value);
        }

        /**
         * Construct the object with the given parent context and removal key.
         *
         * @param context the given parent context
         * @param removeKey the given remove key
         */
        public RandomMatchContext(Association<?, ?, ?> context, Object removeKey) {
            super(random(context), removeKey);
        }

        @Override public Container<Object> keys() {
            return new KeysContainer(); }

        /**
         * {@link RandomMatch} {@link KeysContainer} implementation.
         */
        protected class KeysContainer extends NestContext.KeysContainer implements RandomMatch {}

        /**
         * Assert the specified {@link Association} context is instance of {@link RandomMatch} and return.
         *
         * @param context the specified {@link Association} context
         * @return the specified {@link Association} context if it is an instance of {@link RandomMatch}
         */
        private static Association<?, ?, ?> random(Association<?, ?, ?> context) {
            if (context instanceof RandomMatch) return context;
            throw new IllegalArgumentException("The specified context is not an instance of RandomMatch interface.");
        }
    }

    /**
     * Return new empty {@link NestContext}.
     *
     * @return new empty {@link NestContext}
     */
    public static NestContext context() {
        return new RandomMatchContext();
    }

    /**
     * Return new {@link NestContext} by the given parent context and adding key and value.
     *
     * @param context the given parent context
     * @param key the given adding key
     * @param value the given adding value
     * @return new {@link NestContext} by the given parent context and adding key and value
     */
    public static NestContext context(Association<?, ?, ?> context, Object key, Object value) {
        return context instanceof RandomMatch ? new RandomMatchContext(context, key, value)
                : new NestContext(context, key, value);
    }

    /**
     * Return new {@link NestContext} by the given parent context and removal key.
     *
     * @param context the given parent context
     * @param removeKey the given remove key
     * @return new {@link NestContext} by the given parent context and removal key
     */
    public static NestContext context(Association<?, ?, ?> context, Object removeKey) {
        return context instanceof RandomMatch ? new RandomMatchContext(context, removeKey)
                : new NestContext(context, removeKey);
    }
}
