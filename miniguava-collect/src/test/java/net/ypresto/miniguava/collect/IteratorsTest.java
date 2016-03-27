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

import static com.google.common.collect.testing.IteratorFeature.MODIFIABLE;
import static com.google.common.collect.testing.IteratorFeature.UNMODIFIABLE;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

import com.google.common.base.Predicates;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.testing.IteratorTester;
import com.google.common.collect.testing.ListTestSuiteBuilder;
import com.google.common.collect.testing.TestStringListGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.ListFeature;
import com.google.common.testing.NullPointerTester;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import net.ypresto.miniguava.annotations.MiniGuavaSpecific;
import net.ypresto.miniguava.base.Function;
import net.ypresto.miniguava.base.Predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Unit test for {@code Iterators}.
 *
 * @author Kevin Bourrillion
 */
public class IteratorsTest extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(IteratorsTest.class.getSimpleName());
    suite.addTest(testsForRemoveAll());
    suite.addTestSuite(IteratorsTest.class);
    return suite;
  }

  public void testEmptyIterator() {
    Iterator<String> iterator = Iterators.emptyIterator();
    assertFalse(iterator.hasNext());
    try {
      iterator.next();
      fail("no exception thrown");
    } catch (NoSuchElementException expected) {
    }
    try {
      iterator.remove();
      fail("no exception thrown");
    } catch (UnsupportedOperationException expected) {
    }
  }

  public void testEmptyListIterator() {
    ListIterator<String> iterator = Iterators.emptyListIterator();
    assertFalse(iterator.hasNext());
    assertFalse(iterator.hasPrevious());
    assertEquals(0, iterator.nextIndex());
    assertEquals(-1, iterator.previousIndex());
    try {
      iterator.next();
      fail("no exception thrown");
    } catch (NoSuchElementException expected) {
    }
    try {
      iterator.previous();
      fail("no exception thrown");
    } catch (NoSuchElementException expected) {
    }
    try {
      iterator.remove();
      fail("no exception thrown");
    } catch (UnsupportedOperationException expected) {
    }
    try {
      iterator.set("a");
      fail("no exception thrown");
    } catch (UnsupportedOperationException expected) {
    }
    try {
      iterator.add("a");
      fail("no exception thrown");
    } catch (UnsupportedOperationException expected) {
    }
  }

  public void testSize0() {
    Iterator<String> iterator = Iterators.emptyIterator();
    assertEquals(0, Iterators.size(iterator));
  }

  public void testSize1() {
    Iterator<Integer> iterator = Collections.singleton(0).iterator();
    assertEquals(1, Iterators.size(iterator));
  }

  public void testSize_partiallyConsumed() {
    Iterator<Integer> iterator = asList(1, 2, 3, 4, 5).iterator();
    iterator.next();
    iterator.next();
    assertEquals(3, Iterators.size(iterator));
  }

  public void testFilterSimple() {
    Iterator<String> unfiltered = newArrayList("foo", "bar").iterator();
    Iterator<String> filtered = Iterators.filter(unfiltered,
      toGuava(Predicates.equalTo("foo")));
    List<String> expected = Collections.singletonList("foo");
    List<String> actual = newArrayList(filtered);
    assertEquals(expected, actual);
  }

  public void testFilterNoMatch() {
    Iterator<String> unfiltered = newArrayList("foo", "bar").iterator();
    Iterator<String> filtered = Iterators.filter(unfiltered,
      toGuava(Predicates.alwaysFalse()));
    List<String> expected = Collections.emptyList();
    List<String> actual = newArrayList(filtered);
    assertEquals(expected, actual);
  }

  public void testFilterMatchAll() {
    Iterator<String> unfiltered = newArrayList("foo", "bar").iterator();
    Iterator<String> filtered = Iterators.filter(unfiltered,
      toGuava(Predicates.alwaysTrue()));
    List<String> expected = newArrayList("foo", "bar");
    List<String> actual = newArrayList(filtered);
    assertEquals(expected, actual);
  }

  public void testFilterNothing() {
    Iterator<String> unfiltered = Collections.<String>emptyList().iterator();
    Iterator<String> filtered = Iterators.filter(unfiltered,
        new Predicate<String>() {
          @Override
          public boolean apply(String s) {
            throw new AssertionFailedError("Should never be evaluated");
          }
        });

    List<String> expected = Collections.emptyList();
    List<String> actual = newArrayList(filtered);
    assertEquals(expected, actual);
  }

  public void testFilterUsingIteratorTester() {
    final List<Integer> list = asList(1, 2, 3, 4, 5);
    final Predicate<Integer> isEven = new Predicate<Integer>() {
      @Override
      public boolean apply(Integer integer) {
        return integer % 2 == 0;
      }
    };
    new IteratorTester<Integer>(5, UNMODIFIABLE, asList(2, 4),
        IteratorTester.KnownOrder.KNOWN_ORDER) {
      @Override protected Iterator<Integer> newTargetIterator() {
        return Iterators.filter(list.iterator(), isEven);
      }
    }.test();
  }

  public void testTransform() {
    Iterator<String> input = asList("1", "2", "3").iterator();
    Iterator<Integer> result = Iterators.transform(input,
      new Function<String, Integer>() {
        @Override
        public Integer apply(String from) {
          return Integer.valueOf(from);
        }
      });

    List<Integer> actual = newArrayList(result);
    List<Integer> expected = asList(1, 2, 3);
    assertEquals(expected, actual);
  }

  public void testTransformRemove() {
    List<String> list = newArrayList("1", "2", "3");
    Iterator<String> input = list.iterator();
    Iterator<Integer> iterator = Iterators.transform(input,
        new Function<String, Integer>() {
          @Override
          public Integer apply(String from) {
            return Integer.valueOf(from);
          }
        });

    assertEquals(Integer.valueOf(1), iterator.next());
    assertEquals(Integer.valueOf(2), iterator.next());
    iterator.remove();
    assertEquals(asList("1", "3"), list);
  }

  public void testPoorlyBehavedTransform() {
    Iterator<String> input = asList("1", null, "3").iterator();
    Iterator<Integer> result = Iterators.transform(input,
        new Function<String, Integer>() {
          @Override
          public Integer apply(String from) {
            return Integer.valueOf(from);
          }
        });

    result.next();
    try {
      result.next();
      fail("Expected NFE");
    } catch (NumberFormatException nfe) {
      // Expected to fail.
    }
  }

  public void testNullFriendlyTransform() {
    Iterator<Integer> input = asList(1, 2, null, 3).iterator();
    Iterator<String> result = Iterators.transform(input,
      new Function<Integer, String>() {
        @Override
        public String apply(Integer from) {
          return String.valueOf(from);
        }
      });

    List<String> actual = newArrayList(result);
    List<String> expected = asList("1", "2", "null", "3");
    assertEquals(expected, actual);
  }

  public void testConcatMultipleEmptyIteratorsYieldsEmpty() {
    new EmptyIteratorTester() {
      @Override protected Iterator<Integer> newTargetIterator() {
        return Iterators.concat(iterateOver(), iterateOver());
      }
    }.test();
  }

  public void testConcatSingletonYieldsSingleton() {
    new SingletonIteratorTester() {
      @SuppressWarnings("unchecked")
      @Override protected Iterator<Integer> newTargetIterator() {
        return Iterators.concat(iterateOver(1));
      }
    }.test();
  }

  public void testConcatEmptyAndSingletonAndEmptyYieldsSingleton() {
    new SingletonIteratorTester() {
      @Override protected Iterator<Integer> newTargetIterator() {
        return Iterators.concat(iterateOver(), iterateOver(1), iterateOver());
      }
    }.test();
  }

  public void testConcatSingletonAndSingletonYieldsDoubleton() {
    new DoubletonIteratorTester() {
      @Override protected Iterator<Integer> newTargetIterator() {
        return Iterators.concat(iterateOver(1), iterateOver(2));
      }
    }.test();
  }

  public void testConcatSingletonAndSingletonWithEmptiesYieldsDoubleton() {
    new DoubletonIteratorTester() {
      @Override protected Iterator<Integer> newTargetIterator() {
        return Iterators.concat(
            iterateOver(1), iterateOver(), iterateOver(), iterateOver(2));
      }
    }.test();
  }

  public void testConcatUnmodifiable() {
    new IteratorTester<Integer>(5, UNMODIFIABLE, asList(1, 2),
        IteratorTester.KnownOrder.KNOWN_ORDER) {
      @Override protected Iterator<Integer> newTargetIterator() {
        return Iterators.concat(asList(1).iterator(),
            Arrays.<Integer>asList().iterator(), asList(2).iterator());
      }
    }.test();
  }

  /**
   * Illustrates the somewhat bizarre behavior when a null is passed in.
   */
  public void testConcatContainingNull() {
    @SuppressWarnings("unchecked")
    Iterator<Iterator<Integer>> input
        = asList(iterateOver(1, 2), null, iterateOver(3)).iterator();
    Iterator<Integer> result = Iterators.concat(input);
    assertEquals(1, (int) result.next());
    assertEquals(2, (int) result.next());
    try {
      result.hasNext();
      fail("no exception thrown");
    } catch (NullPointerException e) {
    }
    try {
      result.next();
      fail("no exception thrown");
    } catch (NullPointerException e) {
    }
    // There is no way to get "through" to the 3.  Buh-bye
  }

  @SuppressWarnings("unchecked")
  public void testConcatVarArgsContainingNull() {
    try {
      Iterators.concat(iterateOver(1, 2), null, iterateOver(3), iterateOver(4),
          iterateOver(5));
      fail("no exception thrown");
    } catch (NullPointerException e) {
    }
  }

  public void testAddAllWithEmptyIterator() {
    List<String> alreadyThere = newArrayList("already", "there");

    boolean changed = Iterators.addAll(alreadyThere,
                                       Iterators.<String>emptyIterator());
    assertThat(alreadyThere).containsExactly("already", "there").inOrder();
    assertFalse(changed);
  }

  public void testAddAllToList() {
    List<String> alreadyThere = newArrayList("already", "there");
    List<String> freshlyAdded = newArrayList("freshly", "added");

    boolean changed = Iterators.addAll(alreadyThere, freshlyAdded.iterator());

    assertThat(alreadyThere).containsExactly("already", "there", "freshly", "added");
    assertTrue(changed);
  }

  public void testAddAllToSet() {
    Set<String> alreadyThere
        = new LinkedHashSet<String>(asList("already", "there"));
    List<String> oneMore = newArrayList("there");

    boolean changed = Iterators.addAll(alreadyThere, oneMore.iterator());
    assertThat(alreadyThere).containsExactly("already", "there").inOrder();
    assertFalse(changed);
  }

  public void testNullPointerExceptions() {
    NullPointerTester tester = new NullPointerTester();
    tester.testAllPublicStaticMethods(Iterators.class);
  }

  private static abstract class EmptyIteratorTester
      extends IteratorTester<Integer> {
    protected EmptyIteratorTester() {
      super(3, MODIFIABLE, Collections.<Integer>emptySet(),
          IteratorTester.KnownOrder.KNOWN_ORDER);
    }
  }

  private static abstract class SingletonIteratorTester
      extends IteratorTester<Integer> {
    protected SingletonIteratorTester() {
      super(3, MODIFIABLE, singleton(1), IteratorTester.KnownOrder.KNOWN_ORDER);
    }
  }

  private static abstract class DoubletonIteratorTester
      extends IteratorTester<Integer> {
    protected DoubletonIteratorTester() {
      super(5, MODIFIABLE, newArrayList(1, 2),
          IteratorTester.KnownOrder.KNOWN_ORDER);
    }
  }

  private static Iterator<Integer> iterateOver(final Integer... values) {
    return newArrayList(values).iterator();
  }

  public void testRemoveAll() {
    List<String> list = newArrayList("a", "b", "c", "d", "e");
    assertTrue(Iterators.removeAll(
      list.iterator(), newArrayList("b", "d", "f")));
    assertEquals(newArrayList("a", "c", "e"), list);
    assertFalse(Iterators.removeAll(
      list.iterator(), newArrayList("x", "y", "z")));
    assertEquals(newArrayList("a", "c", "e"), list);
  }

  public void testRemoveIf() {
    List<String> list = newArrayList("a", "b", "c", "d", "e");
    assertTrue(Iterators.removeIf(
      list.iterator(),
      new Predicate<String>() {
        @Override
        public boolean apply(String s) {
          return s.equals("b") || s.equals("d") || s.equals("f");
        }
      }));
    assertEquals(newArrayList("a", "c", "e"), list);
    assertFalse(Iterators.removeIf(
      list.iterator(),
      new Predicate<String>() {
        @Override
        public boolean apply(String s) {
          return s.equals("x") || s.equals("y") || s.equals("z");
        }
      }));
    assertEquals(newArrayList("a", "c", "e"), list);
  }

  @MiniGuavaSpecific // miniguava: Originally testsForRemoveAllAndRetainAll
  private static Test testsForRemoveAll() {
    return ListTestSuiteBuilder.using(new TestStringListGenerator() {
      @Override public List<String> create(final String[] elements) {
        final List<String> delegate = newArrayList(elements);
        return new ForwardingList<String>() {
          @Override protected List<String> delegate() {
            return delegate;
          }

          @Override public boolean removeAll(Collection<?> c) {
            return Iterators.removeAll(iterator(), c);
          }
        };
      }
    })
      .named("ArrayList with Iterators.removeAll")
      .withFeatures(
        ListFeature.GENERAL_PURPOSE,
        CollectionFeature.ALLOWS_NULL_VALUES,
        CollectionSize.ANY)
      .createTestSuite();
  }

  @SuppressWarnings("deprecation")
  public void testUnmodifiableIteratorShortCircuit() {
    Iterator<String> mod = newArrayList("a", "b", "c").iterator();
    UnmodifiableIterator<String> unmod = Iterators.unmodifiableIterator(mod);
    assertNotSame(mod, unmod);
    assertSame(unmod, Iterators.unmodifiableIterator(unmod));
    assertSame(unmod, Iterators.unmodifiableIterator((Iterator<String>) unmod));
  }

  @MiniGuavaSpecific
  private <T> Predicate<T> toGuava(final com.google.common.base.Predicate<T> predicate) {
    return new Predicate<T>() {
      @Override
      public boolean apply(@Nullable T input) {
        return predicate.apply(input);
      }
    };
  }

  @MiniGuavaSpecific
  private static <T> ArrayList<T> newArrayList(T... elements) {
    return new ArrayList<T>(Arrays.asList(elements));
  }

  @MiniGuavaSpecific
  private static <T> ArrayList<T> newArrayList(Iterator<T> iterator) {
    ArrayList<T> arrayList = new ArrayList<T>();
    Iterators.addAll(arrayList, iterator);
    return arrayList;
  }
}
