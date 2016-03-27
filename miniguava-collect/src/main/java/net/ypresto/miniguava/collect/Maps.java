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

import net.ypresto.miniguava.annotations.Beta;
import net.ypresto.miniguava.base.Function;
import net.ypresto.miniguava.base.Joiner.MapJoiner;
import net.ypresto.miniguava.base.Objects;
import net.ypresto.miniguava.base.internal.InternalImmutableEmulation;
import net.ypresto.miniguava.collect.MapDifference.ValueDifference;
import net.ypresto.miniguava.collect.internal.AbstractMapEntry;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import javax.annotation.Nullable;

/**
 * Static utility methods pertaining to {@link Map} instances (including instances of
 * {@link SortedMap}, {@code BiMap}, etc.). Also see this class's counterparts
 * {@link Lists}, {@link Sets} and {@code Queues}.
 *
 * <p>See the Guava User Guide article on <a href=
 * "https://github.com/google/guava/wiki/CollectionUtilitiesExplained#maps">
 * {@code Maps}</a>.
 *
 * @author Kevin Bourrillion
 * @author Mike Bostock
 * @author Isaac Shum
 * @author Louis Wasserman
 * @since 2.0
 */
// miniguava: Removed newXX() methods.
// miniguava: Removed internal helper methods with package-private visibility.
// miniguava: Removed filter family methods as many operations on map view are inefficient. Please open issue if you want to use it.
// miniguava: Removed asMap() methods as I didn't find any use case of this method. Please open issue if you want to use it.
// miniguava: Removed toMap() and uniqueIndex() as they can be done with Java 8 stream API or its backport.
// miniguava: Removed fromProperties() as `Properties` is not used at least on Android. Please open issue if you want to use it.
// miniguava: Removed asConverter() as Converter is omitted from miniguava.
// miniguava: Removed BiMap and NavigableMap related methods.
public final class Maps {
  private Maps() {}

  private enum EntryFunction implements Function<Entry<?, ?>, Object> {
    KEY {
      @Override
      @Nullable
      public Object apply(Entry<?, ?> entry) {
        return entry.getKey();
      }
    },
    VALUE {
      @Override
      @Nullable
      public Object apply(Entry<?, ?> entry) {
        return entry.getValue();
      }
    };
  }

  @SuppressWarnings("unchecked")
  static <K> Function<Entry<K, ?>, K> keyFunction() {
    return (Function) EntryFunction.KEY;
  }

  @SuppressWarnings("unchecked")
  static <V> Function<Entry<?, V>, V> valueFunction() {
    return (Function) EntryFunction.VALUE;
  }

  static <K, V> Iterator<K> keyIterator(Iterator<Entry<K, V>> entryIterator) {
    return Iterators.transform(entryIterator, Maps.<K>keyFunction());
  }

  static <K, V> Iterator<V> valueIterator(Iterator<Entry<K, V>> entryIterator) {
    return Iterators.transform(entryIterator, Maps.<V>valueFunction());
  }

  /**
   * Creates a <i>mutable</i>, empty, insertion-ordered {@code LinkedHashMap}
   * instance.
   *
   * <p><b>Note:</b> if mutability is not required, use {@code
   * ImmutableMap#of()} instead.
   *
   * @return a new, empty {@code LinkedHashMap}
   */
  // miniguava: Reduced visibility from public and kept for internal use only.
  private static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
    return new LinkedHashMap<K, V>();
  }

  /**
   * Creates a <i>mutable</i>, empty {@code TreeMap} instance using the given
   * comparator.
   *
   * <p><b>Note:</b> if mutability is not required, use {@code
   * ImmutableSortedMap.orderedBy(comparator).build()} instead.
   *
   * @param comparator the comparator to sort the keys with
   * @return a new, empty {@code TreeMap}
   */
  // miniguava: Reduced visibility from public and kept for internal use only.
  private static <C, K extends C, V> TreeMap<K, V> newTreeMap(@Nullable Comparator<C> comparator) {
    // Ideally, the extra type parameter "C" shouldn't be necessary. It is a
    // work-around of a compiler type inference quirk that prevents the
    // following code from being compiled:
    // Comparator<Class<?>> comparator = null;
    // Map<Class<? extends Throwable>, String> map = newTreeMap(comparator);
    return new TreeMap<K, V>(comparator);
  }

  /**
   * Computes the difference between two maps. This difference is an immutable
   * snapshot of the state of the maps at the time this method is called. It
   * will never change, even if the maps change at a later time.
   *
   * <p>Since this method uses {@code HashMap} instances internally, the keys of
   * the supplied maps must be well-behaved with respect to
   * {@link Object#equals} and {@link Object#hashCode}.
   *
   * <p><b>Note:</b>If you only need to know whether two maps have the same
   * mappings, call {@code left.equals(right)} instead of this method.
   *
   * @param left the map to treat as the "left" map for purposes of comparison
   * @param right the map to treat as the "right" map for purposes of comparison
   * @return the difference between the two maps
   */
  @SuppressWarnings("unchecked")
  public static <K, V> MapDifference<K, V> difference(
      Map<? extends K, ? extends V> left, Map<? extends K, ? extends V> right) {
    if (left instanceof SortedMap) {
      SortedMap<K, ? extends V> sortedLeft = (SortedMap<K, ? extends V>) left;
      SortedMapDifference<K, V> result = difference(sortedLeft, right);
      return result;
    }
    return difference(left, right, Equivalence.equals());
  }

  /**
   * Computes the difference between two maps. This difference is an immutable
   * snapshot of the state of the maps at the time this method is called. It
   * will never change, even if the maps change at a later time.
   *
   * <p>Values are compared using a provided equivalence, in the case of
   * equality, the value on the 'left' is returned in the difference.
   *
   * <p>Since this method uses {@code HashMap} instances internally, the keys of
   * the supplied maps must be well-behaved with respect to
   * {@link Object#equals} and {@link Object#hashCode}.
   *
   * @param left the map to treat as the "left" map for purposes of comparison
   * @param right the map to treat as the "right" map for purposes of comparison
   * @param valueEquivalence the equivalence relationship to use to compare
   *    values
   * @return the difference between the two maps
   * @since 10.0
   */
  @Beta
  public static <K, V> MapDifference<K, V> difference(
      Map<? extends K, ? extends V> left,
      Map<? extends K, ? extends V> right,
      Equivalence<? super V> valueEquivalence) {
    checkNotNull(valueEquivalence);

    Map<K, V> onlyOnLeft = newLinkedHashMap();
    Map<K, V> onlyOnRight = new LinkedHashMap<K, V>(right); // will whittle it down
    Map<K, V> onBoth = newLinkedHashMap();
    Map<K, MapDifference.ValueDifference<V>> differences = newLinkedHashMap();
    doDifference(left, right, valueEquivalence, onlyOnLeft, onlyOnRight, onBoth, differences);
    return new MapDifferenceImpl<K, V>(onlyOnLeft, onlyOnRight, onBoth, differences);
  }

  private static <K, V> void doDifference(
      Map<? extends K, ? extends V> left,
      Map<? extends K, ? extends V> right,
      Equivalence<? super V> valueEquivalence,
      Map<K, V> onlyOnLeft,
      Map<K, V> onlyOnRight,
      Map<K, V> onBoth,
      Map<K, MapDifference.ValueDifference<V>> differences) {
    for (Entry<? extends K, ? extends V> entry : left.entrySet()) {
      K leftKey = entry.getKey();
      V leftValue = entry.getValue();
      if (right.containsKey(leftKey)) {
        V rightValue = onlyOnRight.remove(leftKey);
        if (valueEquivalence.equivalent(leftValue, rightValue)) {
          onBoth.put(leftKey, leftValue);
        } else {
          differences.put(leftKey, ValueDifferenceImpl.create(leftValue, rightValue));
        }
      } else {
        onlyOnLeft.put(leftKey, leftValue);
      }
    }
  }

  private static <K, V> Map<K, V> unmodifiableMap(Map<K, V> map) {
    if (map instanceof SortedMap) {
      return Collections.unmodifiableSortedMap((SortedMap<K, ? extends V>) map);
    } else {
      return Collections.unmodifiableMap(map);
    }
  }

  static class MapDifferenceImpl<K, V> implements MapDifference<K, V> {
    final Map<K, V> onlyOnLeft;
    final Map<K, V> onlyOnRight;
    final Map<K, V> onBoth;
    final Map<K, ValueDifference<V>> differences;

    MapDifferenceImpl(
        Map<K, V> onlyOnLeft,
        Map<K, V> onlyOnRight,
        Map<K, V> onBoth,
        Map<K, ValueDifference<V>> differences) {
      this.onlyOnLeft = unmodifiableMap(onlyOnLeft);
      this.onlyOnRight = unmodifiableMap(onlyOnRight);
      this.onBoth = unmodifiableMap(onBoth);
      this.differences = unmodifiableMap(differences);
    }

    @Override
    public boolean areEqual() {
      return onlyOnLeft.isEmpty() && onlyOnRight.isEmpty() && differences.isEmpty();
    }

    @Override
    public Map<K, V> entriesOnlyOnLeft() {
      return onlyOnLeft;
    }

    @Override
    public Map<K, V> entriesOnlyOnRight() {
      return onlyOnRight;
    }

    @Override
    public Map<K, V> entriesInCommon() {
      return onBoth;
    }

    @Override
    public Map<K, ValueDifference<V>> entriesDiffering() {
      return differences;
    }

    @Override
    public boolean equals(Object object) {
      if (object == this) {
        return true;
      }
      if (object instanceof MapDifference) {
        MapDifference<?, ?> other = (MapDifference<?, ?>) object;
        return entriesOnlyOnLeft().equals(other.entriesOnlyOnLeft())
            && entriesOnlyOnRight().equals(other.entriesOnlyOnRight())
            && entriesInCommon().equals(other.entriesInCommon())
            && entriesDiffering().equals(other.entriesDiffering());
      }
      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(
          entriesOnlyOnLeft(), entriesOnlyOnRight(), entriesInCommon(), entriesDiffering());
    }

    @Override
    public String toString() {
      if (areEqual()) {
        return "equal";
      }

      StringBuilder result = new StringBuilder("not equal");
      if (!onlyOnLeft.isEmpty()) {
        result.append(": only on left=").append(onlyOnLeft);
      }
      if (!onlyOnRight.isEmpty()) {
        result.append(": only on right=").append(onlyOnRight);
      }
      if (!differences.isEmpty()) {
        result.append(": value differences=").append(differences);
      }
      return result.toString();
    }
  }

  static class ValueDifferenceImpl<V> implements MapDifference.ValueDifference<V> {
    private final V left;
    private final V right;

    static <V> ValueDifference<V> create(@Nullable V left, @Nullable V right) {
      return new ValueDifferenceImpl<V>(left, right);
    }

    private ValueDifferenceImpl(@Nullable V left, @Nullable V right) {
      this.left = left;
      this.right = right;
    }

    @Override
    public V leftValue() {
      return left;
    }

    @Override
    public V rightValue() {
      return right;
    }

    @Override
    public boolean equals(@Nullable Object object) {
      if (object instanceof MapDifference.ValueDifference) {
        MapDifference.ValueDifference<?> that = (MapDifference.ValueDifference<?>) object;
        return Objects.equal(this.left, that.leftValue())
            && Objects.equal(this.right, that.rightValue());
      }
      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(left, right);
    }

    @Override
    public String toString() {
      return "(" + left + ", " + right + ")";
    }
  }

  /**
   * Computes the difference between two sorted maps, using the comparator of
   * the left map, or natural order if the left map uses the
   * natural ordering of its elements. This difference is an immutable snapshot
   * of the state of the maps at the time this method is called. It will never
   * change, even if the maps change at a later time.
   *
   * <p>Since this method uses {@code TreeMap} instances internally, the keys of
   * the right map must all compare as distinct according to the comparator
   * of the left map.
   *
   * <p><b>Note:</b>If you only need to know whether two sorted maps have the
   * same mappings, call {@code left.equals(right)} instead of this method.
   *
   * @param left the map to treat as the "left" map for purposes of comparison
   * @param right the map to treat as the "right" map for purposes of comparison
   * @return the difference between the two maps
   * @since 11.0
   */
  // miniguava: Modified not to use Ordering.natural(), by using new TreeMap((Comparator) null) constructor.
  public static <K, V> SortedMapDifference<K, V> difference(
      SortedMap<K, ? extends V> left, Map<? extends K, ? extends V> right) {
    checkNotNull(left);
    checkNotNull(right);
    Comparator<? super K> comparator = left.comparator(); // miniguava: changed to nullable.
    SortedMap<K, V> onlyOnLeft = Maps.newTreeMap(comparator);
    SortedMap<K, V> onlyOnRight = Maps.newTreeMap(comparator);
    onlyOnRight.putAll(right); // will whittle it down
    SortedMap<K, V> onBoth = Maps.newTreeMap(comparator);
    SortedMap<K, MapDifference.ValueDifference<V>> differences = Maps.newTreeMap(comparator);
    doDifference(left, right, Equivalence.equals(), onlyOnLeft, onlyOnRight, onBoth, differences);
    return new SortedMapDifferenceImpl<K, V>(onlyOnLeft, onlyOnRight, onBoth, differences);
  }

  static class SortedMapDifferenceImpl<K, V> extends MapDifferenceImpl<K, V>
      implements SortedMapDifference<K, V> {
    SortedMapDifferenceImpl(
        SortedMap<K, V> onlyOnLeft,
        SortedMap<K, V> onlyOnRight,
        SortedMap<K, V> onBoth,
        SortedMap<K, ValueDifference<V>> differences) {
      super(onlyOnLeft, onlyOnRight, onBoth, differences);
    }

    @Override
    public SortedMap<K, ValueDifference<V>> entriesDiffering() {
      return (SortedMap<K, ValueDifference<V>>) super.entriesDiffering();
    }

    @Override
    public SortedMap<K, V> entriesInCommon() {
      return (SortedMap<K, V>) super.entriesInCommon();
    }

    @Override
    public SortedMap<K, V> entriesOnlyOnLeft() {
      return (SortedMap<K, V>) super.entriesOnlyOnLeft();
    }

    @Override
    public SortedMap<K, V> entriesOnlyOnRight() {
      return (SortedMap<K, V>) super.entriesOnlyOnRight();
    }
  }

  /**
   * Returns a view of a map where each value is transformed by a function. All
   * other properties of the map, such as iteration order, are left intact. For
   * example, the code: <pre>   {@code
   *
   *   Map<String, Integer> map = ImmutableMap.of("a", 4, "b", 9);
   *   Function<Integer, Double> sqrt =
   *       new Function<Integer, Double>() {
   *         public Double apply(Integer in) {
   *           return Math.sqrt((int) in);
   *         }
   *       };
   *   Map<String, Double> transformed = Maps.transformValues(map, sqrt);
   *   System.out.println(transformed);}</pre>
   *
   * ... prints {@code {a=2.0, b=3.0}}.
   *
   * <p>Changes in the underlying map are reflected in this view. Conversely,
   * this view supports removal operations, and these are reflected in the
   * underlying map.
   *
   * <p>It's acceptable for the underlying map to contain null keys, and even
   * null values provided that the function is capable of accepting null input.
   * The transformed map might contain null values, if the function sometimes
   * gives a null result.
   *
   * <p>The returned map is not thread-safe or serializable, even if the
   * underlying map is.
   *
   * <p>The function is applied lazily, invoked when needed. This is necessary
   * for the returned map to be a view, but it means that the function will be
   * applied many times for bulk operations like {@link Map#containsValue} and
   * {@code Map.toString()}. For this to perform well, {@code function} should
   * be fast. To avoid lazy evaluation when the returned map doesn't need to be
   * a view, copy the returned map into a new map of your choosing.
   */
  public static <K, V1, V2> Map<K, V2> transformValues(
      Map<K, V1> fromMap, Function<? super V1, V2> function) {
    return transformEntries(fromMap, asEntryTransformer(function));
  }

  /**
   * Returns a view of a sorted map where each value is transformed by a
   * function. All other properties of the map, such as iteration order, are
   * left intact. For example, the code: <pre>   {@code
   *
   *   SortedMap<String, Integer> map = ImmutableSortedMap.of("a", 4, "b", 9);
   *   Function<Integer, Double> sqrt =
   *       new Function<Integer, Double>() {
   *         public Double apply(Integer in) {
   *           return Math.sqrt((int) in);
   *         }
   *       };
   *   SortedMap<String, Double> transformed =
   *        Maps.transformValues(map, sqrt);
   *   System.out.println(transformed);}</pre>
   *
   * ... prints {@code {a=2.0, b=3.0}}.
   *
   * <p>Changes in the underlying map are reflected in this view. Conversely,
   * this view supports removal operations, and these are reflected in the
   * underlying map.
   *
   * <p>It's acceptable for the underlying map to contain null keys, and even
   * null values provided that the function is capable of accepting null input.
   * The transformed map might contain null values, if the function sometimes
   * gives a null result.
   *
   * <p>The returned map is not thread-safe or serializable, even if the
   * underlying map is.
   *
   * <p>The function is applied lazily, invoked when needed. This is necessary
   * for the returned map to be a view, but it means that the function will be
   * applied many times for bulk operations like {@link Map#containsValue} and
   * {@code Map.toString()}. For this to perform well, {@code function} should
   * be fast. To avoid lazy evaluation when the returned map doesn't need to be
   * a view, copy the returned map into a new map of your choosing.
   *
   * @since 11.0
   */
  public static <K, V1, V2> SortedMap<K, V2> transformValues(
      SortedMap<K, V1> fromMap, Function<? super V1, V2> function) {
    return transformEntries(fromMap, asEntryTransformer(function));
  }

  /**
   * Returns a view of a map whose values are derived from the original map's
   * entries. In contrast to {@link #transformValues}, this method's
   * entry-transformation logic may depend on the key as well as the value.
   *
   * <p>All other properties of the transformed map, such as iteration order,
   * are left intact. For example, the code: <pre>   {@code
   *
   *   Map<String, Boolean> options =
   *       ImmutableMap.of("verbose", true, "sort", false);
   *   EntryTransformer<String, Boolean, String> flagPrefixer =
   *       new EntryTransformer<String, Boolean, String>() {
   *         public String transformEntry(String key, Boolean value) {
   *           return value ? key : "no" + key;
   *         }
   *       };
   *   Map<String, String> transformed =
   *       Maps.transformEntries(options, flagPrefixer);
   *   System.out.println(transformed);}</pre>
   *
   * ... prints {@code {verbose=verbose, sort=nosort}}.
   *
   * <p>Changes in the underlying map are reflected in this view. Conversely,
   * this view supports removal operations, and these are reflected in the
   * underlying map.
   *
   * <p>It's acceptable for the underlying map to contain null keys and null
   * values provided that the transformer is capable of accepting null inputs.
   * The transformed map might contain null values if the transformer sometimes
   * gives a null result.
   *
   * <p>The returned map is not thread-safe or serializable, even if the
   * underlying map is.
   *
   * <p>The transformer is applied lazily, invoked when needed. This is
   * necessary for the returned map to be a view, but it means that the
   * transformer will be applied many times for bulk operations like {@link
   * Map#containsValue} and {@link Object#toString}. For this to perform well,
   * {@code transformer} should be fast. To avoid lazy evaluation when the
   * returned map doesn't need to be a view, copy the returned map into a new
   * map of your choosing.
   *
   * <p><b>Warning:</b> This method assumes that for any instance {@code k} of
   * {@code EntryTransformer} key type {@code K}, {@code k.equals(k2)} implies
   * that {@code k2} is also of type {@code K}. Using an {@code
   * EntryTransformer} key type for which this may not hold, such as {@code
   * ArrayList}, may risk a {@code ClassCastException} when calling methods on
   * the transformed map.
   *
   * @since 7.0
   */
  public static <K, V1, V2> Map<K, V2> transformEntries(
      Map<K, V1> fromMap, EntryTransformer<? super K, ? super V1, V2> transformer) {
    if (fromMap instanceof SortedMap) {
      return transformEntries((SortedMap<K, V1>) fromMap, transformer);
    }
    return new TransformedEntriesMap<K, V1, V2>(fromMap, transformer);
  }

  /**
   * Returns a view of a sorted map whose values are derived from the original
   * sorted map's entries. In contrast to {@link #transformValues}, this
   * method's entry-transformation logic may depend on the key as well as the
   * value.
   *
   * <p>All other properties of the transformed map, such as iteration order,
   * are left intact. For example, the code: <pre>   {@code
   *
   *   Map<String, Boolean> options =
   *       ImmutableSortedMap.of("verbose", true, "sort", false);
   *   EntryTransformer<String, Boolean, String> flagPrefixer =
   *       new EntryTransformer<String, Boolean, String>() {
   *         public String transformEntry(String key, Boolean value) {
   *           return value ? key : "yes" + key;
   *         }
   *       };
   *   SortedMap<String, String> transformed =
   *       Maps.transformEntries(options, flagPrefixer);
   *   System.out.println(transformed);}</pre>
   *
   * ... prints {@code {sort=yessort, verbose=verbose}}.
   *
   * <p>Changes in the underlying map are reflected in this view. Conversely,
   * this view supports removal operations, and these are reflected in the
   * underlying map.
   *
   * <p>It's acceptable for the underlying map to contain null keys and null
   * values provided that the transformer is capable of accepting null inputs.
   * The transformed map might contain null values if the transformer sometimes
   * gives a null result.
   *
   * <p>The returned map is not thread-safe or serializable, even if the
   * underlying map is.
   *
   * <p>The transformer is applied lazily, invoked when needed. This is
   * necessary for the returned map to be a view, but it means that the
   * transformer will be applied many times for bulk operations like {@link
   * Map#containsValue} and {@link Object#toString}. For this to perform well,
   * {@code transformer} should be fast. To avoid lazy evaluation when the
   * returned map doesn't need to be a view, copy the returned map into a new
   * map of your choosing.
   *
   * <p><b>Warning:</b> This method assumes that for any instance {@code k} of
   * {@code EntryTransformer} key type {@code K}, {@code k.equals(k2)} implies
   * that {@code k2} is also of type {@code K}. Using an {@code
   * EntryTransformer} key type for which this may not hold, such as {@code
   * ArrayList}, may risk a {@code ClassCastException} when calling methods on
   * the transformed map.
   *
   * miniguava: Note that it does not returns NavigableMap even when {@code fromMap} is
   * NavigableMap, in contrast of google guava.
   *
   * @since 11.0
   */
  // miniguava: Inlined Platform.mapsTransformEntriesSortedMap() and dropped NavigableMap support.
  public static <K, V1, V2> SortedMap<K, V2> transformEntries(
      SortedMap<K, V1> fromMap, EntryTransformer<? super K, ? super V1, V2> transformer) {
    return transformEntriesIgnoreNavigable(fromMap, transformer);
  }

  static <K, V1, V2> SortedMap<K, V2> transformEntriesIgnoreNavigable(
    SortedMap<K, V1> fromMap, EntryTransformer<? super K, ? super V1, V2> transformer) {
    return new TransformedEntriesSortedMap<K, V1, V2>(fromMap, transformer);
  }

  /**
   * A transformation of the value of a key-value pair, using both key and value
   * as inputs. To apply the transformation to a map, use
   * {@link Maps#transformEntries(Map, EntryTransformer)}.
   *
   * @param <K> the key type of the input and output entries
   * @param <V1> the value type of the input entry
   * @param <V2> the value type of the output entry
   * @since 7.0
   */
  public interface EntryTransformer<K, V1, V2> {
    /**
     * Determines an output value based on a key-value pair. This method is
     * <i>generally expected</i>, but not absolutely required, to have the
     * following properties:
     *
     * <ul>
     * <li>Its execution does not cause any observable side effects.
     * <li>The computation is <i>consistent with equals</i>; that is,
     *     {@link Objects#equal Objects.equal}{@code (k1, k2) &&}
     *     {@link Objects#equal}{@code (v1, v2)} implies that {@code
     *     Objects.equal(transformer.transform(k1, v1),
     *     transformer.transform(k2, v2))}.
     * </ul>
     *
     * @throws NullPointerException if the key or value is null and this
     *     transformer does not accept null arguments
     */
    V2 transformEntry(@Nullable K key, @Nullable V1 value);
  }

  /**
   * Views a function as an entry transformer that ignores the entry key.
   */
  static <K, V1, V2> EntryTransformer<K, V1, V2> asEntryTransformer(
      final Function<? super V1, V2> function) {
    checkNotNull(function);
    return new EntryTransformer<K, V1, V2>() {
      @Override
      public V2 transformEntry(K key, V1 value) {
        return function.apply(value);
      }
    };
  }

  static <K, V1, V2> Function<V1, V2> asValueToValueFunction(
      final EntryTransformer<? super K, V1, V2> transformer, final K key) {
    checkNotNull(transformer);
    return new Function<V1, V2>() {
      @Override
      public V2 apply(@Nullable V1 v1) {
        return transformer.transformEntry(key, v1);
      }
    };
  }

  /**
   * Views an entry transformer as a function from {@code Entry} to values.
   */
  static <K, V1, V2> Function<Entry<K, V1>, V2> asEntryToValueFunction(
      final EntryTransformer<? super K, ? super V1, V2> transformer) {
    checkNotNull(transformer);
    return new Function<Entry<K, V1>, V2>() {
      @Override
      public V2 apply(Entry<K, V1> entry) {
        return transformer.transformEntry(entry.getKey(), entry.getValue());
      }
    };
  }

  /**
   * Returns a view of an entry transformed by the specified transformer.
   */
  static <V2, K, V1> Entry<K, V2> transformEntry(
      final EntryTransformer<? super K, ? super V1, V2> transformer, final Entry<K, V1> entry) {
    checkNotNull(transformer);
    checkNotNull(entry);
    return new AbstractMapEntry<K, V2>() {
      @Override
      public K getKey() {
        return entry.getKey();
      }

      @Override
      public V2 getValue() {
        return transformer.transformEntry(entry.getKey(), entry.getValue());
      }
    };
  }

  /**
   * Views an entry transformer as a function from entries to entries.
   */
  static <K, V1, V2> Function<Entry<K, V1>, Entry<K, V2>> asEntryToEntryFunction(
      final EntryTransformer<? super K, ? super V1, V2> transformer) {
    checkNotNull(transformer);
    return new Function<Entry<K, V1>, Entry<K, V2>>() {
      @Override
      public Entry<K, V2> apply(final Entry<K, V1> entry) {
        return transformEntry(transformer, entry);
      }
    };
  }

  static class TransformedEntriesMap<K, V1, V2> extends IteratorBasedAbstractMap<K, V2> {
    final Map<K, V1> fromMap;
    final EntryTransformer<? super K, ? super V1, V2> transformer;

    TransformedEntriesMap(
        Map<K, V1> fromMap, EntryTransformer<? super K, ? super V1, V2> transformer) {
      this.fromMap = checkNotNull(fromMap);
      this.transformer = checkNotNull(transformer);
    }

    @Override
    public int size() {
      return fromMap.size();
    }

    @Override
    public boolean containsKey(Object key) {
      return fromMap.containsKey(key);
    }

    // safe as long as the user followed the <b>Warning</b> in the javadoc
    @SuppressWarnings("unchecked")
    @Override
    public V2 get(Object key) {
      V1 value = fromMap.get(key);
      return (value != null || fromMap.containsKey(key))
          ? transformer.transformEntry((K) key, value)
          : null;
    }

    // safe as long as the user followed the <b>Warning</b> in the javadoc
    @SuppressWarnings("unchecked")
    @Override
    public V2 remove(Object key) {
      return fromMap.containsKey(key)
          ? transformer.transformEntry((K) key, fromMap.remove(key))
          : null;
    }

    @Override
    public void clear() {
      fromMap.clear();
    }

    @Override
    public Set<K> keySet() {
      return fromMap.keySet();
    }

    @Override
    Iterator<Entry<K, V2>> entryIterator() {
      return Iterators.transform(
          fromMap.entrySet().iterator(), Maps.<K, V1, V2>asEntryToEntryFunction(transformer));
    }

    @Override
    public Collection<V2> values() {
      return new Values<K, V2>(this);
    }
  }

  static class TransformedEntriesSortedMap<K, V1, V2> extends TransformedEntriesMap<K, V1, V2>
      implements SortedMap<K, V2> {

    protected SortedMap<K, V1> fromMap() {
      return (SortedMap<K, V1>) fromMap;
    }

    TransformedEntriesSortedMap(
        SortedMap<K, V1> fromMap, EntryTransformer<? super K, ? super V1, V2> transformer) {
      super(fromMap, transformer);
    }

    @Override
    public Comparator<? super K> comparator() {
      return fromMap().comparator();
    }

    @Override
    public K firstKey() {
      return fromMap().firstKey();
    }

    @Override
    public SortedMap<K, V2> headMap(K toKey) {
      return transformEntries(fromMap().headMap(toKey), transformer);
    }

    @Override
    public K lastKey() {
      return fromMap().lastKey();
    }

    @Override
    public SortedMap<K, V2> subMap(K fromKey, K toKey) {
      return transformEntries(fromMap().subMap(fromKey, toKey), transformer);
    }

    @Override
    public SortedMap<K, V2> tailMap(K fromKey) {
      return transformEntries(fromMap().tailMap(fromKey), transformer);
    }
  }

  /**
   * {@code AbstractMap} extension that makes it easy to cache customized keySet, values,
   * and entrySet views.
   */
  abstract static class ViewCachingAbstractMap<K, V> extends AbstractMap<K, V> {
    /**
     * Creates the entry set to be returned by {@link #entrySet()}. This method
     * is invoked at most once on a given map, at the time when {@code entrySet}
     * is first called.
     */
    abstract Set<Entry<K, V>> createEntrySet();

    private transient Set<Entry<K, V>> entrySet;

    @Override
    public Set<Entry<K, V>> entrySet() {
      Set<Entry<K, V>> result = entrySet;
      return (result == null) ? entrySet = createEntrySet() : result;
    }

    private transient Set<K> keySet;

    @Override
    public Set<K> keySet() {
      Set<K> result = keySet;
      return (result == null) ? keySet = createKeySet() : result;
    }

    Set<K> createKeySet() {
      return new KeySet<K, V>(this);
    }

    private transient Collection<V> values;

    @Override
    public Collection<V> values() {
      Collection<V> result = values;
      return (result == null) ? values = createValues() : result;
    }

    Collection<V> createValues() {
      return new Values<K, V>(this);
    }
  }

  abstract static class IteratorBasedAbstractMap<K, V> extends AbstractMap<K, V> {
    @Override
    public abstract int size();

    abstract Iterator<Entry<K, V>> entryIterator();

    @Override
    public Set<Entry<K, V>> entrySet() {
      return new EntrySet<K, V>() {
        @Override
        Map<K, V> map() {
          return IteratorBasedAbstractMap.this;
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
          return entryIterator();
        }
      };
    }

    @Override
    public void clear() {
      Iterators.clear(entryIterator());
    }
  }

  /**
   * Delegates to {@link Map#get}. Returns {@code null} on {@code
   * ClassCastException} and {@code NullPointerException}.
   */
  static <V> V safeGet(Map<?, V> map, @Nullable Object key) {
    checkNotNull(map);
    try {
      return map.get(key);
    } catch (ClassCastException e) {
      return null;
    } catch (NullPointerException e) {
      return null;
    }
  }

  static final MapJoiner STANDARD_JOINER = InternalUtils.STANDARD_JOINER.withKeyValueSeparator("=");

  /**
   * An implementation of {@link Map#toString}.
   */
  static String toStringImpl(Map<?, ?> map) {
    StringBuilder sb = InternalUtils.newStringBuilderForCollection(map.size()).append('{');
    STANDARD_JOINER.appendTo(sb, map);
    return sb.append('}').toString();
  }

  static class KeySet<K, V> extends Sets.ImprovedAbstractSet<K> {
    final Map<K, V> map;

    KeySet(Map<K, V> map) {
      this.map = checkNotNull(map);
    }

    Map<K, V> map() {
      return map;
    }

    @Override
    public Iterator<K> iterator() {
      return keyIterator(map().entrySet().iterator());
    }

    @Override
    public int size() {
      return map().size();
    }

    @Override
    public boolean isEmpty() {
      return map().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
      return map().containsKey(o);
    }

    @Override
    public boolean remove(Object o) {
      if (contains(o)) {
        map().remove(o);
        return true;
      }
      return false;
    }

    @Override
    public void clear() {
      map().clear();
    }
  }

  static class SortedKeySet<K, V> extends KeySet<K, V> implements SortedSet<K> {
    SortedKeySet(SortedMap<K, V> map) {
      super(map);
    }

    @Override
    SortedMap<K, V> map() {
      return (SortedMap<K, V>) super.map();
    }

    @Override
    public Comparator<? super K> comparator() {
      return map().comparator();
    }

    @Override
    public SortedSet<K> subSet(K fromElement, K toElement) {
      return new SortedKeySet<K, V>(map().subMap(fromElement, toElement));
    }

    @Override
    public SortedSet<K> headSet(K toElement) {
      return new SortedKeySet<K, V>(map().headMap(toElement));
    }

    @Override
    public SortedSet<K> tailSet(K fromElement) {
      return new SortedKeySet<K, V>(map().tailMap(fromElement));
    }

    @Override
    public K first() {
      return map().firstKey();
    }

    @Override
    public K last() {
      return map().lastKey();
    }
  }

  static class Values<K, V> extends AbstractCollection<V> {
    final Map<K, V> map;

    Values(Map<K, V> map) {
      this.map = checkNotNull(map);
    }

    final Map<K, V> map() {
      return map;
    }

    @Override
    public Iterator<V> iterator() {
      return valueIterator(map().entrySet().iterator());
    }

    @Override
    public boolean remove(Object o) {
      try {
        return super.remove(o);
      } catch (UnsupportedOperationException e) {
        for (Entry<K, V> entry : map().entrySet()) {
          if (Objects.equal(o, entry.getValue())) {
            map().remove(entry.getKey());
            return true;
          }
        }
        return false;
      }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      try {
        return super.removeAll(checkNotNull(c));
      } catch (UnsupportedOperationException e) {
        Set<K> toRemove = new HashSet<K>();
        for (Entry<K, V> entry : map().entrySet()) {
          if (c.contains(entry.getValue())) {
            toRemove.add(entry.getKey());
          }
        }
        return map().keySet().removeAll(toRemove);
      }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      try {
        return super.retainAll(checkNotNull(c));
      } catch (UnsupportedOperationException e) {
        Set<K> toRetain = new HashSet<K>();
        for (Entry<K, V> entry : map().entrySet()) {
          if (c.contains(entry.getValue())) {
            toRetain.add(entry.getKey());
          }
        }
        return map().keySet().retainAll(toRetain);
      }
    }

    @Override
    public int size() {
      return map().size();
    }

    @Override
    public boolean isEmpty() {
      return map().isEmpty();
    }

    @Override
    public boolean contains(@Nullable Object o) {
      return map().containsValue(o);
    }

    @Override
    public void clear() {
      map().clear();
    }
  }

  abstract static class EntrySet<K, V> extends Sets.ImprovedAbstractSet<Entry<K, V>> {
    abstract Map<K, V> map();

    @Override
    public int size() {
      return map().size();
    }

    @Override
    public void clear() {
      map().clear();
    }

    @Override
    public boolean contains(Object o) {
      if (o instanceof Entry) {
        Entry<?, ?> entry = (Entry<?, ?>) o;
        Object key = entry.getKey();
        V value = Maps.safeGet(map(), key);
        return Objects.equal(value, entry.getValue()) && (value != null || map().containsKey(key));
      }
      return false;
    }

    @Override
    public boolean isEmpty() {
      return map().isEmpty();
    }

    @Override
    public boolean remove(Object o) {
      if (contains(o)) {
        Entry<?, ?> entry = (Entry<?, ?>) o;
        return map().keySet().remove(entry.getKey());
      }
      return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      try {
        return super.removeAll(checkNotNull(c));
      } catch (UnsupportedOperationException e) {
        // if the iterators don't support remove
        return Sets.removeAllImpl(this, c.iterator());
      }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      try {
        return super.retainAll(checkNotNull(c));
      } catch (UnsupportedOperationException e) {
        // if the iterators don't support remove
        Set<Object> keys = new HashSet<Object>(c.size());
        for (Object o : c) {
          if (contains(o)) {
            Entry<?, ?> entry = (Entry<?, ?>) o;
            keys.add(entry.getKey());
          }
        }
        return map().keySet().retainAll(keys);
      }
    }
  }

  /**
   * Returns a map from the ith element of list to i.
   */
  // miniguava: Please update compare.InternalUtils.indexMap too.
  static <E> Map<E, Integer> indexMap(Collection<E> list) {
    InternalImmutableEmulation.checkElementsNotNull(list);
    Map<E, Integer> map = new HashMap<E, Integer>(list.size());
    int i = 0;
    for (E e : list) {
      checkArgument(!map.containsKey(e), "Multiple elements with same value: %s", e);
      map.put(e, i++);
    }
    return Collections.unmodifiableMap(map);
  }
}
