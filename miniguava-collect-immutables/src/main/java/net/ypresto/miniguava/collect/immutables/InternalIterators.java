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

package net.ypresto.miniguava.collect.immutables;

import static net.ypresto.miniguava.base.Preconditions.checkArgument;

import net.ypresto.miniguava.annotations.MiniGuavaSpecific;
import net.ypresto.miniguava.base.Objects;
import net.ypresto.miniguava.base.Preconditions;
import net.ypresto.miniguava.collect.Iterators;
import net.ypresto.miniguava.collect.UnmodifiableIterator;
import net.ypresto.miniguava.collect.UnmodifiableListIterator;
import net.ypresto.miniguava.collect.internal.AbstractIndexedListIterator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

// miniguava: A collection of methods from Iterators.java for internal use.
@MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "Iterators")
class InternalIterators {
  private InternalIterators() {}

  /**
   * Determines whether two iterators contain equal elements in the same order.
   * More specifically, this method returns {@code true} if {@code iterator1}
   * and {@code iterator2} contain the same number of elements and every element
   * of {@code iterator1} is equal to the corresponding element of
   * {@code iterator2}.
   *
   * <p>Note that this will modify the supplied iterators, since they will have
   * been advanced some number of elements forward.
   */
  public static boolean elementsEqual(Iterator<?> iterator1, Iterator<?> iterator2) {
    while (iterator1.hasNext()) {
      if (!iterator2.hasNext()) {
        return false;
      }
      Object o1 = iterator1.next();
      Object o2 = iterator2.next();
      if (!Objects.equal(o1, o2)) {
        return false;
      }
    }
    return !iterator2.hasNext();
  }

  /**
   * Returns the single element contained in {@code iterator}.
   *
   * @throws NoSuchElementException if the iterator is empty
   * @throws IllegalArgumentException if the iterator contains multiple
   *     elements.  The state of the iterator is unspecified.
   */
  public static <T> T getOnlyElement(Iterator<T> iterator) {
    T first = iterator.next();
    if (!iterator.hasNext()) {
      return first;
    }

    StringBuilder sb = new StringBuilder();
    sb.append("expected one element but was: <" + first);
    for (int i = 0; i < 4 && iterator.hasNext(); i++) {
      sb.append(", " + iterator.next());
    }
    if (iterator.hasNext()) {
      sb.append(", ...");
    }
    sb.append('>');

    throw new IllegalArgumentException(sb.toString());
  }

  /**
   * Returns an iterator containing the elements of {@code array} in order. The
   * returned iterator is a view of the array; subsequent changes to the array
   * will be reflected in the iterator.
   *
   * <p><b>Note:</b> It is often preferable to represent your data using a
   * collection type, for example using {@link Arrays#asList(Object[])}, making
   * this method unnecessary.
   *
   * <p>The {@code Iterable} equivalent of this method is either {@link
   * Arrays#asList(Object[])}, {@link ImmutableList#copyOf(Object[])}},
   * or {@link ImmutableList#of}.
   */
  public static <T> UnmodifiableIterator<T> forArray(final T... array) {
    return forArray(array, 0, array.length, 0);
  }

  /**
   * Returns a list iterator containing the elements in the specified range of
   * {@code array} in order, starting at the specified index.
   *
   * <p>The {@code Iterable} equivalent of this method is {@code
   * Arrays.asList(array).subList(offset, offset + length).listIterator(index)}.
   */
  static <T> UnmodifiableListIterator<T> forArray(
      final T[] array, final int offset, int length, int index) {
    checkArgument(length >= 0);
    int end = offset + length;

    // Technically we should give a slightly more descriptive error on overflow
    Preconditions.checkPositionIndexes(offset, end, array.length);
    Preconditions.checkPositionIndex(index, length);
    if (length == 0) {
      return Iterators.emptyListIterator();
    }

    /*
     * We can't use call the two-arg constructor with arguments (offset, end)
     * because the returned Iterator is a ListIterator that may be moved back
     * past the beginning of the iteration.
     */
    return new AbstractIndexedListIterator<T>(length, index) {
      @Override
      protected T get(int index) {
        return array[offset + index];
      }
    };
  }

  /**
   * Returns an iterator containing only {@code value}.
   *
   * <p>The {@link Iterable} equivalent of this method is {@link
   * Collections#singleton}.
   */
  public static <T> UnmodifiableIterator<T> singletonIterator(@Nullable final T value) {
    return new UnmodifiableIterator<T>() {
      boolean done;

      @Override
      public boolean hasNext() {
        return !done;
      }

      @Override
      public T next() {
        if (done) {
          throw new NoSuchElementException();
        }
        done = true;
        return value;
      }
    };
  }
}
