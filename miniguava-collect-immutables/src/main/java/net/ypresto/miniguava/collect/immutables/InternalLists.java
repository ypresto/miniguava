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

import static net.ypresto.miniguava.base.Preconditions.checkNotNull;

import net.ypresto.miniguava.annotations.MiniGuavaSpecific;
import net.ypresto.miniguava.base.Objects;

import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import javax.annotation.Nullable;

// miniguava: A collection of methods from Lists.java for internal use.
@MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "Lists")
class InternalLists {
  private InternalLists() {}

  /**
   * An implementation of {@link List#equals(Object)}.
   */
  static boolean equalsImpl(List<?> thisList, @Nullable Object other) {
    if (other == checkNotNull(thisList)) {
      return true;
    }
    if (!(other instanceof List)) {
      return false;
    }
    List<?> otherList = (List<?>) other;
    int size = thisList.size();
    if (size != otherList.size()) {
      return false;
    }
    if (thisList instanceof RandomAccess && otherList instanceof RandomAccess) {
      // avoid allocation and use the faster loop
      for (int i = 0; i < size; i++) {
        if (!Objects.equal(thisList.get(i), otherList.get(i))) {
          return false;
        }
      }
      return true;
    } else {
      return InternalIterators.elementsEqual(thisList.iterator(), otherList.iterator());
    }
  }

  /**
   * An implementation of {@link List#indexOf(Object)}.
   */
  static int indexOfImpl(List<?> list, @Nullable Object element) {
    if (list instanceof RandomAccess) {
      return indexOfRandomAccess(list, element);
    } else {
      ListIterator<?> listIterator = list.listIterator();
      while (listIterator.hasNext()) {
        if (Objects.equal(element, listIterator.next())) {
          return listIterator.previousIndex();
        }
      }
      return -1;
    }
  }

  private static int indexOfRandomAccess(List<?> list, @Nullable Object element) {
    int size = list.size();
    if (element == null) {
      for (int i = 0; i < size; i++) {
        if (list.get(i) == null) {
          return i;
        }
      }
    } else {
      for (int i = 0; i < size; i++) {
        if (element.equals(list.get(i))) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * An implementation of {@link List#lastIndexOf(Object)}.
   */
  static int lastIndexOfImpl(List<?> list, @Nullable Object element) {
    if (list instanceof RandomAccess) {
      return lastIndexOfRandomAccess(list, element);
    } else {
      ListIterator<?> listIterator = list.listIterator(list.size());
      while (listIterator.hasPrevious()) {
        if (Objects.equal(element, listIterator.previous())) {
          return listIterator.nextIndex();
        }
      }
      return -1;
    }
  }

  private static int lastIndexOfRandomAccess(List<?> list, @Nullable Object element) {
    if (element == null) {
      for (int i = list.size() - 1; i >= 0; i--) {
        if (list.get(i) == null) {
          return i;
        }
      }
    } else {
      for (int i = list.size() - 1; i >= 0; i--) {
        if (element.equals(list.get(i))) {
          return i;
        }
      }
    }
    return -1;
  }
}
