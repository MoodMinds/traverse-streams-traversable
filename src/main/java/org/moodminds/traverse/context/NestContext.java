package org.moodminds.traverse.context;

import org.moodminds.elemental.AbstractAssociation;
import org.moodminds.elemental.AbstractKeyValue;
import org.moodminds.elemental.Association;
import org.moodminds.elemental.Container;
import org.moodminds.elemental.KeyValue;
import org.moodminds.elemental.OptionalIterator;
import org.moodminds.elemental.RandomMatch;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.moodminds.sneaky.Cast.cast;

/**
 * Implementation of the {@link Association} context, incorporating additional changes (delta) to the parent context.
 *
 * <p>This implementation utilizes an internal {@link Delta} to manage the addition or removal of key-value pairs
 * within the context.
 */
public class NestContext extends AbstractAssociation<Object, Object, KeyValue<?, ?>> implements Serializable {

    private static final long serialVersionUID = 8505132574336115737L;

    /**
     * A context size holder field.
     */
    private Integer size;

    /**
     * A {@link Delta} holder field.
     */
    private final Delta delta;

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
        this.delta = new Addition(context, key, value);
    }

    /**
     * Construct the object with the given parent context and removal key.
     *
     * @param context the given parent context
     * @param removeKey the given remove key
     */
    public NestContext(Association<?, ?, ?> context, Object removeKey) {
        this.delta = new Deletion(context, removeKey);
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
    public Iterator<KeyValue<?, ?>> iterator() {
        return stream().iterator();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Spliterator<KeyValue<?, ?>> spliterator() {
        return stream().spliterator();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Stream<KeyValue<?, ?>> stream() {
        return ofNullable(delta).map(Delta::stream).orElseGet(Stream::empty);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Stream<KeyValue<?, ?>> parallelStream() {
        return stream().parallel();
    }

    /**
     * {@inheritDoc}
     *
     * @param key {@inheritDoc}
     * @param value {@inheritDoc}
     * @param present {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected Iterator<KeyValue<?, ?>> iterator(Object key, Object value, boolean present) {
        return OptionalIterator.iterator(() -> new AbstractKeyValue<>() {
            @Override public Object getKey() { return key; }
            @Override public Object getValue() { return value; }
        }, present);
    }

    /**
     * Nest Context keys Container.
     */
    protected class KeysContainer extends AbstractKeysContainer {

        @Override public Spliterator<Object> spliterator() {
            return NestContext.this.stream().parallel().<Object>map(KeyValue::getKey).spliterator(); }
    }

    /**
     * Nest Context values Container.
     */
    protected class ValuesContainer extends AbstractValuesContainer {

        @Override public Spliterator<Object> spliterator() {
            return NestContext.this.stream().parallel().<Object>map(KeyValue::getValue).spliterator(); }
    }

    /**
     * A context change delta handling the addition or removal operation.
     */
    private interface Delta {

        int size();

        <R> R get(Object key);

        boolean contains(Object key);

        Stream<KeyValue<?, ?>> stream();
    }

    /**
     * A context change delta handling the addition operation.
     */
    private static class Addition implements Delta, Serializable {

        private static final long serialVersionUID = -914920042822272603L;

        protected final Association<?, ?, ?> basis;

        protected final Object key, value;

        private Addition(Association<?, ?, ?> basis, Object key, Object value) {
            this.basis = basis; this.key = key; this.value = value; }

        @Override public int size() {
            return basis.size() + (basis.containsKey(key) ? 0 : 1); }
        @Override public <R> R get(Object key) {
            return cast(this.key.equals(key) ? value : basis.get(key)); }
        @Override public boolean contains(Object key) {
            return this.key.equals(key) || basis.containsKey(key); }
        
        @Override public Stream<KeyValue<?, ?>> stream() {
            return concat(basis.stream(), of(new AbstractKeyValue<Object, Object>() {
                @Override public Object getKey() { return key; }
                @Override public Object getValue() { return value; }
            })); }
    }


    /**
     * A context change delta handling the deletion operation.
     */
    private static class Deletion implements Delta, Serializable {

        private static final long serialVersionUID = -8952133755780171133L;

        protected final Association<?, ?, ?> basis;

        protected final Object key;

        private Deletion(Association<?, ?, ?> basis, Object key) {
            this.basis = basis; this.key = key; }

        @Override public int size() {
            return basis.size() + (basis.containsKey(key) ? -1 : 0); }
        @Override public <R> R get(Object key) {
            if (this.key.equals(key))
                throw new NoSuchElementException("No value in context for key: " + key);
            return cast(basis.get(key)); }
        @Override public boolean contains(Object key) {
            return !this.key.equals(key) && basis.containsKey(key); }
        
        @Override public Stream<KeyValue<?, ?>> stream() {
            return basis.stream().<KeyValue<?, ?>>map(identity())
                    .filter(entry -> !key.equals(entry.getKey())); }
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
            super(expectRandom(context), key, value);
        }

        /**
         * Construct the object with the given parent context and removal key.
         *
         * @param context the given parent context
         * @param removeKey the given remove key
         */
        public RandomMatchContext(Association<?, ?, ?> context, Object removeKey) {
            super(expectRandom(context), removeKey);
        }

        @Override public Container<Object> keys() {
            return new KeysContainer(); }


        /**
         * {@link RandomMatch} {@link RandomMatchContext.KeysContainer} implementation.
         */
        protected class KeysContainer extends NestContext.KeysContainer implements RandomMatch {}

        /**
         * Assert the specified {@link Association} context is instance of {@link RandomMatch} and return.
         *
         * @param context the specified {@link Association} context
         * @return the specified {@link Association} context if it is an instance of {@link RandomMatch}
         */
        private static Association<?, ?, ?> expectRandom(Association<?, ?, ?> context) {
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
