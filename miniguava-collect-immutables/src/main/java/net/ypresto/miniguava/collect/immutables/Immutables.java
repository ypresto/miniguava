/*
 * Copyright (C) 2016 The Guava Authors
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

import net.ypresto.miniguava.annotations.Beta;
import net.ypresto.miniguava.annotations.MiniGuavaSpecific;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

/**
 * This is Mini Guava specific class consists of immutableXX() methods
 * moved from correct package.
 */
@MiniGuavaSpecific
public class Immutables {
  /**
   * Returns an immutable map instance containing the given entries.
   * Internally, the returned map will be backed by an {@link EnumMap}.
   *
   * <p>The iteration order of the returned map follows the enum's iteration
   * order, not the order in which the elements appear in the given map.
   *
   * @param map the map to make an immutable copy of
   * @return an immutable map containing those entries
   * @since 14.0
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "collect.Maps")
  @Beta
  public static <K extends Enum<K>, V> ImmutableMap<K, V> immutableEnumMap(
      Map<K, ? extends V> map) {
    if (map instanceof ImmutableEnumMap) {
      @SuppressWarnings("unchecked") // safe covariant cast
      ImmutableEnumMap<K, V> result = (ImmutableEnumMap<K, V>) map;
      return result;
    } else if (map.isEmpty()) {
      return ImmutableMap.of();
    } else {
      for (Map.Entry<K, ? extends V> entry : map.entrySet()) {
        checkNotNull(entry.getKey());
        checkNotNull(entry.getValue());
      }
      return ImmutableEnumMap.asImmutable(new EnumMap<K, V>(map));
    }
  }

  /**
   * Returns an immutable set instance containing the given enum elements.
   * Internally, the returned set will be backed by an {@link EnumSet}.
   *
   * <p>The iteration order of the returned set follows the enum's iteration
   * order, not the order in which the elements appear in the given collection.
   *
   * @param elements the elements, all of the same {@code enum} type, that the
   *     set should contain
   * @return an immutable set containing those elements, minus duplicates
   */
  // http://code.google.com/p/google-web-toolkit/issues/detail?id=3028
  // miniguava: Rewritten for Collection instead of Iterable.
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "collect.Sets")
  public static <E extends Enum<E>> ImmutableSet<E> immutableEnumSet(Collection<E> elements) {
    if (elements instanceof ImmutableEnumSet) {
      return (ImmutableEnumSet<E>) elements;
    } else {
      if (elements.isEmpty()) {
        return ImmutableSet.of();
      } else {
        return ImmutableEnumSet.asImmutable(EnumSet.copyOf(elements));
      }
    }
  }

  /**
   * Returns an <b>immutable</b> list containing {@code elements} sorted by this
   * ordering. The input is not modified.
   *
   * <p>This method does not discard
   * elements that are duplicates according to the comparator. The sort
   * performed is <i>stable</i>, meaning that such elements will appear in the
   * returned list in the same order they appeared in {@code elements}.
   *
   * <p><b>Performance note:</b> According to our
   * benchmarking
   * on Open JDK 7, this method is the most efficient way to make a sorted copy
   * of a collection.
   *
   * @throws NullPointerException if any of {@code elements} (or {@code
   *     elements} itself) is null
   * @since 3.0
   * @see net.ypresto.miniguava.collect.Lists#sortedCopy(Collection, Comparator)
   */
  // miniguava: Rewritten for Collection instead of Iterable.
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "collect.Ordering#immutableSortedCopy")
  public static <E> ImmutableList<E> immutableSortedCopyOfList(Collection<E> elements, Comparator<? super E> comparator) {
    @SuppressWarnings("unchecked") // we'll only ever have E's in here
        E[] array = (E[]) elements.toArray();
    for (E e : array) {
      checkNotNull(e);
    }
    Arrays.sort(array, comparator);
    return ImmutableList.asImmutableList(array);
  }
}
