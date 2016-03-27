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

import net.ypresto.miniguava.annotations.MiniGuavaSpecific;
import net.ypresto.miniguava.base.Joiner;
import net.ypresto.miniguava.base.Joiner.MapJoiner;
import net.ypresto.miniguava.collect.UnmodifiableIterator;
import net.ypresto.miniguava.collect.internal.AbstractMapEntry;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nullable;

// miniguava: A collection of helper methods used in this module.
@MiniGuavaSpecific
class InternalUtils {
  private InternalUtils() {}

  /**
   * The largest power of two that can be represented as an {@code int}.
   *
   * @since 10.0
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "primitives.Ints")
  public static final int MAX_POWER_OF_TWO = 1 << (Integer.SIZE - 2);

  // miniguava: From Sets.java, original method name: hashCodeImpl
  /**
   * An implementation for {@link Set#hashCode()}.
   */
  static int setsHashCodeImpl(Set<?> s) {
    int hashCode = 0;
    for (Object o : s) {
      hashCode += o != null ? o.hashCode() : 0;

      hashCode = ~~hashCode;
      // Needed to deal with unusual integer overflow in GWT.
    }
    return hashCode;
  }

  /**
   * An implementation for {@link Set#equals(Object)}.
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "collect.Sets#equalsImpl")
  static boolean setsEqualsImpl(Set<?> s, @Nullable Object object) {
    if (s == object) {
      return true;
    }
    if (object instanceof Set) {
      Set<?> o = (Set<?>) object;

      try {
        return s.size() == o.size() && s.containsAll(o);
      } catch (NullPointerException ignored) {
        return false;
      } catch (ClassCastException ignored) {
        return false;
      }
    }
    return false;
  }

  /**
   * An implementation of {@link Map#equals}.
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "collect.Maps#equalsImpl")
  static boolean mapsEqualsImpl(Map<?, ?> map, Object object) {
    if (map == object) {
      return true;
    } else if (object instanceof Map) {
      Map<?, ?> o = (Map<?, ?>) object;
      return map.entrySet().equals(o.entrySet());
    }
    return false;
  }

  /**
   * Returns best-effort-sized StringBuilder based on the given collection size.
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "collect.Collections2")
  static StringBuilder newStringBuilderForCollection(int size) {
    checkNonnegative(size, "size");
    return new StringBuilder((int) Math.min(size * 8L, MAX_POWER_OF_TWO));
  }

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "collect.Collections2")
  static final Joiner STANDARD_JOINER = Joiner.on(", ").useForNull("null");

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "collect.Maps#STANDARD_JOINER")
  static final MapJoiner STANDARD_MAP_JOINER = STANDARD_JOINER.withKeyValueSeparator("=");

  /**
   * An implementation of {@link Map#toString}.
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "collect.Maps#toStringImpl")
  static String mapsToStringImpl(Map<?, ?> map) {
    StringBuilder sb = InternalUtils.newStringBuilderForCollection(map.size()).append('{');
    STANDARD_MAP_JOINER.appendTo(sb, map);
    return sb.append('}').toString();
  }

  /**
   * Returns an unmodifiable view of the specified map entry. The {@link
   * Map.Entry#setValue} operation throws an {@link UnsupportedOperationException}.
   * This also has the side-effect of redefining {@code equals} to comply with
   * the Entry contract, to avoid a possible nefarious implementation of equals.
   *
   * @param entry the entry for which to return an unmodifiable view
   * @return an unmodifiable view of the entry
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "collect.Maps")
  static <K, V> Map.Entry<K, V> unmodifiableEntry(final Map.Entry<? extends K, ? extends V> entry) {
    checkNotNull(entry);
    return new AbstractMapEntry<K, V>() {
      @Override
      public K getKey() {
        return entry.getKey();
      }

      @Override
      public V getValue() {
        return entry.getValue();
      }
    };
  }

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "collect.Maps")
  static <K, V> UnmodifiableIterator<Map.Entry<K, V>> unmodifiableEntryIterator(
      final Iterator<Map.Entry<K, V>> entryIterator) {
    return new UnmodifiableIterator<Map.Entry<K, V>>() {
      @Override
      public boolean hasNext() {
        return entryIterator.hasNext();
      }

      @Override
      public Map.Entry<K, V> next() {
        return unmodifiableEntry(entryIterator.next());
      }
    };
  }

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "collect.CollectPreconditions")
  static void checkEntryNotNull(Object key, Object value) {
    if (key == null) {
      throw new NullPointerException("null key in entry: null=" + value);
    } else if (value == null) {
      throw new NullPointerException("null value in entry: " + key + "=null");
    }
  }

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "collect.CollectPreconditions")
  static int checkNonnegative(int value, String name) {
    if (value < 0) {
      throw new IllegalArgumentException(name + " cannot be negative but was: " + value);
    }
    return value;
  }

  /**
   * Returns the single element contained in {@code iterable}.
   *
   * @throws NoSuchElementException if the iterable is empty
   * @throws IllegalArgumentException if the iterable contains multiple
   *     elements
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "collect.Iterables")
  public static <T> T getOnlyElement(Iterable<T> iterable) {
    return InternalIterators.getOnlyElement(iterable.iterator());
  }
}
