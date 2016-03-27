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

import static com.google.common.truth.Truth.assertThat;
import static net.ypresto.miniguava.collect.Maps.transformEntries;
import static net.ypresto.miniguava.collect.Maps.transformValues;

import com.google.common.collect.ForwardingSortedMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;

import junit.framework.TestCase;

import net.ypresto.miniguava.annotations.MiniGuavaSpecific;
import net.ypresto.miniguava.base.Function;
import net.ypresto.miniguava.collect.Maps.EntryTransformer;
import net.ypresto.miniguava.collect.Maps.ValueDifferenceImpl;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nullable;

/**
 * Unit test for {@code Maps}.
 *
 * @author Kevin Bourrillion
 * @author Mike Bostock
 * @author Jared Levy
 */
public class MapsTest extends TestCase {

  public void testNullPointerExceptions() {
    new NullPointerTester()
      // miniguava: added Equivalence sample object.
      .setDefault(Equivalence.class, new Equivalence<Object>() {
        @Override
        protected boolean doEquivalent(Object a, Object b) {
          return false;
        }

        @Override
        protected int doHash(Object o) {
          return 0;
        }
      })
      .testAllPublicStaticMethods(Maps.class);
  }

  private static final Map<Integer, Integer> EMPTY
      = Collections.emptyMap();
  private static final Map<Integer, Integer> SINGLETON
      = Collections.singletonMap(1, 2);

  public void testMapDifferenceEmptyEmpty() {
    MapDifference<Integer, Integer> diff = Maps.difference(EMPTY, EMPTY);
    assertTrue(diff.areEqual());
    assertEquals(EMPTY, diff.entriesOnlyOnLeft());
    assertEquals(EMPTY, diff.entriesOnlyOnRight());
    assertEquals(EMPTY, diff.entriesInCommon());
    assertEquals(EMPTY, diff.entriesDiffering());
    assertEquals("equal", diff.toString());
  }

  public void testMapDifferenceEmptySingleton() {
    MapDifference<Integer, Integer> diff = Maps.difference(EMPTY, SINGLETON);
    assertFalse(diff.areEqual());
    assertEquals(EMPTY, diff.entriesOnlyOnLeft());
    assertEquals(SINGLETON, diff.entriesOnlyOnRight());
    assertEquals(EMPTY, diff.entriesInCommon());
    assertEquals(EMPTY, diff.entriesDiffering());
    assertEquals("not equal: only on right={1=2}", diff.toString());
  }

  public void testMapDifferenceSingletonEmpty() {
    MapDifference<Integer, Integer> diff = Maps.difference(SINGLETON, EMPTY);
    assertFalse(diff.areEqual());
    assertEquals(SINGLETON, diff.entriesOnlyOnLeft());
    assertEquals(EMPTY, diff.entriesOnlyOnRight());
    assertEquals(EMPTY, diff.entriesInCommon());
    assertEquals(EMPTY, diff.entriesDiffering());
    assertEquals("not equal: only on left={1=2}", diff.toString());
  }

  public void testMapDifferenceTypical() {
    Map<Integer, String> left = ImmutableMap.of(
        1, "a", 2, "b", 3, "c", 4, "d", 5, "e");
    Map<Integer, String> right = ImmutableMap.of(
        1, "a", 3, "f", 5, "g", 6, "z");

    MapDifference<Integer, String> diff1 = Maps.difference(left, right);
    assertFalse(diff1.areEqual());
    assertEquals(ImmutableMap.of(2, "b", 4, "d"), diff1.entriesOnlyOnLeft());
    assertEquals(ImmutableMap.of(6, "z"), diff1.entriesOnlyOnRight());
    assertEquals(ImmutableMap.of(1, "a"), diff1.entriesInCommon());
    assertEquals(ImmutableMap.of(3,
        ValueDifferenceImpl.create("c", "f"), 5,
        ValueDifferenceImpl.create("e", "g")),
        diff1.entriesDiffering());
    assertEquals("not equal: only on left={2=b, 4=d}: only on right={6=z}: "
        + "value differences={3=(c, f), 5=(e, g)}", diff1.toString());

    MapDifference<Integer, String> diff2 = Maps.difference(right, left);
    assertFalse(diff2.areEqual());
    assertEquals(ImmutableMap.of(6, "z"), diff2.entriesOnlyOnLeft());
    assertEquals(ImmutableMap.of(2, "b", 4, "d"), diff2.entriesOnlyOnRight());
    assertEquals(ImmutableMap.of(1, "a"), diff2.entriesInCommon());
    assertEquals(ImmutableMap.of(3,
        ValueDifferenceImpl.create("f", "c"), 5,
        ValueDifferenceImpl.create("g", "e")),
        diff2.entriesDiffering());
    assertEquals("not equal: only on left={6=z}: only on right={2=b, 4=d}: "
        + "value differences={3=(f, c), 5=(g, e)}", diff2.toString());
  }

  public void testMapDifferenceEquals() {
    Map<Integer, String> left = ImmutableMap.of(
        1, "a", 2, "b", 3, "c", 4, "d", 5, "e");
    Map<Integer, String> right = ImmutableMap.of(
        1, "a", 3, "f", 5, "g", 6, "z");
    Map<Integer, String> right2 = ImmutableMap.of(
        1, "a", 3, "h", 5, "g", 6, "z");
    MapDifference<Integer, String> original = Maps.difference(left, right);
    MapDifference<Integer, String> same = Maps.difference(left, right);
    MapDifference<Integer, String> reverse = Maps.difference(right, left);
    MapDifference<Integer, String> diff2 = Maps.difference(left, right2);

    new EqualsTester()
        .addEqualityGroup(original, same)
        .addEqualityGroup(reverse)
        .addEqualityGroup(diff2)
        .testEquals();
  }

  public void testMapDifferencePredicateTypical() {
    Map<Integer, String> left = ImmutableMap.of(
        1, "a", 2, "b", 3, "c", 4, "d", 5, "e");
    Map<Integer, String> right = ImmutableMap.of(
        1, "A", 3, "F", 5, "G", 6, "Z");

    // TODO(kevinb): replace with Ascii.caseInsensitiveEquivalence() when it
    // exists
    Equivalence<String> caseInsensitiveEquivalence = toGuava(com.google.common.base.Equivalence.equals().onResultOf(
        new com.google.common.base.Function<String, Object>() {
          @Override public String apply(String input) {
            return input.toLowerCase();
          }
        }));

    MapDifference<Integer, String> diff1 = Maps.difference(left, right,
        caseInsensitiveEquivalence);
    assertFalse(diff1.areEqual());
    assertEquals(ImmutableMap.of(2, "b", 4, "d"), diff1.entriesOnlyOnLeft());
    assertEquals(ImmutableMap.of(6, "Z"), diff1.entriesOnlyOnRight());
    assertEquals(ImmutableMap.of(1, "a"), diff1.entriesInCommon());
    assertEquals(ImmutableMap.of(3,
        ValueDifferenceImpl.create("c", "F"), 5,
        ValueDifferenceImpl.create("e", "G")),
        diff1.entriesDiffering());
    assertEquals("not equal: only on left={2=b, 4=d}: only on right={6=Z}: "
        + "value differences={3=(c, F), 5=(e, G)}", diff1.toString());

    MapDifference<Integer, String> diff2 = Maps.difference(right, left,
        caseInsensitiveEquivalence);
    assertFalse(diff2.areEqual());
    assertEquals(ImmutableMap.of(6, "Z"), diff2.entriesOnlyOnLeft());
    assertEquals(ImmutableMap.of(2, "b", 4, "d"), diff2.entriesOnlyOnRight());
    assertEquals(ImmutableMap.of(1, "A"), diff2.entriesInCommon());
    assertEquals(ImmutableMap.of(3,
        ValueDifferenceImpl.create("F", "c"), 5,
        ValueDifferenceImpl.create("G", "e")),
        diff2.entriesDiffering());
    assertEquals("not equal: only on left={6=Z}: only on right={2=b, 4=d}: "
        + "value differences={3=(F, c), 5=(G, e)}", diff2.toString());
  }

  private static final SortedMap<Integer, Integer> SORTED_EMPTY = new TreeMap<Integer, Integer>();
  private static final SortedMap<Integer, Integer> SORTED_SINGLETON =
      ImmutableSortedMap.of(1, 2);

  public void testMapDifferenceOfSortedMapIsSorted() {
    Map<Integer, Integer> map = SORTED_SINGLETON;
    MapDifference<Integer, Integer> difference = Maps.difference(map, EMPTY);
    assertTrue(difference instanceof SortedMapDifference);
  }

  public void testSortedMapDifferenceEmptyEmpty() {
    SortedMapDifference<Integer, Integer> diff =
        Maps.difference(SORTED_EMPTY, SORTED_EMPTY);
    assertTrue(diff.areEqual());
    assertEquals(SORTED_EMPTY, diff.entriesOnlyOnLeft());
    assertEquals(SORTED_EMPTY, diff.entriesOnlyOnRight());
    assertEquals(SORTED_EMPTY, diff.entriesInCommon());
    assertEquals(SORTED_EMPTY, diff.entriesDiffering());
    assertEquals("equal", diff.toString());
  }

  public void testSortedMapDifferenceEmptySingleton() {
    SortedMapDifference<Integer, Integer> diff =
        Maps.difference(SORTED_EMPTY, SORTED_SINGLETON);
    assertFalse(diff.areEqual());
    assertEquals(SORTED_EMPTY, diff.entriesOnlyOnLeft());
    assertEquals(SORTED_SINGLETON, diff.entriesOnlyOnRight());
    assertEquals(SORTED_EMPTY, diff.entriesInCommon());
    assertEquals(SORTED_EMPTY, diff.entriesDiffering());
    assertEquals("not equal: only on right={1=2}", diff.toString());
  }

  public void testSortedMapDifferenceSingletonEmpty() {
    SortedMapDifference<Integer, Integer> diff =
        Maps.difference(SORTED_SINGLETON, SORTED_EMPTY);
    assertFalse(diff.areEqual());
    assertEquals(SORTED_SINGLETON, diff.entriesOnlyOnLeft());
    assertEquals(SORTED_EMPTY, diff.entriesOnlyOnRight());
    assertEquals(SORTED_EMPTY, diff.entriesInCommon());
    assertEquals(SORTED_EMPTY, diff.entriesDiffering());
    assertEquals("not equal: only on left={1=2}", diff.toString());
  }

  public void testSortedMapDifferenceTypical() {
    SortedMap<Integer, String> left =
        ImmutableSortedMap.<Integer, String>reverseOrder()
        .put(1, "a").put(2, "b").put(3, "c").put(4, "d").put(5, "e")
        .build();

    SortedMap<Integer, String> right =
        ImmutableSortedMap.of(1, "a", 3, "f", 5, "g", 6, "z");

    SortedMapDifference<Integer, String> diff1 =
        Maps.difference(left, right);
    assertFalse(diff1.areEqual());
    assertThat(diff1.entriesOnlyOnLeft().entrySet()).containsExactly(
        immutableEntry(4, "d"), immutableEntry(2, "b")).inOrder();
    assertThat(diff1.entriesOnlyOnRight().entrySet()).contains(
        immutableEntry(6, "z"));
    assertThat(diff1.entriesInCommon().entrySet()).contains(
        immutableEntry(1, "a"));
    assertThat(diff1.entriesDiffering().entrySet()).containsExactly(
        immutableEntry(5, ValueDifferenceImpl.create("e", "g")),
        immutableEntry(3, ValueDifferenceImpl.create("c", "f"))).inOrder();
    assertEquals("not equal: only on left={4=d, 2=b}: only on right={6=z}: "
        + "value differences={5=(e, g), 3=(c, f)}", diff1.toString());

    SortedMapDifference<Integer, String> diff2 =
        Maps.difference(right, left);
    assertFalse(diff2.areEqual());
    assertThat(diff2.entriesOnlyOnLeft().entrySet()).contains(
        immutableEntry(6, "z"));
    assertThat(diff2.entriesOnlyOnRight().entrySet()).containsExactly(
        immutableEntry(2, "b"), immutableEntry(4, "d")).inOrder();
    assertThat(diff1.entriesInCommon().entrySet()).contains(
        immutableEntry(1, "a"));
    assertEquals(ImmutableMap.of(
            3, ValueDifferenceImpl.create("f", "c"),
            5, ValueDifferenceImpl.create("g", "e")),
        diff2.entriesDiffering());
    assertEquals("not equal: only on left={6=z}: only on right={2=b, 4=d}: "
        + "value differences={3=(f, c), 5=(g, e)}", diff2.toString());
  }

  public void testSortedMapDifferenceImmutable() {
    SortedMap<Integer, String> left = new TreeMap<Integer, String>(
        ImmutableSortedMap.of(1, "a", 2, "b", 3, "c", 4, "d", 5, "e"));
    SortedMap<Integer, String> right =
        new TreeMap<Integer, String>(ImmutableSortedMap.of(1, "a", 3, "f", 5, "g", 6, "z"));

    SortedMapDifference<Integer, String> diff1 =
        Maps.difference(left, right);
    left.put(6, "z");
    assertFalse(diff1.areEqual());
    assertThat(diff1.entriesOnlyOnLeft().entrySet()).containsExactly(
        immutableEntry(2, "b"), immutableEntry(4, "d")).inOrder();
    assertThat(diff1.entriesOnlyOnRight().entrySet()).contains(
        immutableEntry(6, "z"));
    assertThat(diff1.entriesInCommon().entrySet()).contains(
        immutableEntry(1, "a"));
    assertThat(diff1.entriesDiffering().entrySet()).containsExactly(
        immutableEntry(3, ValueDifferenceImpl.create("c", "f")),
        immutableEntry(5, ValueDifferenceImpl.create("e", "g"))).inOrder();
    try {
      diff1.entriesInCommon().put(7, "x");
      fail();
    } catch (UnsupportedOperationException expected) {
    }
    try {
      diff1.entriesOnlyOnLeft().put(7, "x");
      fail();
    } catch (UnsupportedOperationException expected) {
    }
    try {
      diff1.entriesOnlyOnRight().put(7, "x");
      fail();
    } catch (UnsupportedOperationException expected) {
    }
  }

  public void testSortedMapDifferenceEquals() {
    SortedMap<Integer, String> left =
        ImmutableSortedMap.of(1, "a", 2, "b", 3, "c", 4, "d", 5, "e");
    SortedMap<Integer, String> right =
        ImmutableSortedMap.of(1, "a", 3, "f", 5, "g", 6, "z");
    SortedMap<Integer, String> right2 =
        ImmutableSortedMap.of(1, "a", 3, "h", 5, "g", 6, "z");
    SortedMapDifference<Integer, String> original =
        Maps.difference(left, right);
    SortedMapDifference<Integer, String> same =
        Maps.difference(left, right);
    SortedMapDifference<Integer, String> reverse =
        Maps.difference(right, left);
    SortedMapDifference<Integer, String> diff2 =
        Maps.difference(left, right2);

    new EqualsTester()
        .addEqualityGroup(original, same)
        .addEqualityGroup(reverse)
        .addEqualityGroup(diff2)
        .testEquals();
  }

  private static final Function<Integer, Double> SQRT_FUNCTION = new Function<Integer, Double>() {
      @Override
      public Double apply(Integer in) {
        return Math.sqrt(in);
      }
    };

  public void testTransformValues() {
    Map<String, Integer> map = ImmutableMap.of("a", 4, "b", 9);
    Map<String, Double> transformed = transformValues(map, SQRT_FUNCTION);

    assertEquals(ImmutableMap.of("a", 2.0, "b", 3.0), transformed);
  }

  public void testTransformValuesSecretlySorted() {
    Map<String, Integer> map =
        sortedNotNavigable(ImmutableSortedMap.of("a", 4, "b", 9));
    Map<String, Double> transformed = transformValues(map, SQRT_FUNCTION);

    assertEquals(ImmutableMap.of("a", 2.0, "b", 3.0), transformed);
    assertTrue(transformed instanceof SortedMap);
  }

  @MiniGuavaSpecific // originally testTransformValuesSecretlyNavigable but this behavior is changed in miniguava.
  public void testTransformValuesNOTSecretlyNavigable() {
    Map<String, Integer> map = ImmutableSortedMap.of("a", 4, "b", 9);
    Map<String, Double> transformed;

    transformed = transformValues(map, SQRT_FUNCTION);
    assertEquals(ImmutableMap.of("a", 2.0, "b", 3.0), transformed);
    assertFalse(transformed instanceof NavigableMap);

    transformed =
        transformValues((SortedMap<String, Integer>) map, SQRT_FUNCTION);
    assertEquals(ImmutableMap.of("a", 2.0, "b", 3.0), transformed);
    assertFalse(transformed instanceof NavigableMap);
  }

  public void testTransformEntries() {
    Map<String, String> map = ImmutableMap.of("a", "4", "b", "9");
    EntryTransformer<String, String, String> concat =
        new EntryTransformer<String, String, String>() {
          @Override
          public String transformEntry(String key, String value) {
            return key + value;
          }
        };
    Map<String, String> transformed = transformEntries(map, concat);

    assertEquals(ImmutableMap.of("a", "a4", "b", "b9"), transformed);
  }

  public void testTransformEntriesSecretlySorted() {
    Map<String, String> map = ImmutableSortedMap.of("a", "4", "b", "9");
    EntryTransformer<String, String, String> concat =
        new EntryTransformer<String, String, String>() {
          @Override
          public String transformEntry(String key, String value) {
            return key + value;
          }
        };
    Map<String, String> transformed = transformEntries(map, concat);

    assertEquals(ImmutableMap.of("a", "a4", "b", "b9"), transformed);
    assertTrue(transformed instanceof SortedMap);
  }

  @SuppressWarnings("unused")
  public void testTransformEntriesGenerics() {
    Map<Object, Object> map1 = ImmutableMap.<Object, Object>of(1, 2);
    Map<Object, Number> map2 = ImmutableMap.<Object, Number>of(1, 2);
    Map<Object, Integer> map3 = ImmutableMap.<Object, Integer>of(1, 2);
    Map<Number, Object> map4 = ImmutableMap.<Number, Object>of(1, 2);
    Map<Number, Number> map5 = ImmutableMap.<Number, Number>of(1, 2);
    Map<Number, Integer> map6 = ImmutableMap.<Number, Integer>of(1, 2);
    Map<Integer, Object> map7 = ImmutableMap.<Integer, Object>of(1, 2);
    Map<Integer, Number> map8 = ImmutableMap.<Integer, Number>of(1, 2);
    Map<Integer, Integer> map9 = ImmutableMap.<Integer, Integer>of(1, 2);
    Map<? extends Number, ? extends Number> map0 = ImmutableMap.of(1, 2);

    EntryTransformer<Number, Number, Double> transformer =
        new EntryTransformer<Number, Number, Double>() {
          @Override
          public Double transformEntry(Number key, Number value) {
            return key.doubleValue() + value.doubleValue();
          }
        };

    Map<Object, Double> objectKeyed;
    Map<Number, Double> numberKeyed;
    Map<Integer, Double> integerKeyed;

    numberKeyed = transformEntries(map5, transformer);
    numberKeyed = transformEntries(map6, transformer);
    integerKeyed = transformEntries(map8, transformer);
    integerKeyed = transformEntries(map9, transformer);

    Map<? extends Number, Double> wildcarded = transformEntries(map0, transformer);

    // Can't loosen the key type:
    // objectKeyed = transformEntries(map5, transformer);
    // objectKeyed = transformEntries(map6, transformer);
    // objectKeyed = transformEntries(map8, transformer);
    // objectKeyed = transformEntries(map9, transformer);
    // numberKeyed = transformEntries(map8, transformer);
    // numberKeyed = transformEntries(map9, transformer);

    // Can't loosen the value type:
    // Map<Number, Number> looseValued1 = transformEntries(map5, transformer);
    // Map<Number, Number> looseValued2 = transformEntries(map6, transformer);
    // Map<Integer, Number> looseValued3 = transformEntries(map8, transformer);
    // Map<Integer, Number> looseValued4 = transformEntries(map9, transformer);

    // Can't call with too loose a key:
    // transformEntries(map1, transformer);
    // transformEntries(map2, transformer);
    // transformEntries(map3, transformer);

    // Can't call with too loose a value:
    // transformEntries(map1, transformer);
    // transformEntries(map4, transformer);
    // transformEntries(map7, transformer);
  }

  public void testTransformEntriesExample() {
    Map<String, Boolean> options =
        ImmutableMap.of("verbose", true, "sort", false);
    EntryTransformer<String, Boolean, String> flagPrefixer =
        new EntryTransformer<String, Boolean, String>() {
          @Override
          public String transformEntry(String key, Boolean value) {
            return value ? key : "no" + key;
          }
        };
    Map<String, String> transformed = transformEntries(options, flagPrefixer);
    assertEquals("{verbose=verbose, sort=nosort}", transformed.toString());
  }

  // Logically this would accept a NavigableMap, but that won't work under GWT.
  private static <K, V> SortedMap<K, V> sortedNotNavigable(
      final SortedMap<K, V> map) {
    return new ForwardingSortedMap<K, V>() {
      @Override protected SortedMap<K, V> delegate() {
        return map;
      }
    };
  }

  public void testSortedMapTransformValues() {
    SortedMap<String, Integer> map =
        sortedNotNavigable(ImmutableSortedMap.of("a", 4, "b", 9));
    SortedMap<String, Double> transformed =
        transformValues(map, SQRT_FUNCTION);

    /*
     * We'd like to sanity check that we didn't get a NavigableMap out, but we
     * can't easily do so while maintaining GWT compatibility.
     */
    assertEquals(ImmutableSortedMap.of("a", 2.0, "b", 3.0), transformed);
  }

  public void testSortedMapTransformEntries() {
    SortedMap<String, String> map =
        sortedNotNavigable(ImmutableSortedMap.of("a", "4", "b", "9"));
    EntryTransformer<String, String, String> concat =
        new EntryTransformer<String, String, String>() {
          @Override
          public String transformEntry(String key, String value) {
            return key + value;
          }
        };
    SortedMap<String, String> transformed = transformEntries(map, concat);

    /*
     * We'd like to sanity check that we didn't get a NavigableMap out, but we
     * can't easily do so while maintaining GWT compatibility.
     */
    assertEquals(ImmutableSortedMap.of("a", "a4", "b", "b9"), transformed);
  }

  @MiniGuavaSpecific
  private <K, V> Entry<K, V> immutableEntry(@Nullable K key, @Nullable V value) {
    return com.google.common.collect.Maps.immutableEntry(key, value);
  }

  @MiniGuavaSpecific
  private <T> Equivalence<T> toGuava(final com.google.common.base.Equivalence<T> equivalence) {
    return new Equivalence<T>() {
      @Override
      protected boolean doEquivalent(T a, T b) {
        return equivalence.equivalent(a, b);
      }

      @Override
      protected int doHash(T t) {
        return equivalence.hash(t);
      }
    };
  }
}
