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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.NullPointerTester;

import junit.framework.TestCase;

import java.util.Arrays;

/**
 * Unit test for {@code ObjectArrays}.
 *
 * @author Kevin Bourrillion
 */
// miniguava: Picked only necessary tests.
public class ObjectArraysTest extends TestCase {

  public void testNullPointerExceptions() {
    NullPointerTester tester = new NullPointerTester();
    tester.testAllPublicStaticMethods(ObjectArrays.class);
  }

  public void testNewArray_fromArray_Empty() {
    String[] in = new String[0];
    String[] empty = ObjectArrays.newArray(in, 0);
    assertThat(empty).isEmpty();
  }

  public void testNewArray_fromArray_Nonempty() {
    String[] array = ObjectArrays.newArray(new String[0], 2);
    assertEquals(String[].class, array.getClass());
    assertThat(array).hasLength(2);
    assertNull(array[0]);
  }

  public void testNewArray_fromArray_OfArray() {
    String[][] array = ObjectArrays.newArray(new String[0][0], 1);
    assertEquals(String[][].class, array.getClass());
    assertThat(array).hasLength(1);
    assertNull(array[0]);
  }

  public void testEmptyArrayToEmpty() {
    doTestNewArrayEquals(new Object[0], 0);
  }

  public void testEmptyArrayToNonEmpty() {
    checkArrayEquals(new Long[5], ObjectArrays.newArray(new Long[0], 5));
  }

  public void testNonEmptyToShorter() {
    checkArrayEquals(new String[9], ObjectArrays.newArray(new String[10], 9));
  }

  public void testNonEmptyToSameLength() {
    doTestNewArrayEquals(new String[10], 10);
  }

  public void testNonEmptyToLonger() {
    checkArrayEquals(new String[10],
        ObjectArrays.newArray(new String[] { "a", "b", "c", "d", "e" }, 10));
  }

  private static void checkArrayEquals(Object[] expected, Object[] actual) {
    assertTrue("expected(" + expected.getClass() + "): " + Arrays.toString(expected)
        + " actual(" + actual.getClass() + "): " + Arrays.toString(actual),
        arrayEquals(expected, actual));
  }

  private static boolean arrayEquals(Object[] array1, Object[] array2) {
    assertSame(array1.getClass(), array2.getClass());
    return Arrays.equals(array1, array2);
  }

  private static void doTestNewArrayEquals(Object[] expected, int length) {
    checkArrayEquals(expected, ObjectArrays.newArray(expected, length));
  }
}
