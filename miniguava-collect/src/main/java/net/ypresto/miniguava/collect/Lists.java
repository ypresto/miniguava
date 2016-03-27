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
import static net.ypresto.miniguava.base.Preconditions.checkElementIndex;
import static net.ypresto.miniguava.base.Preconditions.checkNotNull;
import static net.ypresto.miniguava.base.Preconditions.checkPositionIndex;
import static net.ypresto.miniguava.base.Preconditions.checkPositionIndexes;
import static net.ypresto.miniguava.base.Preconditions.checkState;
import static net.ypresto.miniguava.collect.InternalUtils.checkRemove;

import net.ypresto.miniguava.annotations.MiniGuavaSpecific;
import net.ypresto.miniguava.base.Function;

import java.util.AbstractList;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

/**
 * Static utility methods pertaining to {@link List} instances. Also see this
 * class's counterparts {@link Sets}, {@link Maps} and {@code Queues}.
 *
 * <p>See the Guava User Guide article on <a href=
 * "https://github.com/google/guava/wiki/CollectionUtilitiesExplained#lists">
 * {@code Lists}</a>.
 *
 * @author Kevin Bourrillion
 * @author Mike Bostock
 * @author Louis Wasserman
 * @since 2.0
 */
// miniguava: Removed newXX() methods.
// miniguava: Removed internal helper methods with package-private visibility.
// miniguava: Removed charactersOf() methods as it should placed to anywhere else but this class.
// miniguava: Removed asList(E, E, E[]) method as I didn't find any reason to use this instead of asList(E, E[]). Please open issue if you want to use it.
// miniguava: Removed Serializable support.
public final class Lists {
  private Lists() {}

  /**
   * Returns an unmodifiable list containing the specified first element and
   * backed by the specified array of additional elements. Changes to the {@code
   * rest} array will be reflected in the returned list. Unlike {@link
   * Arrays#asList}, the returned list is unmodifiable.
   *
   * <p>This is useful when a varargs method needs to use a signature such as
   * {@code (Foo firstFoo, Foo... moreFoos)}, in order to avoid overload
   * ambiguity or to enforce a minimum argument count.
   *
   * <p>The returned list is NOT serializable and implements {@link RandomAccess}.
   *
   * @param first the first element
   * @param rest an array of additional elements, possibly empty
   * @return an unmodifiable list containing the specified elements
   */
  public static <E> List<E> asList(@Nullable E first, E[] rest) {
    return new OnePlusArrayList<E>(first, rest);
  }

  /** @see Lists#asList(Object, Object[]) */
  // miniguava: Removed Serializable
  private static class OnePlusArrayList<E> extends AbstractList<E> implements RandomAccess {
    final E first;
    final E[] rest;

    OnePlusArrayList(@Nullable E first, E[] rest) {
      this.first = first;
      this.rest = checkNotNull(rest);
    }

    @Override
    public int size() {
      return rest.length + 1;
    }

    @Override
    public E get(int index) {
      // check explicitly so the IOOBE will have the right message
      checkElementIndex(index, size());
      return (index == 0) ? first : rest[index - 1];
    }
  }

  /**
   * Returns every possible list that can be formed by choosing one element
   * from each of the given lists in order; the "n-ary
   * <a href="http://en.wikipedia.org/wiki/Cartesian_product">Cartesian
   * product</a>" of the lists. For example: <pre>   {@code
   *
   *   Lists.cartesianProduct(ImmutableList.of(
   *       ImmutableList.of(1, 2),
   *       ImmutableList.of("A", "B", "C")))}</pre>
   *
   * <p>returns a list containing six lists in the following order:
   *
   * <ul>
   * <li>{@code ImmutableList.of(1, "A")}
   * <li>{@code ImmutableList.of(1, "B")}
   * <li>{@code ImmutableList.of(1, "C")}
   * <li>{@code ImmutableList.of(2, "A")}
   * <li>{@code ImmutableList.of(2, "B")}
   * <li>{@code ImmutableList.of(2, "C")}
   * </ul>
   *
   * <p>The result is guaranteed to be in the "traditional", lexicographical
   * order for Cartesian products that you would get from nesting for loops:
   * <pre>   {@code
   *
   *   for (B b0 : lists.get(0)) {
   *     for (B b1 : lists.get(1)) {
   *       ...
   *       ImmutableList<B> tuple = ImmutableList.of(b0, b1, ...);
   *       // operate on tuple
   *     }
   *   }}</pre>
   *
   * <p>Note that if any input list is empty, the Cartesian product will also be
   * empty. If no lists at all are provided (an empty list), the resulting
   * Cartesian product has one element, an empty list (counter-intuitive, but
   * mathematically consistent).
   *
   * <p><i>Performance notes:</i> while the cartesian product of lists of size
   * {@code m, n, p} is a list of size {@code m x n x p}, its actual memory
   * consumption is much smaller. When the cartesian product is constructed, the
   * input lists are merely copied. Only as the resulting list is iterated are
   * the individual lists created, and these are not retained after iteration.
   *
   * @param lists the lists to choose elements from, in the order that
   *     the elements chosen from those lists should appear in the resulting
   *     lists
   * @param <B> any common base class shared by all axes (often just {@link
   *     Object})
   * @return the Cartesian product, as an immutable list containing immutable
   *     lists
   * @throws IllegalArgumentException if the size of the cartesian product would
   *     be greater than {@link Integer#MAX_VALUE}
   * @throws NullPointerException if {@code lists}, any one of the {@code lists},
   *     or any element of a provided list is null
   * @since 19.0
   */
  public static <B> List<List<B>> cartesianProduct(List<? extends List<? extends B>> lists) {
    return CartesianList.create(lists);
  }

  /**
   * Returns every possible list that can be formed by choosing one element
   * from each of the given lists in order; the "n-ary
   * <a href="http://en.wikipedia.org/wiki/Cartesian_product">Cartesian
   * product</a>" of the lists. For example: <pre>   {@code
   *
   *   Lists.cartesianProduct(ImmutableList.of(
   *       ImmutableList.of(1, 2),
   *       ImmutableList.of("A", "B", "C")))}</pre>
   *
   * <p>returns a list containing six lists in the following order:
   *
   * <ul>
   * <li>{@code ImmutableList.of(1, "A")}
   * <li>{@code ImmutableList.of(1, "B")}
   * <li>{@code ImmutableList.of(1, "C")}
   * <li>{@code ImmutableList.of(2, "A")}
   * <li>{@code ImmutableList.of(2, "B")}
   * <li>{@code ImmutableList.of(2, "C")}
   * </ul>
   *
   * <p>The result is guaranteed to be in the "traditional", lexicographical
   * order for Cartesian products that you would get from nesting for loops:
   * <pre>   {@code
   *
   *   for (B b0 : lists.get(0)) {
   *     for (B b1 : lists.get(1)) {
   *       ...
   *       ImmutableList<B> tuple = ImmutableList.of(b0, b1, ...);
   *       // operate on tuple
   *     }
   *   }}</pre>
   *
   * <p>Note that if any input list is empty, the Cartesian product will also be
   * empty. If no lists at all are provided (an empty list), the resulting
   * Cartesian product has one element, an empty list (counter-intuitive, but
   * mathematically consistent).
   *
   * <p><i>Performance notes:</i> while the cartesian product of lists of size
   * {@code m, n, p} is a list of size {@code m x n x p}, its actual memory
   * consumption is much smaller. When the cartesian product is constructed, the
   * input lists are merely copied. Only as the resulting list is iterated are
   * the individual lists created, and these are not retained after iteration.
   *
   * @param lists the lists to choose elements from, in the order that
   *     the elements chosen from those lists should appear in the resulting
   *     lists
   * @param <B> any common base class shared by all axes (often just {@link
   *     Object})
   * @return the Cartesian product, as an immutable list containing immutable
   *     lists
   * @throws IllegalArgumentException if the size of the cartesian product would
   *     be greater than {@link Integer#MAX_VALUE}
   * @throws NullPointerException if {@code lists}, any one of the
   *     {@code lists}, or any element of a provided list is null
   * @since 19.0
   */
  public static <B> List<List<B>> cartesianProduct(List<? extends B>... lists) {
    return cartesianProduct(Arrays.asList(lists));
  }

  /**
   * Returns a list that applies {@code function} to each element of {@code
   * fromList}. The returned list is a transformed view of {@code fromList};
   * changes to {@code fromList} will be reflected in the returned list and vice
   * versa.
   *
   * <p>Since functions are not reversible, the transform is one-way and new
   * items cannot be stored in the returned list. The {@code add},
   * {@code addAll} and {@code set} methods are unsupported in the returned
   * list.
   *
   * <p>The function is applied lazily, invoked when needed. This is necessary
   * for the returned list to be a view, but it means that the function will be
   * applied many times for bulk operations like {@link List#contains} and
   * {@link List#hashCode}. For this to perform well, {@code function} should be
   * fast. To avoid lazy evaluation when the returned list doesn't need to be a
   * view, copy the returned list into a new list of your choosing.
   *
   * <p>If {@code fromList} implements {@link RandomAccess}, so will the
   * returned list. The returned list is threadsafe if the supplied list and
   * function are.
   *
   * <p><b>Note:</b> serializing support is REMOVED in miniguava. Instead,
   * copy the list using {@code ImmutableList#copyOf(Collection)} (for example),
   * then serialize the copy. Other methods similar to this do not implement
   * serialization at all for this reason.
   */
  // miniguava: This method was dropped serialization support.
  @CheckReturnValue
  public static <F, T> List<T> transform(
      List<F> fromList, Function<? super F, ? extends T> function) {
    return (fromList instanceof RandomAccess)
        ? new TransformingRandomAccessList<F, T>(fromList, function)
        : new TransformingSequentialList<F, T>(fromList, function);
  }

  /**
   * Implementation of a sequential transforming list.
   *
   * @see Lists#transform
   */
  // miniguava: Removed Serializable
  private static class TransformingSequentialList<F, T> extends AbstractSequentialList<T> {
    final List<F> fromList;
    final Function<? super F, ? extends T> function;

    TransformingSequentialList(List<F> fromList, Function<? super F, ? extends T> function) {
      this.fromList = checkNotNull(fromList);
      this.function = checkNotNull(function);
    }
    /**
     * The default implementation inherited is based on iteration and removal of
     * each element which can be overkill. That's why we forward this call
     * directly to the backing list.
     */
    @Override
    public void clear() {
      fromList.clear();
    }

    @Override
    public int size() {
      return fromList.size();
    }

    @Override
    public ListIterator<T> listIterator(final int index) {
      return new TransformedListIterator<F, T>(fromList.listIterator(index)) {
        @Override
        T transform(F from) {
          return function.apply(from);
        }
      };
    }
  }

  /**
   * Implementation of a transforming random access list. We try to make as many
   * of these methods pass-through to the source list as possible so that the
   * performance characteristics of the source list and transformed list are
   * similar.
   *
   * @see Lists#transform
   */
  // miniguava: Removed Serializable
  private static class TransformingRandomAccessList<F, T> extends AbstractList<T> implements RandomAccess {
    final List<F> fromList;
    final Function<? super F, ? extends T> function;

    TransformingRandomAccessList(List<F> fromList, Function<? super F, ? extends T> function) {
      this.fromList = checkNotNull(fromList);
      this.function = checkNotNull(function);
    }

    @Override
    public void clear() {
      fromList.clear();
    }

    @Override
    public T get(int index) {
      return function.apply(fromList.get(index));
    }

    @Override
    public Iterator<T> iterator() {
      return listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
      return new TransformedListIterator<F, T>(fromList.listIterator(index)) {
        @Override
        T transform(F from) {
          return function.apply(from);
        }
      };
    }

    @Override
    public boolean isEmpty() {
      return fromList.isEmpty();
    }

    @Override
    public T remove(int index) {
      return function.apply(fromList.remove(index));
    }

    @Override
    public int size() {
      return fromList.size();
    }
  }

  /**
   * Returns consecutive {@linkplain List#subList(int, int) sublists} of a list,
   * each of the same size (the final list may be smaller). For example,
   * partitioning a list containing {@code [a, b, c, d, e]} with a partition
   * size of 3 yields {@code [[a, b, c], [d, e]]} -- an outer list containing
   * two inner lists of three and two elements, all in the original order.
   *
   * <p>The outer list is unmodifiable, but reflects the latest state of the
   * source list. The inner lists are sublist views of the original list,
   * produced on demand using {@link List#subList(int, int)}, and are subject
   * to all the usual caveats about modification as explained in that API.
   *
   * @param list the list to return consecutive sublists of
   * @param size the desired size of each sublist (the last may be
   *     smaller)
   * @return a list of consecutive sublists
   * @throws IllegalArgumentException if {@code partitionSize} is nonpositive
   */
  // miniguava: This method was dropped serialization support.
  public static <T> List<List<T>> partition(List<T> list, int size) {
    checkNotNull(list);
    checkArgument(size > 0);
    return (list instanceof RandomAccess)
        ? new RandomAccessPartition<T>(list, size)
        : new Partition<T>(list, size);
  }

  private static class Partition<T> extends AbstractList<List<T>> {
    final List<T> list;
    final int size;

    Partition(List<T> list, int size) {
      this.list = list;
      this.size = size;
    }

    @Override
    public List<T> get(int index) {
      checkElementIndex(index, size());
      int start = index * size;
      int end = Math.min(start + size, list.size());
      return list.subList(start, end);
    }

    @Override
    public int size() {
      // miniguava: Originally using IntMath.divide(), but double seems to having sufficient precision.
      return (int) Math.ceil((double) list.size() / size);
    }

    @Override
    public boolean isEmpty() {
      return list.isEmpty();
    }
  }

  private static class RandomAccessPartition<T> extends Partition<T> implements RandomAccess {
    RandomAccessPartition(List<T> list, int size) {
      super(list, size);
    }
  }

  /**
   * Returns a reversed view of the specified list. For example, {@code
   * Lists.reverse(Arrays.asList(1, 2, 3))} returns a list containing {@code 3,
   * 2, 1}. The returned list is backed by this list, so changes in the returned
   * list are reflected in this list, and vice-versa. The returned list supports
   * all of the optional list operations supported by this list.
   *
   * <p>The returned list is random-access if the specified list is random
   * access.
   *
   * @since 7.0
   */
  // miniguava: Removed ImmutableList check.
  @CheckReturnValue
  public static <T> List<T> reverse(List<T> list) {
    if (list instanceof ReverseList) {
      return ((ReverseList<T>) list).getForwardList();
    } else if (list instanceof RandomAccess) {
      return new RandomAccessReverseList<T>(list);
    } else {
      return new ReverseList<T>(list);
    }
  }

  private static class ReverseList<T> extends AbstractList<T> {
    private final List<T> forwardList;

    ReverseList(List<T> forwardList) {
      this.forwardList = checkNotNull(forwardList);
    }

    List<T> getForwardList() {
      return forwardList;
    }

    private int reverseIndex(int index) {
      int size = size();
      checkElementIndex(index, size);
      return (size - 1) - index;
    }

    private int reversePosition(int index) {
      int size = size();
      checkPositionIndex(index, size);
      return size - index;
    }

    @Override
    public void add(int index, @Nullable T element) {
      forwardList.add(reversePosition(index), element);
    }

    @Override
    public void clear() {
      forwardList.clear();
    }

    @Override
    public T remove(int index) {
      return forwardList.remove(reverseIndex(index));
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
      subList(fromIndex, toIndex).clear();
    }

    @Override
    public T set(int index, @Nullable T element) {
      return forwardList.set(reverseIndex(index), element);
    }

    @Override
    public T get(int index) {
      return forwardList.get(reverseIndex(index));
    }

    @Override
    public int size() {
      return forwardList.size();
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
      checkPositionIndexes(fromIndex, toIndex, size());
      return reverse(forwardList.subList(reversePosition(toIndex), reversePosition(fromIndex)));
    }

    @Override
    public Iterator<T> iterator() {
      return listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
      int start = reversePosition(index);
      final ListIterator<T> forwardIterator = forwardList.listIterator(start);
      return new ListIterator<T>() {

        boolean canRemoveOrSet;

        @Override
        public void add(T e) {
          forwardIterator.add(e);
          forwardIterator.previous();
          canRemoveOrSet = false;
        }

        @Override
        public boolean hasNext() {
          return forwardIterator.hasPrevious();
        }

        @Override
        public boolean hasPrevious() {
          return forwardIterator.hasNext();
        }

        @Override
        public T next() {
          if (!hasNext()) {
            throw new NoSuchElementException();
          }
          canRemoveOrSet = true;
          return forwardIterator.previous();
        }

        @Override
        public int nextIndex() {
          return reversePosition(forwardIterator.nextIndex());
        }

        @Override
        public T previous() {
          if (!hasPrevious()) {
            throw new NoSuchElementException();
          }
          canRemoveOrSet = true;
          return forwardIterator.next();
        }

        @Override
        public int previousIndex() {
          return nextIndex() - 1;
        }

        @Override
        public void remove() {
          checkRemove(canRemoveOrSet);
          forwardIterator.remove();
          canRemoveOrSet = false;
        }

        @Override
        public void set(T e) {
          checkState(canRemoveOrSet);
          forwardIterator.set(e);
        }
      };
    }
  }

  private static class RandomAccessReverseList<T> extends ReverseList<T> implements RandomAccess {
    RandomAccessReverseList(List<T> forwardList) {
      super(forwardList);
    }
  }

  /**
   * Returns a <b>mutable</b> list containing {@code elements} sorted by this
   * ordering; use this only when the resulting list may need further
   * modification, or may contain {@code null}. The input is not modified. The
   * returned list is serializable and has random access.
   *
   * <p>This method does not discard
   * elements that are duplicates according to the comparator. The sort
   * performed is <i>stable</i>, meaning that such elements will appear in the
   * returned list in the same order they appeared in {@code elements}.
   *
   * <p><b>Performance note:</b> According to our
   * benchmarking
   * on Open JDK 7, {@code Immutables#immutableSortedCopyOfList} generally performs better (in
   * both time and space) than this method, and this method in turn generally
   * performs better than copying the list and calling {@link
   * Collections#sort(List)}.
   *
   * @throws NullPointerException if {@code elements} or {@code comparator} is null
   */
  // miniguava: Moved from Ordering.java, rewritten for List instead of Iterable.
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "Ordering")
  public static <E> List<E> sortedCopy(Collection<E> elements, Comparator<? super E> comparator) {
    checkNotNull(comparator, "comparator");
    @SuppressWarnings("unchecked") // does not escape, and contains only E's
        E[] array = (E[]) elements.toArray();
    Arrays.sort(array, comparator);
    return new ArrayList<E>(Arrays.asList(array));
  }
}
