package org.moodminds.traverse;

import org.moodminds.elemental.Association;
import org.moodminds.function.Executable1Throwing2;
import org.moodminds.function.Executable1Throwing3;
import org.moodminds.traverse.TraverseSupport.Traverser;

import java.util.NoSuchElementException;

/**
 * A {@link TraverseSupport} execution referring method.
 */
public enum TraverseMethod {

    /**
     * Represents a set of 'sequence' methods.
     */
    SEQUENCE {
        @Override public <V, E extends Exception, H1 extends Exception, H2 extends Exception> boolean traverse(TraverseSupport<? extends V, ? extends E> traverseSupport, Executable1Throwing2<? super Traverser<? extends V, ? extends E>, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {
            return traverseSupport.sequence(traverse, ctx); }
        @Override public <V, E extends Exception, H1 extends Exception, H2 extends Exception, H3 extends Exception> boolean traverse(TraverseSupport<? extends V, ? extends E> traverseSupport, Executable1Throwing3<? super Traverser<? extends V, ? extends E>, ? extends H1, ? extends H2, ? extends H3> traverse, Association<?, ?, ?> ctx) throws E, H1, H2, H3 {
            return traverseSupport.sequence(traverse, ctx); }
    },

    /**
     * Represents a set of 'traverse' methods.
     */
    TRAVERSE {
        @Override public <V, E extends Exception, H1 extends Exception, H2 extends Exception> boolean traverse(TraverseSupport<? extends V, ? extends E> traverseSupport, Executable1Throwing2<? super Traverser<? extends V, ? extends E>, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {
            return traverseSupport.traverse(traverse, ctx); }
        @Override public <V, E extends Exception, H1 extends Exception, H2 extends Exception, H3 extends Exception> boolean traverse(TraverseSupport<? extends V, ? extends E> traverseSupport, Executable1Throwing3<? super Traverser<? extends V, ? extends E>, ? extends H1, ? extends H2, ? extends H3> traverse, Association<?, ?, ?> ctx) throws E, H1, H2, H3 {
            return traverseSupport.traverse(traverse, ctx); }
    },

    /**
     * Represents a set of 'parallel' methods.
     */
    PARALLEL {
        @Override public <V, E extends Exception, H1 extends Exception, H2 extends Exception> boolean traverse(TraverseSupport<? extends V, ? extends E> traverseSupport, Executable1Throwing2<? super Traverser<? extends V, ? extends E>, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2 {
            return traverseSupport.parallel(traverse, ctx); }
        @Override public <V, E extends Exception, H1 extends Exception, H2 extends Exception, H3 extends Exception> boolean traverse(TraverseSupport<? extends V, ? extends E> traverseSupport, Executable1Throwing3<? super Traverser<? extends V, ? extends E>, ? extends H1, ? extends H2, ? extends H3> traverse, Association<?, ?, ?> ctx) throws E, H1, H2, H3 {
            return traverseSupport.parallel(traverse, ctx); }
    };


    /**
     * Check if this method is of type {@link #SEQUENCE}.
     *
     * @return true if this method is of type {@link #SEQUENCE}, false otherwise.
     */
    public boolean isSequence() { return this == SEQUENCE; }

    /**
     * Check if this method is of type {@link #TRAVERSE}.
     *
     * @return true if this method is of type {@link #TRAVERSE}, false otherwise.
     */
    public boolean isTraverse() { return this == TRAVERSE; }

    /**
     * Check if this method is of type {@link #PARALLEL}.
     *
     * @return true if this method is of type {@link #PARALLEL}, false otherwise.
     */
    public boolean isParallel() { return this == PARALLEL; }


    /**
     * Try to traverse the given {@link TraverseSupport} with the given {@link Executable1Throwing2} traverse function
     * and {@link Association} context.
     * <p>
     * The specified {@link Association} must contain non-null keys and values, and its {@link Association#get(Object)}
     * method should raise a {@link NoSuchElementException} instead of returning {@code null} when no value is associated
     * with a key.
     *
     * @param traverseSupport the given {@link TraverseSupport}
     * @param traverse the given {@link Executable1Throwing2} traverse function
     * @param ctx the given {@link Association} context
     * @return the completion flag indicating either the Source was completely traversed, or not
     * @param <V> the type of item values
     * @param <E> the type of potential exceptions
     * @param <H1> the possible exception 1 type throwing by the traverse function
     * @param <H2> the possible exception 2 type throwing by the traverse function
     * @throws E in case of the traversal error
     * @throws H1 in the case of traverse function error 1
     * @throws H2 in the case of traverse function error 2
     * @throws NullPointerException if the specified traverse function is {@code null} or {@link Association}
     * contains {@code null} keys or values
     */
    public abstract <V, E extends Exception, H1 extends Exception, H2 extends Exception> boolean traverse(TraverseSupport<? extends V, ? extends E> traverseSupport, Executable1Throwing2<? super Traverser<? extends V, ? extends E>, ? extends H1, ? extends H2> traverse, Association<?, ?, ?> ctx) throws E, H1, H2;

    /**
     * Try to traverse the given {@link TraverseSupport} with the given {@link Executable1Throwing3} traverse function
     * and {@link Association} context.
     * <p>
     * The specified {@link Association} must contain non-null keys and values, and its {@link Association#get(Object)}
     * method should raise a {@link NoSuchElementException} instead of returning {@code null} when no value is associated
     * with a key.
     *
     * @param traverseSupport the given {@link TraverseSupport}
     * @param traverse the given {@link Executable1Throwing3} traverse function
     * @param ctx the given {@link Association} context
     * @return the completion flag indicating either the Source was completely traversed, or not
     * @param <V> the type of item values
     * @param <E> the type of potential exceptions
     * @param <H1> the possible exception 1 type throwing by the traverse function
     * @param <H2> the possible exception 2 type throwing by the traverse function
     * @param <H3> the possible exception 3 type throwing by the traverse function
     * @throws E in case of the traversal error
     * @throws H1 in the case of traverse function error 1
     * @throws H2 in the case of traverse function error 2
     * @throws H3 in the case of traverse function error 3
     * @throws NullPointerException if the specified traverse function is {@code null} or {@link Association}
     * contains {@code null} keys or values
     */
    public abstract <V, E extends Exception, H1 extends Exception, H2 extends Exception, H3 extends Exception> boolean traverse(TraverseSupport<? extends V, ? extends E> traverseSupport, Executable1Throwing3<? super Traverser<? extends V, ? extends E>, ? extends H1, ? extends H2, ? extends H3> traverse, Association<?, ?, ?> ctx) throws E, H1, H2, H3;
}
