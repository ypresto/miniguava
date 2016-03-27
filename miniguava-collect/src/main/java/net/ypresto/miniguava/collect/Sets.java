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

package net.ypresto.miniguava.collect;

import static net.ypresto.miniguava.base.Preconditions.checkArgument;
import static net.ypresto.miniguava.base.Preconditions.checkNotNull;

import net.ypresto.miniguava.base.Predicate;
import net.ypresto.miniguava.collect.internal.AbstractIndexedListIterator;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

/**
 * Static utility methods pertaining to {@link Set} instances. Also see this
 * class's counterparts {@link Lists}, {@link Maps} and {@code Queues}.
 *
 * <p>See the Guava User Guide article on <a href=
 * "https://github.com/google/guava/wiki/CollectionUtilitiesExplained#sets">
 * {@code Sets}</a>.
 *
 * @author Kevin Bourrillion
 * @author Jared Levy
 * @author Chris Povirk
 * @since 2.0
 */
// miniguava: Removed newXX() methods.
// miniguava: Removed internal helper methods with package-private visibility.
// miniguava: Removed filter() methods as many operations on set view are inefficient. Please open issue if you want to use it.
// miniguava: Removed NavigableSet related methods.
public final class Sets {
  private Sets() {}

  /**
   * {@link AbstractSet} substitute without the potentially-quadratic
   * {@code removeAll} implementation.
   */
  abstract static class ImprovedAbstractSet<E> extends AbstractSet<E> {
    @Override
    public boolean removeAll(Collection<?> c) {
      return removeAllImpl(this, c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      return super.retainAll(checkNotNull(c)); // GWT compatibility
    }
  }

  /**
   * Creates a thread-safe set backed by a hash map. The set is backed by a
   * {@link ConcurrentHashMap} instance, and thus carries the same concurrency
   * guarantees.
   *
   * <p>Unlike {@code HashSet}, this class does NOT allow {@code null} to be
   * used as an element. The set is serializable.
   *
   * @return a new, empty thread-safe {@code Set}
   * @since 15.0
   */
  // miniguava: Modified not to use Platform.java
  // miniguava: Android: Note that Collections.newSetFromMap() is added at API 9.
  public static <E> Set<E> newConcurrentHashSet() {
    return Collections.newSetFromMap(new ConcurrentHashMap<E, Boolean>());
  }

  /**
   * Creates a thread-safe set backed by a hash map and containing the given
   * elements. The set is backed by a {@link ConcurrentHashMap} instance, and
   * thus carries the same concurrency guarantees.
   *
   * <p>Unlike {@code HashSet}, this class does NOT allow {@code null} to be
   * used as an element. The set is serializable.
   *
   * @param elements the elements that the set should contain
   * @return a new thread-safe set containing those elements (minus duplicates)
   * @throws NullPointerException if {@code elements} or any of its contents is
   *      null
   * @since 15.0
   */
  // miniguava: Changed from Iterable to Collection.
  public static <E> Set<E> newConcurrentHashSet(Collection<? extends E> elements) {
    Set<E> set = newConcurrentHashSet();
    set.addAll(elements);
    return set;
  }

  /**
   * Creates an empty {@code Set} that uses identity to determine equality. It
   * compares object references, instead of calling {@code equals}, to
   * determine whether a provided object matches an element in the set. For
   * example, {@code contains} returns {@code false} when passed an object that
   * equals a set member, but isn't the same instance. This behavior is similar
   * to the way {@code IdentityHashMap} handles key lookups.
   *
   * @since 8.0
   */
  // miniguava: Android: Note that Collections.newSetFromMap() is added at API 9.
  public static <E> Set<E> newIdentityHashSet() {
    return Collections.newSetFromMap(new IdentityHashMap<E, Boolean>());
  }

  /**
   * Creates an {@code EnumSet} consisting of all enum values that are not in
   * the specified collection. If the collection is an {@link EnumSet}, this
   * method has the same behavior as {@link EnumSet#complementOf}. Otherwise,
   * the specified collection must contain at least one element, in order to
   * determine the element type. If the collection could be empty, use
   * {@link #complementOf(Collection, Class)} instead of this method.
   *
   * @param collection the collection whose complement should be stored in the
   *     enum set
   * @return a new, modifiable {@code EnumSet} containing all values of the enum
   *     that aren't present in the given collection
   * @throws IllegalArgumentException if {@code collection} is not an
   *     {@code EnumSet} instance and contains no elements
   */
  public static <E extends Enum<E>> EnumSet<E> complementOf(Collection<E> collection) {
    if (collection instanceof EnumSet) {
      return EnumSet.complementOf((EnumSet<E>) collection);
    }
    checkArgument(
        !collection.isEmpty(), "collection is empty; use the other version of this method");
    Class<E> type = collection.iterator().next().getDeclaringClass();
    return makeComplementByHand(collection, type);
  }

  /**
   * Creates an {@code EnumSet} consisting of all enum values that are not in
   * the specified collection. This is equivalent to
   * {@link EnumSet#complementOf}, but can act on any input collection, as long
   * as the elements are of enum type.
   *
   * @param collection the collection whose complement should be stored in the
   *     {@code EnumSet}
   * @param type the type of the elements in the set
   * @return a new, modifiable {@code EnumSet} initially containing all the
   *     values of the enum not present in the given collection
   */
  public static <E extends Enum<E>> EnumSet<E> complementOf(
      Collection<E> collection, Class<E> type) {
    checkNotNull(collection);
    return (collection instanceof EnumSet)
        ? EnumSet.complementOf((EnumSet<E>) collection)
        : makeComplementByHand(collection, type);
  }

  private static <E extends Enum<E>> EnumSet<E> makeComplementByHand(
      Collection<E> collection, Class<E> type) {
    EnumSet<E> result = EnumSet.allOf(type);
    result.removeAll(collection);
    return result;
  }

  /**
   * An unmodifiable view of a set which may be backed by other sets; this view
   * will change as the backing sets do. Contains methods to copy the data into
   * a new set which will then remain stable. There is usually no reason to
   * retain a reference of type {@code SetView}; typically, you either use it
   * as a plain {@link Set}, or immediately invoke {@link HashSet#HashSet(Collection)},
   * {@code ImmutableSet#copyOf} or {@link #copyInto} and forget the {@code SetView} itself.
   *
   * @since 2.0
   */
  public abstract static class SetView<E> extends AbstractSet<E> {
    private SetView() {} // no subclasses but our own

    // miniguava: Removed immutableCopy method

    /**
     * Copies the current contents of this set view into an existing set. This
     * method has equivalent behavior to {@code set.addAll(this)}, assuming that
     * all the sets involved are based on the same notion of equivalence.
     *
     * @return a reference to {@code set}, for convenience
     */
    // Note: S should logically extend Set<? super E> but can't due to either
    // some javac bug or some weirdness in the spec, not sure which.
    public <S extends Set<E>> S copyInto(S set) {
      set.addAll(this);
      return set;
    }
  }

  /**
   * Returns an unmodifiable <b>view</b> of the union of two sets. The returned
   * set contains all elements that are contained in either backing set.
   * Iterating over the returned set iterates first over all the elements of
   * {@code set1}, then over each element of {@code set2}, in order, that is not
   * contained in {@code set1}.
   *
   * <p>Results are undefined if {@code set1} and {@code set2} are sets based on
   * different equivalence relations (as {@link HashSet}, {@link TreeSet}, and
   * the {@link Map#keySet} of an {@code IdentityHashMap} all are).
   *
   * <p><b>Note:</b> The returned view performs better when {@code set1} is the
   * smaller of the two sets. If you have reason to believe one of your sets
   * will generally be smaller than the other, pass it first.
   *
   * <p>Further, note that the current implementation is not suitable for nested
   * {@code union} views, i.e. the following should be avoided when in a loop:
   * {@code union = Sets.union(union, anotherSet);}, since iterating over the resulting
   * set has a cubic complexity to the depth of the nesting.
   */
  public static <E> SetView<E> union(final Set<? extends E> set1, final Set<? extends E> set2) {
    checkNotNull(set1, "set1");
    checkNotNull(set2, "set2");

    final Set<? extends E> set2minus1 = difference(set2, set1);

    return new SetView<E>() {
      @Override
      public int size() {
        return set1.size() + set2minus1.size();
      }

      @Override
      public boolean isEmpty() {
        return set1.isEmpty() && set2.isEmpty();
      }

      @Override
      public Iterator<E> iterator() {
        return Iterators.unmodifiableIterator(
            Iterators.concat(set1.iterator(), set2minus1.iterator()));
      }

      @Override
      public boolean contains(Object object) {
        return set1.contains(object) || set2.contains(object);
      }

      @Override
      public <S extends Set<E>> S copyInto(S set) {
        set.addAll(set1);
        set.addAll(set2);
        return set;
      }
    };
  }

  /**
   * Returns an unmodifiable <b>view</b> of the intersection of two sets. The
   * returned set contains all elements that are contained by both backing sets.
   * The iteration order of the returned set matches that of {@code set1}.
   *
   * <p>Results are undefined if {@code set1} and {@code set2} are sets based
   * on different equivalence relations (as {@code HashSet}, {@code TreeSet},
   * and the keySet of an {@code IdentityHashMap} all are).
   *
   * <p><b>Note:</b> The returned view performs slightly better when {@code
   * set1} is the smaller of the two sets. If you have reason to believe one of
   * your sets will generally be smaller than the other, pass it first.
   * Unfortunately, since this method sets the generic type of the returned set
   * based on the type of the first set passed, this could in rare cases force
   * you to make a cast, for example: <pre>   {@code
   *
   *   Set<Object> aFewBadObjects = ...
   *   Set<String> manyBadStrings = ...
   *
   *   // impossible for a non-String to be in the intersection
   *   SuppressWarnings("unchecked")
   *   Set<String> badStrings = (Set) Sets.intersection(
   *       aFewBadObjects, manyBadStrings);}</pre>
   *
   * <p>This is unfortunate, but should come up only very rarely.
   */
  public static <E> SetView<E> intersection(final Set<E> set1, final Set<?> set2) {
    checkNotNull(set1, "set1");
    checkNotNull(set2, "set2");

    final Predicate<Object> inSet2 = InternalPredicates.in(set2);
    return new SetView<E>() {
      @Override
      public Iterator<E> iterator() {
        return Iterators.filter(set1.iterator(), inSet2);
      }

      @Override
      public int size() {
        return Iterators.size(iterator());
      }

      @Override
      public boolean isEmpty() {
        return !iterator().hasNext();
      }

      @Override
      public boolean contains(Object object) {
        return set1.contains(object) && set2.contains(object);
      }

      @Override
      public boolean containsAll(Collection<?> collection) {
        return set1.containsAll(collection) && set2.containsAll(collection);
      }
    };
  }

  /**
   * Returns an unmodifiable <b>view</b> of the difference of two sets. The
   * returned set contains all elements that are contained by {@code set1} and
   * not contained by {@code set2}. {@code set2} may also contain elements not
   * present in {@code set1}; these are simply ignored. The iteration order of
   * the returned set matches that of {@code set1}.
   *
   * <p>Results are undefined if {@code set1} and {@code set2} are sets based
   * on different equivalence relations (as {@code HashSet}, {@code TreeSet},
   * and the keySet of an {@code IdentityHashMap} all are).
   */
  public static <E> SetView<E> difference(final Set<E> set1, final Set<?> set2) {
    checkNotNull(set1, "set1");
    checkNotNull(set2, "set2");

    final Predicate<Object> notInSet2 = InternalPredicates.not(InternalPredicates.in(set2));
    return new SetView<E>() {
      @Override
      public Iterator<E> iterator() {
        return Iterators.filter(set1.iterator(), notInSet2);
      }

      @Override
      public int size() {
        return Iterators.size(iterator());
      }

      @Override
      public boolean isEmpty() {
        return set2.containsAll(set1);
      }

      @Override
      public boolean contains(Object element) {
        return set1.contains(element) && !set2.contains(element);
      }
    };
  }

  /**
   * Returns an unmodifiable <b>view</b> of the symmetric difference of two
   * sets. The returned set contains all elements that are contained in either
   * {@code set1} or {@code set2} but not in both. The iteration order of the
   * returned set is undefined.
   *
   * <p>Results are undefined if {@code set1} and {@code set2} are sets based
   * on different equivalence relations (as {@code HashSet}, {@code TreeSet},
   * and the keySet of an {@code IdentityHashMap} all are).
   *
   * @since 3.0
   */
  public static <E> SetView<E> symmetricDifference(
      final Set<? extends E> set1, final Set<? extends E> set2) {
    checkNotNull(set1, "set1");
    checkNotNull(set2, "set2");

    return new SetView<E>() {
      @Override
      public Iterator<E> iterator() {
        final Iterator<? extends E> itr1 = set1.iterator();
        final Iterator<? extends E> itr2 = set2.iterator();
        return new AbstractIterator<E>() {
          @Override
          public E computeNext() {
            while (itr1.hasNext()) {
              E elem1 = itr1.next();
              if (!set2.contains(elem1)) {
                return elem1;
              }
            }
            while (itr2.hasNext()) {
              E elem2 = itr2.next();
              if (!set1.contains(elem2)) {
                return elem2;
              }
            }
            return endOfData();
          }
        };
      }

      @Override
      public int size() {
        return Iterators.size(iterator());
      }

      @Override
      public boolean isEmpty() {
        return set1.equals(set2);
      }

      @Override
      public boolean contains(Object element) {
        return set1.contains(element) ^ set2.contains(element);
      }
    };
  }

  // miniguava: Removed filter() family methods as it is easy to check (add if-statement) before accessing source Set.

  /**
   * Returns the set of all possible subsets of {@code set}. For example,
   * {@code powerSet(ImmutableSet.of(1, 2))} returns the set {@code {{},
   * {1}, {2}, {1, 2}}}.
   *
   * <p>Elements appear in these subsets in the same iteration order as they
   * appeared in the input set. The order in which these subsets appear in the
   * outer set is undefined. Note that the power set of the empty set is not the
   * empty set, but a one-element set containing the empty set.
   *
   * <p>The returned set and its constituent sets use {@code equals} to decide
   * whether two elements are identical, even if the input set uses a different
   * concept of equivalence.
   *
   * <p><i>Performance notes:</i> while the power set of a set with size {@code
   * n} is of size {@code 2^n}, its memory usage is only {@code O(n)}. When the
   * power set is constructed, the input set is merely copied. Only as the
   * power set is iterated are the individual subsets created, and these subsets
   * themselves occupy only a small constant amount of memory.
   *
   * @param set the set of elements to construct a power set from
   * @return the power set, as an immutable set of immutable sets
   * @throws IllegalArgumentException if {@code set} has more than 30 unique
   *     elements (causing the power set size to exceed the {@code int} range)
   * @throws NullPointerException if {@code set} is or contains {@code null}
   * @see <a href="http://en.wikipedia.org/wiki/Power_set">Power set article at
   *      Wikipedia</a>
   * @since 4.0
   */
  public static <E> Set<Set<E>> powerSet(Set<E> set) {
    return new PowerSet<E>(set);
  }

  private static final class SubSet<E> extends AbstractSet<E> {
    // miniguava: Modified from ImmutableMap to Map.
    private final Map<E, Integer> inputSet;
    private final int mask;
    private List<E> asList = null; // miniguava: Refer ImmutableCollection#asList()

    SubSet(Map<E, Integer> inputSet, int mask) {
      this.inputSet = inputSet;
      this.mask = mask;
    }

    @Override
    public Iterator<E> iterator() {
      // miniguava: Implemented asList and changed ImmutableList to List.
      if (asList == null) {
        asList = new ArrayList<E>(inputSet.keySet());
      }
      return new UnmodifiableIterator<E>() {
        final List<E> elements = asList;
        int remainingSetBits = mask;

        @Override
        public boolean hasNext() {
          return remainingSetBits != 0;
        }

        @Override
        public E next() {
          int index = Integer.numberOfTrailingZeros(remainingSetBits);
          if (index == 32) {
            throw new NoSuchElementException();
          }
          remainingSetBits &= ~(1 << index);
          return elements.get(index);
        }
      };
    }

    @Override
    public int size() {
      return Integer.bitCount(mask);
    }

    @Override
    public boolean contains(@Nullable Object o) {
      Integer index = inputSet.get(o);
      return index != null && (mask & (1 << index)) != 0;
    }
  }

  private static final class PowerSet<E> extends AbstractSet<Set<E>> {
    // miniguava: Modified from ImmutableMap to Map.
    final Map<E, Integer> inputSet;

    PowerSet(Set<E> input) {
      this.inputSet = Maps.indexMap(input); // miniguava: returns unmodifiableMap.
      checkArgument(
          inputSet.size() <= 30, "Too many elements to create power set: %s > 30", inputSet.size());
    }

    @Override
    public int size() {
      return 1 << inputSet.size();
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public Iterator<Set<E>> iterator() {
      return new AbstractIndexedListIterator<Set<E>>(size()) {
        @Override
        protected Set<E> get(final int setBits) {
          return new SubSet<E>(inputSet, setBits);
        }
      };
    }

    @Override
    public boolean contains(@Nullable Object obj) {
      if (obj instanceof Set) {
        Set<?> set = (Set<?>) obj;
        return inputSet.keySet().containsAll(set);
      }
      return false;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (obj instanceof PowerSet) {
        PowerSet<?> that = (PowerSet<?>) obj;
        return inputSet.equals(that.inputSet);
      }
      return super.equals(obj);
    }

    @Override
    public int hashCode() {
      /*
       * The sum of the sums of the hash codes in each subset is just the sum of
       * each input element's hash code times the number of sets that element
       * appears in. Each element appears in exactly half of the 2^n sets, so:
       */
      return inputSet.keySet().hashCode() << (inputSet.size() - 1);
    }

    @Override
    public String toString() {
      return "powerSet(" + inputSet + ")";
    }
  }

  /**
   * Remove each element in an iterable from a set.
   */
  static boolean removeAllImpl(Set<?> set, Iterator<?> iterator) {
    boolean changed = false;
    while (iterator.hasNext()) {
      changed |= set.remove(iterator.next());
    }
    return changed;
  }

  // miniguava: Removed Multiset check.
  static boolean removeAllImpl(Set<?> set, Collection<?> collection) {
    checkNotNull(collection); // for GWT
    /*
     * AbstractSet.removeAll(List) has quadratic behavior if the list size
     * is just less than the set's size.  We augment the test by
     * assuming that sets have fast contains() performance, and other
     * collections don't.  See
     * http://code.google.com/p/guava-libraries/issues/detail?id=1013
     */
    if (collection instanceof Set && collection.size() > set.size()) {
      return Iterators.removeAll(set.iterator(), collection);
    } else {
      return removeAllImpl(set, collection.iterator());
    }
  }

}
