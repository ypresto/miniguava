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

package net.ypresto.miniguava.compare;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.testing.Helpers;
import com.google.common.primitives.Ints;
import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;

import junit.framework.TestCase;

import net.ypresto.miniguava.annotations.MiniGuavaSpecific;
import net.ypresto.miniguava.base.Function;
import net.ypresto.miniguava.compare.Ordering.IncomparableValueException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Unit tests for {@code Ordering}.
 *
 * @author Jesse Wilson
 */
// miniguava: Removed tests for methods which does not exist in miniguava.
// miniguava: Modified to use sortedCopy of Lists (miniguava specific method).
// miniguava: Removed seriaization tests.
public class OrderingTest extends TestCase {
  // TODO(cpovirk): some of these are inexplicably slow (20-30s) under GWT

  private final Ordering<Number> numberOrdering = new NumberOrdering();

  public void testAllEqual() {
    Ordering<Object> comparator = Ordering.allEqual();
    assertSame(comparator, comparator.reverse());

    assertEquals(comparator.compare(null, null), 0);
    assertEquals(comparator.compare(new Object(), new Object()), 0);
    assertEquals(comparator.compare("apples", "oranges"), 0);
    assertEquals("Ordering.allEqual()", comparator.toString());

    List<String> strings = ImmutableList.of("b", "a", "d", "c");
    assertEquals(strings, sortedCopy(strings, comparator));
  }

  // From https://github.com/google/guava/issues/1342
  public void testComplicatedOrderingExample() {
    Integer nullInt = (Integer) null;
    Ordering<Iterable<Integer>> example =
        Ordering.<Integer>natural().nullsFirst().reverse().lexicographical().reverse().nullsLast();
    List<Integer> list1 = Lists.newArrayList();
    List<Integer> list2 = Lists.newArrayList(1);
    List<Integer> list3 = Lists.newArrayList(1, 1);
    List<Integer> list4 = Lists.newArrayList(1, 2);
    List<Integer> list5 = Lists.newArrayList(1, null, 2);
    List<Integer> list6 = Lists.newArrayList(2);
    List<Integer> list7 = Lists.newArrayList(nullInt);
    List<Integer> list8 = Lists.newArrayList(nullInt, nullInt);
    List<List<Integer>> list =
        Lists.newArrayList(list1, list2, list3, list4, list5, list6, list7, list8, null);
    List<List<Integer>> sorted = sortedCopy(list, example);

    // [[null, null], [null], [1, null, 2], [1, 1], [1, 2], [1], [2], [], null]
    assertThat(sorted)
        .containsExactly(
            Lists.newArrayList(nullInt, nullInt),
            Lists.newArrayList(nullInt),
            Lists.newArrayList(1, null, 2),
            Lists.newArrayList(1, 1),
            Lists.newArrayList(1, 2),
            Lists.newArrayList(1),
            Lists.newArrayList(2),
            Lists.newArrayList(),
            null)
        .inOrder();
  }

  public void testNatural() {
    Ordering<Integer> comparator = Ordering.natural();
    Helpers.testComparator(comparator,
        Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE);
    try {
      comparator.compare(1, null);
      fail();
    } catch (NullPointerException expected) {}
    try {
      comparator.compare(null, 2);
      fail();
    } catch (NullPointerException expected) {}
    try {
      comparator.compare(null, null);
      fail();
    } catch (NullPointerException expected) {}
    assertEquals("Ordering.natural()", comparator.toString());
  }

  public void testFrom() {
    Ordering<String> caseInsensitiveOrdering
        = Ordering.from(String.CASE_INSENSITIVE_ORDER);
    assertEquals(0, caseInsensitiveOrdering.compare("A", "a"));
    assertTrue(caseInsensitiveOrdering.compare("a", "B") < 0);
    assertTrue(caseInsensitiveOrdering.compare("B", "a") > 0);

    @SuppressWarnings("deprecation") // test of deprecated method
    Ordering<String> orderingFromOrdering =
        Ordering.from(Ordering.<String>natural());
    new EqualsTester()
        .addEqualityGroup(caseInsensitiveOrdering, Ordering.from(String.CASE_INSENSITIVE_ORDER))
        .addEqualityGroup(orderingFromOrdering, Ordering.natural())
        .testEquals();
  }

  public void testExplicit_none() {
    Comparator<Integer> c
        = Ordering.explicit(Collections.<Integer>emptyList());
    try {
      c.compare(0, 0);
      fail();
    } catch (IncomparableValueException expected) {
      assertEquals(0, expected.value);
    }
  }

  public void testExplicit_one() {
    Comparator<Integer> c = Ordering.explicit(0);
    assertEquals(0, c.compare(0, 0));
    try {
      c.compare(0, 1);
      fail();
    } catch (IncomparableValueException expected) {
      assertEquals(1, expected.value);
    }
    assertEquals("Ordering.explicit([0])", c.toString());
  }

  public void testExplicit_two() {
    Comparator<Integer> c = Ordering.explicit(42, 5);
    assertEquals(0, c.compare(5, 5));
    assertTrue(c.compare(5, 42) > 0);
    assertTrue(c.compare(42, 5) < 0);
    try {
      c.compare(5, 666);
      fail();
    } catch (IncomparableValueException expected) {
      assertEquals(666, expected.value);
    }
    new EqualsTester()
        .addEqualityGroup(c, Ordering.explicit(42, 5))
        .addEqualityGroup(Ordering.explicit(5, 42))
        .addEqualityGroup(Ordering.explicit(42))
        .testEquals();
  }

  public void testExplicit_sortingExample() {
    Comparator<Integer> c
        = Ordering.explicit(2, 8, 6, 1, 7, 5, 3, 4, 0, 9);
    List<Integer> list = Arrays.asList(0, 3, 5, 6, 7, 8, 9);
    Collections.sort(list, c);
    assertThat(list).containsExactly(8, 6, 7, 5, 3, 0, 9).inOrder();
  }

  public void testExplicit_withDuplicates() {
    try {
      Ordering.explicit(1, 2, 3, 4, 2);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testUsingToString() {
    Ordering<Object> ordering = Ordering.usingToString();
    Helpers.testComparator(ordering, 1, 12, 124, 2);
    assertEquals("Ordering.usingToString()", ordering.toString());
  }

  // use an enum to get easy serializability
  private enum CharAtFunction implements Function<String, Character> {
    AT0(0),
    AT1(1),
    AT2(2),
    AT3(3),
    AT4(4),
    AT5(5),
    ;

    final int index;
    CharAtFunction(int index) {
      this.index = index;
    }
    @Override
    public Character apply(String string) {
      return string.charAt(index);
    }
  }

  private static Ordering<String> byCharAt(int index) {
    return Ordering.natural().onResultOf(CharAtFunction.values()[index]);
  }

  public void testCompound_static() {
    Comparator<String> comparator = Ordering.compound(ImmutableList.of(
        byCharAt(0), byCharAt(1), byCharAt(2),
        byCharAt(3), byCharAt(4), byCharAt(5)));
    Helpers.testComparator(comparator, ImmutableList.of(
        "applesauce",
        "apricot",
        "artichoke",
        "banality",
        "banana",
        "banquet",
        "tangelo",
        "tangerine"));
  }

  public void testCompound_instance() {
    Comparator<String> comparator = byCharAt(1).compound(byCharAt(0));
    Helpers.testComparator(comparator, ImmutableList.of(
        "red",
        "yellow",
        "violet",
        "blue",
        "indigo",
        "green",
        "orange"));
  }

  public void testCompound_instance_generics() {
    Ordering<Object> objects = Ordering.explicit((Object) 1);
    Ordering<Number> numbers = Ordering.explicit((Number) 1);
    Ordering<Integer> integers = Ordering.explicit(1);

    // Like by like equals like
    Ordering<Number> a = numbers.compound(numbers);

    // The compound takes the more specific type of the two, regardless of order

    Ordering<Number> b = numbers.compound(objects);
    Ordering<Number> c = objects.compound(numbers);

    Ordering<Integer> d = numbers.compound(integers);
    Ordering<Integer> e = integers.compound(numbers);

    // This works with three levels too (IDEA falsely reports errors as noted
    // below. Both javac and eclipse handle these cases correctly.)

    Ordering<Number> f = numbers.compound(objects).compound(objects); //bad IDEA
    Ordering<Number> g = objects.compound(numbers).compound(objects);
    Ordering<Number> h = objects.compound(objects).compound(numbers);

    Ordering<Number> i = numbers.compound(objects.compound(objects));
    Ordering<Number> j = objects.compound(numbers.compound(objects)); //bad IDEA
    Ordering<Number> k = objects.compound(objects.compound(numbers));

    // You can also arbitrarily assign a more restricted type - not an intended
    // feature, exactly, but unavoidable (I think) and harmless
    Ordering<Integer> l = objects.compound(numbers);

    // This correctly doesn't work:
    // Ordering<Object> m = numbers.compound(objects);

    // Sadly, the following works in javac 1.6, but at least it fails for
    // eclipse, and is *correctly* highlighted red in IDEA.
    // Ordering<Object> n = objects.compound(numbers);
  }

  public void testReverse() {
    Ordering<Number> reverseOrder = numberOrdering.reverse();
    Helpers.testComparator(reverseOrder,
        Integer.MAX_VALUE, 1, 0, -1, Integer.MIN_VALUE);

    new EqualsTester()
        .addEqualityGroup(reverseOrder, numberOrdering.reverse())
        .addEqualityGroup(Ordering.natural().reverse())
        .addEqualityGroup(Collections.reverseOrder())
        .testEquals();
  }

  public void testReverseOfReverseSameAsForward() {
    // Not guaranteed by spec, but it works, and saves us from testing
    // exhaustively
    assertSame(numberOrdering, numberOrdering.reverse().reverse());
  }

  private enum StringLengthFunction implements Function<String, Integer> {
    StringLength;

    @Override
    public Integer apply(String string) {
      return string.length();
    }
  }

  private static final Ordering<Integer> DECREASING_INTEGER
      = Ordering.natural().reverse();

  public void testOnResultOf_natural() {
    Comparator<String> comparator
        = Ordering.natural().onResultOf(StringLengthFunction.StringLength);
    assertTrue(comparator.compare("to", "be") == 0);
    assertTrue(comparator.compare("or", "not") < 0);
    assertTrue(comparator.compare("that", "to") > 0);

    new EqualsTester()
        .addEqualityGroup(
            comparator,
            Ordering.natural().onResultOf(StringLengthFunction.StringLength))
        .addEqualityGroup(DECREASING_INTEGER)
        .testEquals();
    assertEquals("Ordering.natural().onResultOf(StringLength)",
        comparator.toString());
  }

  public void testOnResultOf_chained() {
    Comparator<String> comparator = DECREASING_INTEGER.onResultOf(
        StringLengthFunction.StringLength);
    assertTrue(comparator.compare("to", "be") == 0);
    assertTrue(comparator.compare("not", "or") < 0);
    assertTrue(comparator.compare("to", "that") > 0);

    // miniguava: Modified not to use Functions.constant(1).
    new EqualsTester()
        .addEqualityGroup(
            comparator,
            DECREASING_INTEGER.onResultOf(StringLengthFunction.StringLength))
        .addEqualityGroup(
            DECREASING_INTEGER.onResultOf(new Function<Object, Integer>() {
              @Nullable
              @Override
              public Integer apply(@Nullable Object input) {
                return 1;
              }
            }))
        .addEqualityGroup(Ordering.natural())
        .testEquals();
    assertEquals("Ordering.natural().reverse().onResultOf(StringLength)",
        comparator.toString());
  }

  @SuppressWarnings("unchecked") // dang varargs
  public void testLexicographical() {
    Ordering<String> ordering = Ordering.natural();
    Ordering<Iterable<String>> lexy = ordering.lexicographical();

    ImmutableList<String> empty = ImmutableList.of();
    ImmutableList<String> a = ImmutableList.of("a");
    ImmutableList<String> aa = ImmutableList.of("a", "a");
    ImmutableList<String> ab = ImmutableList.of("a", "b");
    ImmutableList<String> b = ImmutableList.of("b");

    Helpers.testComparator(lexy, empty, a, aa, ab, b);

    new EqualsTester()
        .addEqualityGroup(lexy, ordering.lexicographical())
        .addEqualityGroup(numberOrdering.lexicographical())
        .addEqualityGroup(Ordering.natural())
        .testEquals();
  }

  public void testNullsFirst() {
    Ordering<Integer> ordering = Ordering.natural().nullsFirst();
    Helpers.testComparator(ordering, null, Integer.MIN_VALUE, 0, 1);

    new EqualsTester()
        .addEqualityGroup(ordering, Ordering.natural().nullsFirst())
        .addEqualityGroup(numberOrdering.nullsFirst())
        .addEqualityGroup(Ordering.natural())
        .testEquals();
  }

  public void testNullsLast() {
    Ordering<Integer> ordering = Ordering.natural().nullsLast();
    Helpers.testComparator(ordering, 0, 1, Integer.MAX_VALUE, null);

    new EqualsTester()
        .addEqualityGroup(ordering, Ordering.natural().nullsLast())
        .addEqualityGroup(numberOrdering.nullsLast())
        .addEqualityGroup(Ordering.natural())
        .testEquals();
  }

  // miniguava: Moved testSortedCopy to ListsTest.
  // miniguava: Moved testImmutableSortedCopy to ImmutablesTest.

  private static class NumberOrdering extends Ordering<Number> {
    @Override public int compare(Number a, Number b) {
      return ((Double) a.doubleValue()).compareTo(b.doubleValue());
    }
    @Override public int hashCode() {
      return NumberOrdering.class.hashCode();
    }
    @Override public boolean equals(Object other) {
      return other instanceof NumberOrdering;
    }
  }

  /*
   * Now we have monster tests that create hundreds of Orderings using different
   * combinations of methods, then checks compare(), binarySearch() and so
   * forth on each one.
   */

  // should periodically try increasing this, but it makes the test run long
  private static final int RECURSE_DEPTH = 2;

  public void testCombinationsExhaustively_startingFromNatural() {
    testExhaustively(Ordering.<String>natural(), "a", "b", "d");
  }

  public void testCombinationsExhaustively_startingFromExplicit() {
    testExhaustively(Ordering.explicit("a", "b", "c", "d"),
        "a", "b", "d");
  }

  public void testCombinationsExhaustively_startingFromUsingToString() {
    testExhaustively(Ordering.usingToString(), 1, 12, 2);
  }

  public void testCombinationsExhaustively_startingFromFromComparator() {
    testExhaustively(Ordering.from(String.CASE_INSENSITIVE_ORDER),
        "A", "b", "C", "d");
  }

  /**
   * Requires at least 3 elements in {@code strictlyOrderedElements} in order to
   * test the varargs version of min/max.
   */
  private static <T> void testExhaustively(
      Ordering<? super T> ordering, T... strictlyOrderedElements) {
    checkArgument(strictlyOrderedElements.length >= 3, "strictlyOrderedElements "
        + "requires at least 3 elements");
    List<T> list = Arrays.asList(strictlyOrderedElements);

    // for use calling Collection.toArray later
    T[] emptyArray = newArray(strictlyOrderedElements, 0);

    // shoot me, but I didn't want to deal with wildcards through the whole test
    @SuppressWarnings("unchecked")
    Scenario<T> starter = new Scenario<T>((Ordering) ordering, list, emptyArray);
    verifyScenario(starter, 0);
  }

  /**
   * Returns a new array of the given length with the same type as a reference
   * array.
   *
   * @param reference any array of the desired type
   * @param length the length of the new array
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "Platform")
  private static <T> T[] newArray(T[] reference, int length) {
    Class<?> type = reference.getClass().getComponentType();

    // the cast is safe because
    // result.getClass() == reference.getClass().getComponentType()
    @SuppressWarnings("unchecked")
    T[] result = (T[]) Array.newInstance(type, length);
    return result;
  }

  private static <T> void verifyScenario(Scenario<T> scenario, int level) {
    scenario.testCompareTo();

    if (level < RECURSE_DEPTH) {
      for (OrderingMutation alteration : OrderingMutation.values()) {
        verifyScenario(alteration.mutate(scenario), level + 1);
      }
    }
  }

  /**
   * An aggregation of an ordering with a list (of size > 1) that should prove
   * to be in strictly increasing order according to that ordering.
   */
  private static class Scenario<T> {
    final Ordering<T> ordering;
    final List<T> strictlyOrderedList;
    final T[] emptyArray;

    Scenario(Ordering<T> ordering, List<T> strictlyOrderedList, T[] emptyArray) {
      this.ordering = ordering;
      this.strictlyOrderedList = strictlyOrderedList;
      this.emptyArray = emptyArray;
    }

    void testCompareTo() {
      Helpers.testComparator(ordering, strictlyOrderedList);
    }
  }

  /**
   * A means for changing an Ordering into another Ordering. Each instance is
   * responsible for creating the alternate Ordering, and providing a List that
   * is known to be ordered, based on an input List known to be ordered
   * according to the input Ordering.
   */
  private enum OrderingMutation {
    REVERSE {
      @Override <T> Scenario<?> mutate(Scenario<T> scenario) {
        List<T> newList = Lists.newArrayList(scenario.strictlyOrderedList);
        Collections.reverse(newList);
        return new Scenario<T>(scenario.ordering.reverse(), newList, scenario.emptyArray);
      }
    },
    NULLS_FIRST {
      @Override <T> Scenario<?> mutate(Scenario<T> scenario) {
        @SuppressWarnings("unchecked")
        List<T> newList = Lists.newArrayList((T) null);
        for (T t : scenario.strictlyOrderedList) {
          if (t != null) {
            newList.add(t);
          }
        }
        return new Scenario<T>(scenario.ordering.nullsFirst(), newList, scenario.emptyArray);
      }
    },
    NULLS_LAST {
      @Override <T> Scenario<?> mutate(Scenario<T> scenario) {
        List<T> newList = Lists.newArrayList();
        for (T t : scenario.strictlyOrderedList) {
          if (t != null) {
            newList.add(t);
          }
        }
        newList.add(null);
        return new Scenario<T>(scenario.ordering.nullsLast(), newList, scenario.emptyArray);
      }
    },
    ON_RESULT_OF {
      @Override <T> Scenario<?> mutate(final Scenario<T> scenario) {
        Ordering<Integer> ordering = scenario.ordering.onResultOf(
            new Function<Integer, T>() {
              @Override
              public T apply(@Nullable Integer from) {
                return scenario.strictlyOrderedList.get(from);
              }
            });
        List<Integer> list = Lists.newArrayList();
        for (int i = 0; i < scenario.strictlyOrderedList.size(); i++) {
          list.add(i);
        }
        return new Scenario<Integer>(ordering, list, new Integer[0]);
      }
    },
    COMPOUND_THIS_WITH_NATURAL {
      @SuppressWarnings("unchecked") // raw array
      @Override <T> Scenario<?> mutate(Scenario<T> scenario) {
        List<Composite<T>> composites = Lists.newArrayList();
        for (T t : scenario.strictlyOrderedList) {
          composites.add(new Composite<T>(t, 1));
          composites.add(new Composite<T>(t, 2));
        }
        Ordering<Composite<T>> ordering =
            scenario.ordering.onResultOf(Composite.<T>getValueFunction())
                .compound(Ordering.natural());
        return new Scenario<Composite<T>>(ordering, composites, new Composite[0]);
      }
    },
    COMPOUND_NATURAL_WITH_THIS {
      @SuppressWarnings("unchecked") // raw array
      @Override <T> Scenario<?> mutate(Scenario<T> scenario) {
        List<Composite<T>> composites = Lists.newArrayList();
        for (T t : scenario.strictlyOrderedList) {
          composites.add(new Composite<T>(t, 1));
        }
        for (T t : scenario.strictlyOrderedList) {
          composites.add(new Composite<T>(t, 2));
        }
        Ordering<Composite<T>> ordering = Ordering.natural().compound(
            scenario.ordering.onResultOf(Composite.<T>getValueFunction()));
        return new Scenario<Composite<T>>(ordering, composites, new Composite[0]);
      }
    },
    LEXICOGRAPHICAL {
      @SuppressWarnings("unchecked") // dang varargs
      @Override <T> Scenario<?> mutate(Scenario<T> scenario) {
        List<Iterable<T>> words = Lists.newArrayList();
        words.add(Collections.<T>emptyList());
        for (T t : scenario.strictlyOrderedList) {
          words.add(Arrays.asList(t));
          for (T s : scenario.strictlyOrderedList) {
            words.add(Arrays.asList(t, s));
          }
        }
        return new Scenario<Iterable<T>>(
            scenario.ordering.lexicographical(), words, new Iterable[0]);
      }
    },
    ;

    abstract <T> Scenario<?> mutate(Scenario<T> scenario);
  }

  /**
   * A dummy object we create so that we can have something meaningful to have
   * a compound ordering over.
   */
  private static class Composite<T> implements Comparable<Composite<T>> {
    final T value;
    final int rank;

    Composite(T value, int rank) {
      this.value = value;
      this.rank = rank;
    }

    // natural order is by rank only; the test will compound() this with the
    // order of 't'.
    @Override
    public int compareTo(Composite<T> that) {
      return Ints.compare(rank, that.rank);
    }

    static <T> Function<Composite<T>, T> getValueFunction() {
      return new Function<Composite<T>, T>() {
        @Override
        public T apply(Composite<T> from) {
          return from.value;
        }
      };
    }
  }

  public void testNullPointerExceptions() {
    NullPointerTester tester = new NullPointerTester();
    tester.testAllPublicStaticMethods(Ordering.class);

    // any Ordering<Object> instance that accepts nulls should be good enough
    tester.testAllPublicInstanceMethods(Ordering.usingToString().nullsFirst());
  }

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "Ordering")
  public static <E> List<E> sortedCopy(Collection<E> elements, Comparator<? super E> comparator) {
    checkNotNull(comparator, "comparator");
    @SuppressWarnings("unchecked") // does not escape, and contains only E's
      E[] array = (E[]) elements.toArray();
    Arrays.sort(array, comparator);
    return new ArrayList<E>(Arrays.asList(array));
  }
}
