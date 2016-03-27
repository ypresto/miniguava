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

import static com.google.common.collect.Sets.powerSet;
import static com.google.common.collect.testing.IteratorFeature.UNMODIFIABLE;
import static java.util.Collections.singleton;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.testing.IteratorTester;
import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.SetFeature;
import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import net.ypresto.miniguava.annotations.MiniGuavaSpecific;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Unit test for {@code Sets}.
 *
 * @author Kevin Bourrillion
 * @author Jared Levy
 */
// miniguava: Removed tests without matching method and unused methods.
public class SetsTest extends TestCase {

  private static final IteratorTester.KnownOrder KNOWN_ORDER =
      IteratorTester.KnownOrder.KNOWN_ORDER;

  private static final Collection<Integer> EMPTY_COLLECTION
      = Arrays.<Integer>asList();

  private static final Collection<Integer> SOME_COLLECTION
      = Arrays.asList(0, 1, 1);

  private static final Iterable<Integer> SOME_ITERABLE
      = new Iterable<Integer>() {
        @Override
        public Iterator<Integer> iterator() {
          return SOME_COLLECTION.iterator();
        }
      };

  private static final List<Integer> LONGER_LIST
      = Arrays.asList(8, 6, 7, 5, 3, 0, 9);

  private static final Comparator<Integer> SOME_COMPARATOR
      = Collections.reverseOrder();

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(SetsTest.class);

    suite.addTest(SetTestSuiteBuilder.using(new TestStringSetGenerator() {
          @Override protected Set<String> create(String[] elements) {
            return Sets.newConcurrentHashSet(Arrays.asList(elements));
          }
        })
        .named("Sets.newConcurrentHashSet")
        .withFeatures(CollectionSize.ANY, SetFeature.GENERAL_PURPOSE)
        .createTestSuite());

    suite.addTest(SetTestSuiteBuilder.using(new TestStringSetGenerator() {
          @Override protected Set<String> create(String[] elements) {
            int size = elements.length;
            // Remove last element, if size > 1
            Set<String> set1 = (size > 1)
                ? new HashSet<String>(
                    Arrays.asList(elements).subList(0, size - 1))
                : new HashSet<String>(Arrays.asList(elements));
            // Remove first element, if size > 0
            Set<String> set2 = (size > 0)
                ? new HashSet<String>(
                    Arrays.asList(elements).subList(1, size))
                : new HashSet<String>();
            return Sets.union(set1, set2);
          }
        })
        .named("Sets.union")
        .withFeatures(CollectionSize.ANY, CollectionFeature.ALLOWS_NULL_VALUES)
        .createTestSuite());

    suite.addTest(SetTestSuiteBuilder.using(new TestStringSetGenerator() {
          @Override protected Set<String> create(String[] elements) {
            Set<String> set1 = new HashSet<String>(Arrays.asList(elements));
            set1.add(samples().e3());
            Set<String> set2 = new HashSet<String>(Arrays.asList(elements));
            set2.add(samples().e4());
            return Sets.intersection(set1, set2);
          }
        })
        .named("Sets.intersection")
        .withFeatures(CollectionSize.ANY, CollectionFeature.ALLOWS_NULL_VALUES)
        .createTestSuite());

    suite.addTest(SetTestSuiteBuilder.using(new TestStringSetGenerator() {
          @Override protected Set<String> create(String[] elements) {
            Set<String> set1 = new HashSet<String>(Arrays.asList(elements));
            set1.add(samples().e3());
            Set<String> set2 = new HashSet<String>(Collections.singletonList(samples().e3()));
            return Sets.difference(set1, set2);
          }
        })
        .named("Sets.difference")
        .withFeatures(CollectionSize.ANY, CollectionFeature.ALLOWS_NULL_VALUES)
        .createTestSuite());

    return suite;
  }

  private enum SomeEnum { A, B, C, D }

  public void testNewConcurrentHashSetFromCollection() {
    Set<Integer> set = Sets.newConcurrentHashSet(SOME_COLLECTION);
    verifySetContents(set, SOME_COLLECTION);
  }

  public void testNewIdentityHashSet() {
    Set<Integer> set = Sets.newIdentityHashSet();
    Integer value1 = new Integer(12357);
    Integer value2 = new Integer(12357);
    assertTrue(set.add(value1));
    assertFalse(set.contains(value2));
    assertTrue(set.contains(value1));
    assertTrue(set.add(value2));
    assertEquals(2, set.size());
  }

  public void testComplementOfEnumSet() {
    Set<SomeEnum> units = EnumSet.of(SomeEnum.B, SomeEnum.D);
    EnumSet<SomeEnum> otherUnits = Sets.complementOf(units);
    verifySetContents(otherUnits, EnumSet.of(SomeEnum.A, SomeEnum.C));
  }

  public void testComplementOfEnumSetWithType() {
    Set<SomeEnum> units = EnumSet.of(SomeEnum.B, SomeEnum.D);
    EnumSet<SomeEnum> otherUnits = Sets.complementOf(units, SomeEnum.class);
    verifySetContents(otherUnits, EnumSet.of(SomeEnum.A, SomeEnum.C));
  }

  public void testComplementOfRegularSet() {
    Set<SomeEnum> units = new HashSet<SomeEnum>(Arrays.asList(SomeEnum.B, SomeEnum.D));
    EnumSet<SomeEnum> otherUnits = Sets.complementOf(units);
    verifySetContents(otherUnits, EnumSet.of(SomeEnum.A, SomeEnum.C));
  }

  public void testComplementOfRegularSetWithType() {
    Set<SomeEnum> units = new HashSet<SomeEnum>(Arrays.asList(SomeEnum.B, SomeEnum.D));
    EnumSet<SomeEnum> otherUnits = Sets.complementOf(units, SomeEnum.class);
    verifySetContents(otherUnits, EnumSet.of(SomeEnum.A, SomeEnum.C));
  }

  public void testComplementOfEmptySet() {
    Set<SomeEnum> noUnits = Collections.emptySet();
    EnumSet<SomeEnum> allUnits = Sets.complementOf(noUnits, SomeEnum.class);
    verifySetContents(EnumSet.allOf(SomeEnum.class), allUnits);
  }

  public void testComplementOfFullSet() {
    Set<SomeEnum> allUnits = new HashSet<SomeEnum>(Arrays.asList(SomeEnum.values()));
    EnumSet<SomeEnum> noUnits = Sets.complementOf(allUnits, SomeEnum.class);
    verifySetContents(noUnits, EnumSet.noneOf(SomeEnum.class));
  }

  public void testComplementOfEmptyEnumSetWithoutType() {
    Set<SomeEnum> noUnits = EnumSet.noneOf(SomeEnum.class);
    EnumSet<SomeEnum> allUnits = Sets.complementOf(noUnits);
    verifySetContents(allUnits, EnumSet.allOf(SomeEnum.class));
  }

  public void testComplementOfEmptySetWithoutTypeDoesntWork() {
    Set<SomeEnum> set = Collections.emptySet();
    try {
      Sets.complementOf(set);
      fail();
    } catch (IllegalArgumentException expected) {}
  }

  public void testNullPointerExceptions() {
    new NullPointerTester()
        .setDefault(Enum.class, SomeEnum.A)
        .setDefault(Class.class, SomeEnum.class) // for newEnumSet
        .testAllPublicStaticMethods(Sets.class);
  }

  public void testPowerSetEmpty() {
    ImmutableSet<Integer> elements = ImmutableSet.of();
    Set<Set<Integer>> powerSet = powerSet(elements);
    assertEquals(1, powerSet.size());
    assertEquals(ImmutableSet.of(ImmutableSet.of()), powerSet);
    assertEquals(0, powerSet.hashCode());
  }

  public void testPowerSetContents() {
    ImmutableSet<Integer> elements = ImmutableSet.of(1, 2, 3);
    Set<Set<Integer>> powerSet = powerSet(elements);
    assertEquals(8, powerSet.size());
    assertEquals(4 * 1 + 4 * 2 + 4 * 3, powerSet.hashCode());

    Set<Set<Integer>> expected = newHashSet();
    expected.add(ImmutableSet.<Integer>of());
    expected.add(ImmutableSet.of(1));
    expected.add(ImmutableSet.of(2));
    expected.add(ImmutableSet.of(3));
    expected.add(ImmutableSet.of(1, 2));
    expected.add(ImmutableSet.of(1, 3));
    expected.add(ImmutableSet.of(2, 3));
    expected.add(ImmutableSet.of(1, 2, 3));

    Set<Set<Integer>> almostPowerSet = newHashSet(expected);
    almostPowerSet.remove(ImmutableSet.of(1, 2, 3));
    almostPowerSet.add(ImmutableSet.of(1, 2, 4));

    new EqualsTester()
        .addEqualityGroup(expected, powerSet)
        .addEqualityGroup(ImmutableSet.of(1, 2, 3))
        .addEqualityGroup(almostPowerSet)
        .testEquals();

    for (Set<Integer> subset : expected) {
      assertTrue(powerSet.contains(subset));
    }
    assertFalse(powerSet.contains(ImmutableSet.of(1, 2, 4)));
    assertFalse(powerSet.contains(singleton(null)));
    assertFalse(powerSet.contains(null));
    assertFalse(powerSet.contains("notASet"));
  }

  public void testPowerSetIteration_manual() {
    ImmutableSet<Integer> elements = ImmutableSet.of(1, 2, 3);
    Set<Set<Integer>> powerSet = powerSet(elements);
    // The API doesn't promise this iteration order, but it's convenient here.
    Iterator<Set<Integer>> i = powerSet.iterator();
    assertEquals(ImmutableSet.of(), i.next());
    assertEquals(ImmutableSet.of(1), i.next());
    assertEquals(ImmutableSet.of(2), i.next());
    assertEquals(ImmutableSet.of(2, 1), i.next());
    assertEquals(ImmutableSet.of(3), i.next());
    assertEquals(ImmutableSet.of(3, 1), i.next());
    assertEquals(ImmutableSet.of(3, 2), i.next());
    assertEquals(ImmutableSet.of(3, 2, 1), i.next());
    assertFalse(i.hasNext());
    try {
      i.next();
      fail();
    } catch (NoSuchElementException expected) {
    }
  }

  public void testPowerSetIteration_iteratorTester() {
    ImmutableSet<Integer> elements = ImmutableSet.of(1, 2);

    Set<Set<Integer>> expected = new LinkedHashSet<Set<Integer>>();
    expected.add(ImmutableSet.<Integer>of());
    expected.add(ImmutableSet.of(1));
    expected.add(ImmutableSet.of(2));
    expected.add(ImmutableSet.of(1, 2));

    final Set<Set<Integer>> powerSet = powerSet(elements);
    new IteratorTester<Set<Integer>>(6, UNMODIFIABLE, expected, KNOWN_ORDER) {
      @Override protected Iterator<Set<Integer>> newTargetIterator() {
        return powerSet.iterator();
      }
    }.test();
  }

  public void testPowerSetIteration_iteratorTester_fast() {
    ImmutableSet<Integer> elements = ImmutableSet.of(1, 2);

    Set<Set<Integer>> expected = new LinkedHashSet<Set<Integer>>();
    expected.add(ImmutableSet.<Integer>of());
    expected.add(ImmutableSet.of(1));
    expected.add(ImmutableSet.of(2));
    expected.add(ImmutableSet.of(1, 2));

    final Set<Set<Integer>> powerSet = powerSet(elements);
    new IteratorTester<Set<Integer>>(4, UNMODIFIABLE, expected, KNOWN_ORDER) {
      @Override protected Iterator<Set<Integer>> newTargetIterator() {
        return powerSet.iterator();
      }
    }.test();
  }

  public void testPowerSetSize() {
    assertPowerSetSize(1);
    assertPowerSetSize(2, 'a');
    assertPowerSetSize(4, 'a', 'b');
    assertPowerSetSize(8, 'a', 'b', 'c');
    assertPowerSetSize(16, 'a', 'b', 'd', 'e');
    assertPowerSetSize(1 << 30,
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2',
        '3', '4');
  }

  public void testPowerSetCreationErrors() {
    try {
      powerSet(newHashSet('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
          'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
          'y', 'z', '1', '2', '3', '4', '5'));
      fail();
    } catch (IllegalArgumentException expected) {
    }

    try {
      powerSet(singleton(null));
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testPowerSetEqualsAndHashCode_verifyAgainstHashSet() {
    ImmutableList<Integer> allElements = ImmutableList.of(4233352, 3284593,
        3794208, 3849533, 4013967, 2902658, 1886275, 2131109, 985872, 1843868);
    for (int i = 0; i < allElements.size(); i++) {
      Set<Integer> elements = newHashSet(allElements.subList(0, i));
      Set<Set<Integer>> powerSet1 = powerSet(elements);
      Set<Set<Integer>> powerSet2 = powerSet(elements);
      new EqualsTester()
          .addEqualityGroup(powerSet1, powerSet2, toHashSets(powerSet1))
          .addEqualityGroup(ImmutableSet.of())
          .addEqualityGroup(ImmutableSet.of(9999999))
          .addEqualityGroup("notASet")
          .testEquals();
      assertEquals(toHashSets(powerSet1).hashCode(), powerSet1.hashCode());
    }
  }

  /**
   * Test that a hash code miscomputed by "input.hashCode() * tooFarValue / 2"
   * is correct under our {@code hashCode} implementation.
   */
  public void testPowerSetHashCode_inputHashCodeTimesTooFarValueIsZero() {
    Set<Object> sumToEighthMaxIntElements =
        newHashSet(objectWithHashCode(1 << 29), objectWithHashCode(0));
    assertPowerSetHashCode(1 << 30, sumToEighthMaxIntElements);

    Set<Object> sumToQuarterMaxIntElements =
        newHashSet(objectWithHashCode(1 << 30), objectWithHashCode(0));
    assertPowerSetHashCode(1 << 31, sumToQuarterMaxIntElements);
  }

  public void testPowerSetShowOff() {
    Set<Object> zero = ImmutableSet.of();
    Set<Set<Object>> one = powerSet(zero);
    Set<Set<Set<Object>>> two = powerSet(one);
    Set<Set<Set<Set<Object>>>> four = powerSet(two);
    Set<Set<Set<Set<Set<Object>>>>> sixteen = powerSet(four);
    Set<Set<Set<Set<Set<Set<Object>>>>>> sixtyFiveThousandish =
        powerSet(sixteen);
    assertEquals(1 << 16, sixtyFiveThousandish.size());

    assertTrue(powerSet(makeSetOfZeroToTwentyNine())
        .contains(makeSetOfZeroToTwentyNine()));
    assertFalse(powerSet(makeSetOfZeroToTwentyNine())
        .contains(ImmutableSet.of(30)));
  }

  private static Set<Integer> makeSetOfZeroToTwentyNine() {
    // TODO: use Range once it's publicly available
    Set<Integer> zeroToTwentyNine = newHashSet();
    for (int i = 0; i < 30; i++) {
      zeroToTwentyNine.add(i);
    }
    return zeroToTwentyNine;
  }

  private static <E> Set<Set<E>> toHashSets(Set<Set<E>> powerSet) {
    Set<Set<E>> result = newHashSet();
    for (Set<E> subset : powerSet) {
      result.add(new HashSet<E>(subset));
    }
    return result;
  }

  private static Object objectWithHashCode(final int hashCode) {
    return new Object() {
      @Override public int hashCode() {
        return hashCode;
      }
    };
  }

  private static void assertPowerSetHashCode(int expected, Set<?> elements) {
    assertEquals(expected, powerSet(elements).hashCode());
  }

  private static void assertPowerSetSize(int i, Object... elements) {
    assertEquals(i, powerSet(newHashSet(elements)).size());
  }

  /**
   * Utility method that verifies that the given set is equal to and hashes
   * identically to a set constructed with the elements in the given iterable.
   */
  private static <E> void verifySetContents(Set<E> set, Iterable<E> contents) {
    Set<E> expected = null;
    if (contents instanceof Set) {
      expected = (Set<E>) contents;
    } else {
      expected = new HashSet<E>();
      for (E element : contents) {
        expected.add(element);
      }
    }
    assertEquals(expected, set);
  }

  @MiniGuavaSpecific
  private static <T> Set<T> newHashSet() {
    return new HashSet<T>();
  }

  @MiniGuavaSpecific
  private static <T> Set<T> newHashSet(Collection<T> collection) {
    return new HashSet<T>(collection);
  }

  @MiniGuavaSpecific
  private static <T> Set<T> newHashSet(T... elements) {
    return new HashSet<T>(Arrays.asList(elements));
  }
}
