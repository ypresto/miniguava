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

import static com.google.common.truth.Truth.assertThat;
import static java.io.ObjectStreamConstants.TC_REFERENCE;
import static java.io.ObjectStreamConstants.baseWireHandle;

import com.google.common.collect.Ordering;
import com.google.common.collect.testing.AnEnum;
import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestEnumSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.testing.SerializableTester;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import net.ypresto.miniguava.annotations.MiniGuavaSpecific;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

// miniguava: Firstly copied from SetsTests.java
@MiniGuavaSpecific
public class ImmutablesTest extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(ImmutablesTest.class);

    suite.addTest(SetTestSuiteBuilder.using(new TestEnumSetGenerator() {
      @Override protected Set<AnEnum> create(AnEnum[] elements) {
        return Immutables.immutableEnumSet(Arrays.asList(elements));
      }
    })
        .named("Immutables.immutableEnumSet")
        .withFeatures(CollectionSize.ONE, CollectionSize.SEVERAL,
            CollectionFeature.ALLOWS_NULL_QUERIES)
        .createTestSuite());

    return suite;
  }

  private enum SomeEnum { A, B, C, D }

  public void testImmutableEnumSet() {
    Set<SomeEnum> units = Immutables.immutableEnumSet(Arrays.asList(SomeEnum.D, SomeEnum.B));

    assertThat(units).containsExactly(SomeEnum.B, SomeEnum.D).inOrder();
    try {
      units.remove(SomeEnum.B);
      fail("ImmutableEnumSet should throw an exception on remove()");
    } catch (UnsupportedOperationException expected) {}
    try {
      units.add(SomeEnum.C);
      fail("ImmutableEnumSet should throw an exception on add()");
    } catch (UnsupportedOperationException expected) {}
  }

  public void testImmutableEnumSet_serialized() {
    Set<SomeEnum> units = Immutables.immutableEnumSet(Arrays.asList(SomeEnum.D, SomeEnum.B));

    assertThat(units).containsExactly(SomeEnum.B, SomeEnum.D).inOrder();

    Set<SomeEnum> copy = SerializableTester.reserializeAndAssert(units);
    assertTrue(copy instanceof ImmutableEnumSet);
  }

  public void testImmutableEnumSet_deserializationMakesDefensiveCopy()
      throws Exception {
    ImmutableSet<SomeEnum> original =
        Immutables.immutableEnumSet(Arrays.asList(SomeEnum.A, SomeEnum.B));
    int handleOffset = 6;
    byte[] serializedForm = serializeWithBackReference(original, handleOffset);
    ObjectInputStream in =
        new ObjectInputStream(new ByteArrayInputStream(serializedForm));

    ImmutableSet<?> deserialized = (ImmutableSet<?>) in.readObject();
    EnumSet<?> delegate = (EnumSet<?>) in.readObject();

    assertEquals(original, deserialized);
    assertTrue(delegate.remove(SomeEnum.A));
    assertTrue(deserialized.contains(SomeEnum.A));
  }

  private static byte[] serializeWithBackReference(
      Object original, int handleOffset) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(bos);

    out.writeObject(original);

    byte[] handle = toByteArray(baseWireHandle + handleOffset);
    byte[] ref = prepended(TC_REFERENCE, handle);
    bos.write(ref);

    return bos.toByteArray();
  }

  private static byte[] prepended(byte b, byte[] array) {
    byte[] out = new byte[array.length + 1];
    out[0] = b;
    System.arraycopy(array, 0, out, 1, array.length);
    return out;
  }

  private static byte[] toByteArray(int h) {
    return ByteBuffer.allocate(4).putInt(h).array();
  }

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "OrderingTest.java")
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

  public void testImmutableSortedCopy() {
    NumberOrdering numberOrdering = new NumberOrdering();
    ImmutableList<Integer> unsortedInts = ImmutableList.of(5, 3, 0, 9, 3);
    ImmutableList<Integer> sortedInts
        = Immutables.immutableSortedCopyOfList(unsortedInts, numberOrdering);
    assertEquals(Arrays.asList(0, 3, 3, 5, 9), sortedInts);

    assertEquals(Collections.<Integer>emptyList(),
        Immutables.immutableSortedCopyOfList(Collections.<Integer>emptyList(), numberOrdering));

    List<Integer> listWithNull = Arrays.asList(5, 3, null, 9);
    try {
      Immutables.immutableSortedCopyOfList(listWithNull, Ordering.natural().nullsFirst());
      fail();
    } catch (NullPointerException expected) {
    }
  }
}
