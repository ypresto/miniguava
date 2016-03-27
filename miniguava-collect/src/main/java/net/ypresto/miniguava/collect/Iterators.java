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

import static net.ypresto.miniguava.base.Preconditions.checkNotNull;
import static net.ypresto.miniguava.collect.InternalPredicates.in;
import static net.ypresto.miniguava.collect.InternalUtils.checkRemove;

import net.ypresto.miniguava.annotations.MiniGuavaSpecific;
import net.ypresto.miniguava.annotations.VisibleForTesting;
import net.ypresto.miniguava.base.Function;
import net.ypresto.miniguava.base.Predicate;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckReturnValue;

/**
 * This class contains static utility methods that operate on or return objects
 * of type {@link Iterator}.
 *
 * <p><i>Performance notes:</i> Unless otherwise noted, all of the iterators
 * produced in this class are <i>lazy</i>, which means that they only advance
 * the backing iteration when absolutely necessary.
 *
 * <p>See the Guava User Guide section on <a href=
 * "https://github.com/google/guava/wiki/CollectionUtilitiesExplained#iterables">
 * {@code Iterators}</a>.
 *
 * @author Kevin Bourrillion
 * @author Jared Levy
 * @since 2.0
 */
// miniguava: Picked only methods not related to manipulation, as it can be done with stream API.
public final class Iterators {
  private Iterators() {}

  static final UnmodifiableListIterator<Object> EMPTY_LIST_ITERATOR =
      new UnmodifiableListIterator<Object>() {
        @Override
        public boolean hasNext() {
          return false;
        }

        @Override
        public Object next() {
          throw new NoSuchElementException();
        }

        @Override
        public boolean hasPrevious() {
          return false;
        }

        @Override
        public Object previous() {
          throw new NoSuchElementException();
        }

        @Override
        public int nextIndex() {
          return 0;
        }

        @Override
        public int previousIndex() {
          return -1;
        }
      };

  /**
   * Returns the empty iterator.
   *
   * <p>The {@link Iterable} equivalent of this method is {@code
   * ImmutableSet#of()}.
   *
   * @deprecated Use {@code ImmutableSet.<T>of().iterator()} instead; or for
   *     Java 7 or later, {@link Collections#emptyIterator}. This method is
   *     scheduled for removal in May 2016.
   */
  // miniguava: Decreased visibility from public, as it can be replaced with `emptyList().iterator()`.
  static <T> UnmodifiableIterator<T> emptyIterator() {
    return emptyListIterator();
  }

  /**
   * Returns the empty iterator.
   *
   * <p>The {@link Iterable} equivalent of this method is {@code
   * ImmutableSet#of()}.
   */
  // miniguava: Increased visibility from package-private, as Collections.emptyListIterator() is available on Java 7.
  @MiniGuavaSpecific(MiniGuavaSpecific.Reason.NOT_PUBLIC)
  // Casting to any type is safe since there are no actual elements.
  @SuppressWarnings("unchecked")
  public static <T> UnmodifiableListIterator<T> emptyListIterator() {
    return (UnmodifiableListIterator<T>) EMPTY_LIST_ITERATOR;
  }

  /** Returns an unmodifiable view of {@code iterator}. */
  public static <T> UnmodifiableIterator<T> unmodifiableIterator(final Iterator<T> iterator) {
    checkNotNull(iterator);
    if (iterator instanceof UnmodifiableIterator) {
      return (UnmodifiableIterator<T>) iterator;
    }
    return new UnmodifiableIterator<T>() {
      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public T next() {
        return iterator.next();
      }
    };
  }

  /**
   * Returns the number of elements remaining in {@code iterator}. The iterator
   * will be left exhausted: its {@code hasNext()} method will return
   * {@code false}.
   */
  // miniguava: reduced visibility from public.
  static int size(Iterator<?> iterator) {
    int count = 0;
    while (iterator.hasNext()) {
      iterator.next();
      count++;
    }
    return count;
  }

  /**
   * Traverses an iterator and removes every element that belongs to the
   * provided collection. The iterator will be left exhausted: its
   * {@code hasNext()} method will return {@code false}.
   *
   * @param removeFrom the iterator to (potentially) remove elements from
   * @param elementsToRemove the elements to remove
   * @return {@code true} if any element was removed from {@code iterator}
   */
  // miniguava: reduced visibility from public.
  static boolean removeAll(Iterator<?> removeFrom, Collection<?> elementsToRemove) {
    return removeIf(removeFrom, in(elementsToRemove));
  }

  /**
   * Removes every element that satisfies the provided predicate from the
   * iterator. The iterator will be left exhausted: its {@code hasNext()}
   * method will return {@code false}.
   *
   * @param removeFrom the iterator to (potentially) remove elements from
   * @param predicate a predicate that determines whether an element should
   *     be removed
   * @return {@code true} if any elements were removed from the iterator
   * @since 2.0
   */
  // miniguava: reduced visibility from public.
  @VisibleForTesting // miniguava
  static <T> boolean removeIf(Iterator<T> removeFrom, Predicate<? super T> predicate) {
    checkNotNull(predicate);
    boolean modified = false;
    while (removeFrom.hasNext()) {
      if (predicate.apply(removeFrom.next())) {
        removeFrom.remove();
        modified = true;
      }
    }
    return modified;
  }

  /**
   * Adds all elements in {@code iterator} to {@code collection}. The iterator
   * will be left exhausted: its {@code hasNext()} method will return
   * {@code false}.
   *
   * @return {@code true} if {@code collection} was modified as a result of this
   *         operation
   */
  // miniguava: Reduced visibility from public.
  static <T> boolean addAll(Collection<T> addTo, Iterator<? extends T> iterator) {
    checkNotNull(addTo);
    checkNotNull(iterator);
    boolean wasModified = false;
    while (iterator.hasNext()) {
      wasModified |= addTo.add(iterator.next());
    }
    return wasModified;
  }

  /**
   * Combines two iterators into a single iterator. The returned iterator
   * iterates across the elements in {@code a}, followed by the elements in
   * {@code b}. The source iterators are not polled until necessary.
   *
   * <p>The returned iterator supports {@code remove()} when the corresponding
   * input iterator supports it.
   *
   * <p><b>Note:</b> the current implementation is not suitable for nested
   * concatenated iterators, i.e. the following should be avoided when in a loop:
   * {@code iterator = Iterators.concat(iterator, suffix);}, since iteration over the
   * resulting iterator has a cubic complexity to the depth of the nesting.
   */
  // miniguava: reduced visibility from public.
  static <T> Iterator<T> concat(Iterator<? extends T> a, Iterator<? extends T> b) {
    checkNotNull(a);
    checkNotNull(b);
    return concat(new ConsumingQueueIterator<Iterator<? extends T>>(a, b));
  }

  /**
   * Combines multiple iterators into a single iterator. The returned iterator
   * iterates across the elements of each iterator in {@code inputs}. The input
   * iterators are not polled until necessary.
   *
   * <p>The returned iterator supports {@code remove()} when the corresponding
   * input iterator supports it.
   *
   * <p><b>Note:</b> the current implementation is not suitable for nested
   * concatenated iterators, i.e. the following should be avoided when in a loop:
   * {@code iterator = Iterators.concat(iterator, suffix);}, since iteration over the
   * resulting iterator has a cubic complexity to the depth of the nesting.
   *
   * @throws NullPointerException if any of the provided iterators is null
   */
  @VisibleForTesting // miniguava: Kept only for test code, reduced visibility from public.
  static <T> Iterator<T> concat(Iterator<? extends T>... inputs) {
    for (Iterator<? extends T> input : checkNotNull(inputs)) {
      checkNotNull(input);
    }
    return concat(new ConsumingQueueIterator<Iterator<? extends T>>(inputs));
  }

  /**
   * Combines multiple iterators into a single iterator. The returned iterator
   * iterates across the elements of each iterator in {@code inputs}. The input
   * iterators are not polled until necessary.
   *
   * <p>The returned iterator supports {@code remove()} when the corresponding
   * input iterator supports it. The methods of the returned iterator may throw
   * {@code NullPointerException} if any of the input iterators is null.
   *
   * <p><b>Note:</b> the current implementation is not suitable for nested
   * concatenated iterators, i.e. the following should be avoided when in a loop:
   * {@code iterator = Iterators.concat(iterator, suffix);}, since iteration over the
   * resulting iterator has a cubic complexity to the depth of the nesting.
   */
  // miniguava: reduced visibility from public.
  static <T> Iterator<T> concat(final Iterator<? extends Iterator<? extends T>> inputs) {
    checkNotNull(inputs);
    return new Iterator<T>() {
      Iterator<? extends T> current = emptyIterator();
      Iterator<? extends T> removeFrom;

      @Override
      public boolean hasNext() {
        // http://code.google.com/p/google-collections/issues/detail?id=151
        // current.hasNext() might be relatively expensive, worth minimizing.
        boolean currentHasNext;
        // checkNotNull eager for GWT
        // note: it must be here & not where 'current' is assigned,
        // because otherwise we'll have called inputs.next() before throwing
        // the first NPE, and the next time around we'll call inputs.next()
        // again, incorrectly moving beyond the error.
        while (!(currentHasNext = checkNotNull(current).hasNext()) && inputs.hasNext()) {
          current = inputs.next();
        }
        return currentHasNext;
      }

      @Override
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        removeFrom = current;
        return current.next();
      }

      @Override
      public void remove() {
        checkRemove(removeFrom != null);
        removeFrom.remove();
        removeFrom = null;
      }
    };
  }

  /**
   * Returns the elements of {@code unfiltered} that satisfy a predicate.
   */
  // miniguava: reduced visibility from public.
  @CheckReturnValue
  static <T> UnmodifiableIterator<T> filter(
      final Iterator<T> unfiltered, final Predicate<? super T> predicate) {
    checkNotNull(unfiltered);
    checkNotNull(predicate);
    return new AbstractIterator<T>() {
      @Override
      protected T computeNext() {
        while (unfiltered.hasNext()) {
          T element = unfiltered.next();
          if (predicate.apply(element)) {
            return element;
          }
        }
        return endOfData();
      }
    };
  }

  /**
   * Returns an iterator that applies {@code function} to each element of {@code
   * fromIterator}.
   *
   * <p>The returned iterator supports {@code remove()} if the provided iterator
   * does. After a successful {@code remove()} call, {@code fromIterator} no
   * longer contains the corresponding element.
   */
  // miniguava: Reduced visibility from public.
  static <F, T> Iterator<T> transform(
    final Iterator<F> fromIterator, final Function<? super F, ? extends T> function) {
    checkNotNull(function);
    return new TransformedIterator<F, T>(fromIterator) {
      @Override
      T transform(F from) {
        return function.apply(from);
      }
    };
  }

  // Methods only in Iterators, not in Iterables

  /**
   * Clears the iterator using its remove method.
   */
  static void clear(Iterator<?> iterator) {
    checkNotNull(iterator);
    while (iterator.hasNext()) {
      iterator.next();
      iterator.remove();
    }
  }

  /**
   * Used to avoid http://bugs.sun.com/view_bug.do?bug_id=6558557
   */
  static <T> ListIterator<T> cast(Iterator<T> iterator) {
    return (ListIterator<T>) iterator;
  }
}
