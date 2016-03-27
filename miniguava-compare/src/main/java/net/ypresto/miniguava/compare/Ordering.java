/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ypresto.miniguava.compare;

import static net.ypresto.miniguava.base.Preconditions.checkNotNull;

import net.ypresto.miniguava.base.Function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import javax.annotation.Nullable;

/**
 * A comparator, with additional methods to support common operations. This is an "enriched"
 * version of {@code Comparator}.
 *
 * <h3>Three types of methods</h3>
 *
 * Like other fluent types, there are three types of methods present: methods for <i>acquiring</i>,
 * <i>chaining</i>, and <i>using</i>.
 *
 * <h4>Acquiring</h4>
 *
 * <p>The common ways to get an instance of {@code Ordering} are:
 *
 * <ul>
 * <li>Subclass it and implement {@link #compare} instead of implementing {@link Comparator}
 *     directly
 * <li>Pass a <i>pre-existing</i> {@link Comparator} instance to {@link #from(Comparator)}
 * <li>Use the natural ordering, {@link Ordering#natural}
 * </ul>
 *
 * <h4>Chaining</h4>
 *
 * <p>Then you can use the <i>chaining</i> methods to get an altered version of that {@code
 * Ordering}, including:
 *
 * <ul>
 * <li>{@link #reverse}
 * <li>{@link #compound(Comparator)}
 * <li>{@link #onResultOf(Function)}
 * <li>{@link #nullsFirst} / {@link #nullsLast}
 * </ul>
 *
 * <h4>Using</h4>
 *
 * <p>Finally, use the resulting {@code Ordering} anywhere a {@link Comparator} is required.</p>
 *
 * <h3>Understanding complex orderings</h3>
 *
 * <p>Complex chained orderings like the following example can be challenging to understand.
 * <pre>   {@code
 *
 *   Ordering<Foo> ordering =
 *       Ordering.natural()
 *           .nullsFirst()
 *           .onResultOf(getBarFunction)
 *           .nullsLast();}</pre>
 *
 * Note that each chaining method returns a new ordering instance which is backed by the previous
 * instance, but has the chance to act on values <i>before</i> handing off to that backing
 * instance. As a result, it usually helps to read chained ordering expressions <i>backwards</i>.
 * For example, when {@code compare} is called on the above ordering:
 *
 * <ol>
 * <li>First, if only one {@code Foo} is null, that null value is treated as <i>greater</i>
 * <li>Next, non-null {@code Foo} values are passed to {@code getBarFunction} (we will be
 *     comparing {@code Bar} values from now on)
 * <li>Next, if only one {@code Bar} is null, that null value is treated as <i>lesser</i>
 * <li>Finally, natural ordering is used (i.e. the result of {@code Bar.compareTo(Bar)} is
 *     returned)
 * </ol>
 *
 * <p>Alas, {@link #reverse} is a little different. As you read backwards through a chain and
 * encounter a call to {@code reverse}, continue working backwards until a result is determined,
 * and then reverse that result.
 *
 * <h3>Additional notes</h3>
 *
 * <p>Except as noted, the orderings returned by the factory methods of this
 * class are NOT serializable, in contrast to Guava's original implementation.
 *
 * <p>See the Guava User Guide article on <a href=
 * "https://github.com/google/guava/wiki/OrderingExplained">
 * {@code Ordering}</a>.
 *
 * @author Jesse Wilson
 * @author Kevin Bourrillion
 * @since 2.0
 */
// miniguava: Removed Iterator related methods as it can be replaced by stream api or its backport.
// miniguava: Removed arbitrary() as it is using deprecated method of MapMaker.
// miniguava: Removed serialization support. Refer each subclass of Ordering.
public abstract class Ordering<T> implements Comparator<T> {
  // Natural order

  /**
   * Returns a ordering that uses the natural order of the values.
   * The ordering throws a {@link NullPointerException} when passed a null
   * parameter.
   *
   * <p>The type specification is {@code <C extends Comparable>}, instead of
   * the technically correct {@code <C extends Comparable<? super C>>}, to
   * support legacy types from before Java 5.
   */
  @SuppressWarnings("unchecked") // TODO(kevinb): right way to explain this??
  public static <C extends Comparable> Ordering<C> natural() {
    return (Ordering<C>) NaturalOrdering.INSTANCE;
  }

  // Static factories

  /**
   * Returns an ordering based on an <i>existing</i> comparator instance. Note
   * that it is unnecessary to create a <i>new</i> anonymous inner class
   * implementing {@code Comparator} just to pass it in here. Instead, simply
   * subclass {@code Ordering} and implement its {@code compare} method
   * directly.
   *
   * @param comparator the comparator that defines the order
   * @return comparator itself if it is already an {@code Ordering}; otherwise
   *     an ordering that wraps that comparator
   */
  public static <T> Ordering<T> from(Comparator<T> comparator) {
    return (comparator instanceof Ordering)
        ? (Ordering<T>) comparator
        : new ComparatorOrdering<T>(comparator);
  }

  /**
   * Returns an ordering that compares objects according to the order in
   * which they appear in the given list. Only objects present in the list
   * (according to {@link Object#equals}) may be compared. This comparator
   * imposes a "partial ordering" over the type {@code T}. Subsequent changes
   * to the {@code valuesInOrder} list will have no effect on the returned
   * comparator. Null values in the list are not supported.
   *
   * <p>The returned comparator throws an {@link ClassCastException} when it
   * receives an input parameter that isn't among the provided values.
   *
   * @param valuesInOrder the values that the returned comparator will be able
   *     to compare, in the order the comparator should induce
   * @return the comparator described above
   * @throws NullPointerException if any of the provided values is null
   * @throws IllegalArgumentException if {@code valuesInOrder} contains any
   *     duplicate values (according to {@link Object#equals})
   */
  public static <T> Ordering<T> explicit(List<T> valuesInOrder) {
    return new ExplicitOrdering<T>(valuesInOrder);
  }

  // miniguava: Modified not to use Lists.asList().
  /**
   * Returns an ordering that compares objects according to the order in
   * which they are given to this method. Only objects present in the argument
   * list (according to {@link Object#equals}) may be compared. This comparator
   * imposes a "partial ordering" over the type {@code T}. Null values in the
   * argument list are not supported.
   *
   * <p>The returned comparator throws a {@link ClassCastException} when it
   * receives an input parameter that isn't among the provided values.
   *
   * @param leastValue the value which the returned comparator should consider
   *     the "least" of all values
   * @param remainingValuesInOrder the rest of the values that the returned
   *     comparator will be able to compare, in the order the comparator should
   *     follow
   * @return the comparator described above
   * @throws NullPointerException if any of the provided values is null
   * @throws IllegalArgumentException if any duplicate values (according to
   *     {@link Object#equals(Object)}) are present among the method arguments
   */
  public static <T> Ordering<T> explicit(T leastValue, T... remainingValuesInOrder) {
    List<T> list = new ArrayList<T>(remainingValuesInOrder.length + 1);
    list.add(leastValue);
    Collections.addAll(list, remainingValuesInOrder);
    return explicit(list);
  }

  // Ordering<Object> singletons

  /**
   * Returns an ordering which treats all values as equal, indicating "no
   * ordering." Passing this ordering to any <i>stable</i> sort algorithm
   * results in no change to the order of elements.
   *
   * <p>Example: <pre>   {@code
   *
   *   Ordering.allEqual().nullsLast().sortedCopy(
   *       asList(t, null, e, s, null, t, null))}</pre>
   *
   * <p>Assuming {@code t}, {@code e} and {@code s} are non-null, this returns
   * {@code [t, e, s, t, null, null, null]} regardlesss of the true comparison
   * order of those three values (which might not even implement {@link
   * Comparable} at all).
   *
   * <p><b>Warning:</b> by definition, this comparator is not <i>consistent with
   * equals</i> (as defined {@linkplain Comparator here}). Avoid its use in
   * APIs, such as {@link TreeSet#TreeSet(Comparator)}, where such consistency
   * is expected.
   *
   * @since 13.0
   */
  @SuppressWarnings("unchecked")
  public static Ordering<Object> allEqual() {
    return AllEqualOrdering.INSTANCE;
  }

  /**
   * Returns an ordering that compares objects by the natural ordering of their
   * string representations as returned by {@code toString()}. It does not
   * support null values.
   */
  public static Ordering<Object> usingToString() {
    return UsingToStringOrdering.INSTANCE;
  }

  // Constructor

  /**
   * Constructs a new instance of this class (only invokable by the subclass
   * constructor, typically implicit).
   */
  protected Ordering() {}

  // Instance-based factories (and any static equivalents)

  /**
   * Returns the reverse of this ordering; the {@code Ordering} equivalent to
   * {@link Collections#reverseOrder(Comparator)}.
   */
  // type parameter <S> lets us avoid the extra <String> in statements like:
  // Ordering<String> o = Ordering.<String>natural().reverse();
  public <S extends T> Ordering<S> reverse() {
    return new ReverseOrdering<S>(this);
  }

  /**
   * Returns an ordering that treats {@code null} as less than all other values
   * and uses {@code this} to compare non-null values.
   */
  // type parameter <S> lets us avoid the extra <String> in statements like:
  // Ordering<String> o = Ordering.<String>natural().nullsFirst();
  public <S extends T> Ordering<S> nullsFirst() {
    return new NullsFirstOrdering<S>(this);
  }

  /**
   * Returns an ordering that treats {@code null} as greater than all other
   * values and uses this ordering to compare non-null values.
   */
  // type parameter <S> lets us avoid the extra <String> in statements like:
  // Ordering<String> o = Ordering.<String>natural().nullsLast();
  public <S extends T> Ordering<S> nullsLast() {
    return new NullsLastOrdering<S>(this);
  }

  /**
   * Returns a new ordering on {@code F} which orders elements by first applying
   * a function to them, then comparing those results using {@code this}. For
   * example, to compare objects by their string forms, in a case-insensitive
   * manner, use: <pre>   {@code
   *
   *   Ordering.from(String.CASE_INSENSITIVE_ORDER)
   *       .onResultOf(Functions.toStringFunction())}</pre>
   */
  public <F> Ordering<F> onResultOf(Function<F, ? extends T> function) {
    return new ByFunctionOrdering<F, T>(function, this);
  }

  /**
   * Returns an ordering which first uses the ordering {@code this}, but which
   * in the event of a "tie", then delegates to {@code secondaryComparator}.
   * For example, to sort a bug list first by status and second by priority, you
   * might use {@code byStatus.compound(byPriority)}. For a compound ordering
   * with three or more components, simply chain multiple calls to this method.
   *
   * <p>An ordering produced by this method, or a chain of calls to this method,
   * is equivalent to one created using {@link Ordering#compound(List)} on
   * the same component comparators.
   */
  public <U extends T> Ordering<U> compound(Comparator<? super U> secondaryComparator) {
    return new CompoundOrdering<U>(this, checkNotNull(secondaryComparator));
  }

  /**
   * Returns an ordering which tries each given comparator in order until a
   * non-zero result is found, returning that result, and returning zero only if
   * all comparators return zero. The returned ordering is based on the state of
   * the {@code comparators} iterable at the time it was provided to this
   * method.
   *
   * <p>The returned ordering is equivalent to that produced using {@code
   * Ordering.from(comp1).compound(comp2).compound(comp3) . . .}.
   *
   * <p><b>Warning:</b> Supplying an argument with undefined iteration order,
   * such as a {@link HashSet}, will produce non-deterministic results.
   *
   * @param comparators the comparators to try in order
   */
  // miniguava: originally Iterable, modified to List.
  public static <T> Ordering<T> compound(List<? extends Comparator<? super T>> comparators) {
    return new CompoundOrdering<T>(comparators);
  }

  /**
   * Returns a new ordering which sorts iterables by comparing corresponding
   * elements pairwise until a nonzero result is found; imposes "dictionary
   * order". If the end of one iterable is reached, but not the other, the
   * shorter iterable is considered to be less than the longer one. For example,
   * a lexicographical natural ordering over integers considers {@code
   * [] < [1] < [1, 1] < [1, 2] < [2]}.
   *
   * <p>Note that {@code ordering.lexicographical().reverse()} is not
   * equivalent to {@code ordering.reverse().lexicographical()} (consider how
   * each would order {@code [1]} and {@code [1, 1]}).
   *
   * @since 2.0
   */
  // type parameter <S> lets us avoid the extra <String> in statements like:
  // Ordering<Iterable<String>> o =
  //     Ordering.<String>natural().lexicographical();
  public <S extends T> Ordering<Iterable<S>> lexicographical() {
    /*
     * Note that technically the returned ordering should be capable of
     * handling not just {@code Iterable<S>} instances, but also any {@code
     * Iterable<? extends S>}. However, the need for this comes up so rarely
     * that it doesn't justify making everyone else deal with the very ugly
     * wildcard.
     */
    return new LexicographicalOrdering<S>(this);
  }

  // Regular instance methods

  // Override to add @Nullable
  @Override
  public abstract int compare(@Nullable T left, @Nullable T right);

  /**
   * Exception thrown by a {@link Ordering#explicit(List)} or {@link
   * Ordering#explicit(Object, Object[])} comparator when comparing a value
   * outside the set of values it can compare. Extending {@link
   * ClassCastException} may seem odd, but it is required.
   */
  // TODO(kevinb): make this public, document it right
  static class IncomparableValueException extends ClassCastException {
    final Object value;

    IncomparableValueException(Object value) {
      super("Cannot compare value: " + value);
      this.value = value;
    }

    private static final long serialVersionUID = 0;
  }

  // Never make these public
  static final int LEFT_IS_GREATER = 1;
  static final int RIGHT_IS_GREATER = -1;
}
