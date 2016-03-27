/*
 * Copyright (C) 2012 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package net.ypresto.miniguava.collect;

import static net.ypresto.miniguava.base.Preconditions.checkElementIndex;

import net.ypresto.miniguava.annotations.MiniGuavaSpecific;
import net.ypresto.miniguava.base.internal.InternalImmutableEmulation;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import javax.annotation.Nullable;

/**
 * Implementation of {@link Lists#cartesianProduct(List)}.
 *
 * @author Louis Wasserman
 */
final class CartesianList<E> extends AbstractList<List<E>> implements RandomAccess {

  private transient final List<List<E>> axes;
  private transient final int[] axesSizeProduct;

  // miniguava: Rewritten not to use ImmutableList.
  static <E> List<List<E>> create(List<? extends List<? extends E>> lists) {
    InternalImmutableEmulation.checkElementsNotNull(lists);
    List<List<E>> axes = new ArrayList<List<E>>(lists.size());
    for (List<? extends E> list : lists) {
      List<E> copy = InternalImmutableEmulation.immutableCopy(list);
      if (copy.isEmpty()) {
        return Collections.emptyList();
      }
      axes.add(copy);
    }
    return new CartesianList<E>(axes);
  }

  // miniguava: Decreased visibility from package-private, to ensure passed list will not be mutated.
  private CartesianList(List<List<E>> axes) {
    this.axes = axes;
    int[] axesSizeProduct = new int[axes.size() + 1];
    axesSizeProduct[axes.size()] = 1;
    try {
      for (int i = axes.size() - 1; i >= 0; i--) {
        axesSizeProduct[i] = InternalUtils.checkedMultiply(axesSizeProduct[i + 1], axes.get(i).size());
      }
    } catch (ArithmeticException e) {
      throw new IllegalArgumentException(
          "Cartesian product too large; must have size at most Integer.MAX_VALUE");
    }
    this.axesSizeProduct = axesSizeProduct;
  }

  private int getAxisIndexForProductIndex(int index, int axis) {
    return (index / axesSizeProduct[axis + 1]) % axes.get(axis).size();
  }

  @Override
  public List<E> get(final int index) {
    checkElementIndex(index, size());
    return new AbstractRandomAccessList<E>() {

      @Override
      public int size() {
        return axes.size();
      }

      @Override
      public E get(int axis) {
        checkElementIndex(axis, size());
        int axisIndex = getAxisIndexForProductIndex(index, axis);
        return axes.get(axis).get(axisIndex);
      }
    };
  }

  @MiniGuavaSpecific
  private abstract class AbstractRandomAccessList<E> extends AbstractList<E> implements RandomAccess {
  }

  @Override
  public int size() {
    return axesSizeProduct[0];
  }

  @Override
  public boolean contains(@Nullable Object o) {
    if (!(o instanceof List)) {
      return false;
    }
    List<?> list = (List<?>) o;
    if (list.size() != axes.size()) {
      return false;
    }
    ListIterator<?> itr = list.listIterator();
    while (itr.hasNext()) {
      int index = itr.nextIndex();
      if (!axes.get(index).contains(itr.next())) {
        return false;
      }
    }
    return true;
  }
}
