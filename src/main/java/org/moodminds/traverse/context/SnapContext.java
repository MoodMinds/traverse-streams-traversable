package org.moodminds.traverse.context;

import org.moodminds.elemental.AbstractMapAssociation;
import org.moodminds.elemental.Association;
import org.moodminds.elemental.Container;
import org.moodminds.elemental.EmptyIterator;
import org.moodminds.elemental.KeyValue;
import org.moodminds.elemental.SingleIterator;
import org.moodminds.elemental.WrapIterator;
import org.moodminds.elemental.WrapKeyValue;
import org.moodminds.elemental.WrapSpliterator;
import org.moodminds.sneaky.Cast;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterator;

import static java.lang.Math.max;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.Spliterator.IMMUTABLE;
import static org.moodminds.elemental.ArraySequence.sequence;
import static org.moodminds.elemental.Pair.pair;

/**
 * Implementation of the {@link Association} context, representing a context variables snapshot.
 *
 * <p>This implementation uses a wrapped {@link LinkedHashMap} to store variables.
 */
public class SnapContext extends AbstractMapAssociation<Object, Object,
            KeyValue<Object, Object>, Map<Object, Object>> {

    private static final long serialVersionUID = 5878531652880203896L;

    /**
     * Construct the object with the given {@link KeyValue} vararg context.
     *
     * @param context the given {@link KeyValue} vararg context
     */
    public SnapContext(KeyValue<?, ?>... context) {
        this(sequence(context));
    }

    /**
     * Construct the object with the given parent context and adding key and value.
     *
     * @param context the given parent context
     * @param key the given adding key
     * @param value the given adding value
     */
    public SnapContext(Container<? extends KeyValue<?, ?>> context, Object key, Object value) {
        this(context); map.put(requireNonNull(key), requireNonNull(value));
    }

    /**
     * Construct the object with the given parent context and removal key.
     *
     * @param context the given parent context
     * @param removeKey the given remove key
     */
    public SnapContext(Container<? extends KeyValue<?, ?>> context, Object removeKey) {
        this(context); map.remove(requireNonNull(removeKey));
    }

    /**
     * Construct the object with the given {@link Container} of {@link KeyValue} parent context.
     *
     * @param context the given {@link Container} of {@link KeyValue} parent context
     */
    private SnapContext(Container<? extends KeyValue<?, ?>> context) {
        super(new LinkedHashMap<>(max((int) (context.size()/.75f) + 1, 16))); context.stream().forEach(kv -> map.put(
                requireNonNull(kv.getKey(), "Key cannot be null."),
                requireNonNull(kv.getValue(), "Value cannot be null.")));
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
        return ofNullable(super.<R>get(key)).orElseThrow(() ->
                new NoSuchElementException("No value in context for key: " + key));
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Container<Object> keys() {
        return new SnapKeysContainer();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Container<Object> values() {
        return new SnapValuesContainer();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Iterator<KeyValue<Object, Object>> iterator() {
        return WrapIterator.wrap(map.entrySet().iterator(), WrapKeyValue::wrap);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Spliterator<KeyValue<Object, Object>> spliterator() {
        return WrapSpliterator.wrap(map.entrySet().spliterator(), Cast::cast,
                WrapKeyValue::wrap, ch -> ch | IMMUTABLE);
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
     * Snap Context keys Container.
     */
    protected class SnapKeysContainer extends AbstractKeysContainer {

        @Override public Spliterator<Object> spliterator() {
            return WrapSpliterator.wrap(SnapContext.this.map.keySet().spliterator(),
                    ch -> ch | IMMUTABLE); }
    }

    /**
     * Snap Context values Container.
     */
    protected class SnapValuesContainer extends AbstractValuesContainer {

        @Override public Spliterator<Object> spliterator() {
            return WrapSpliterator.wrap(SnapContext.this.map.values().spliterator(),
                    ch -> ch | IMMUTABLE); }
    }


    /**
     * Return new {@link SnapContext} by the given {@link KeyValue} vararg context.
     *
     * @param context the given {@link KeyValue} vararg context
     * @return new {@link SnapContext} by the given {@link KeyValue} vararg context
     */
    public static SnapContext context(KeyValue<?, ?>... context) {
        return new SnapContext(context);
    }

    /**
     * Return new {@link SnapContext} by the given parent context and adding key and value.
     *
     * @param context the given parent context
     * @param key the given adding key
     * @param value the given adding value
     * @return new {@link SnapContext} by the given parent context and adding key and value
     */
    public static SnapContext context(Association<Object, Object, ?> context, Object key, Object value) {
        return new SnapContext(context, key, value);
    }

    /**
     * Return new {@link SnapContext} by the given parent context and removal key.
     *
     * @param context the given parent context
     * @param removeKey the given remove key
     * @return new {@link SnapContext} by the given parent context and removal key
     */
    public static SnapContext context(Association<Object, Object, ?> context, Object removeKey) {
        return new SnapContext(context, removeKey);
    }
}
