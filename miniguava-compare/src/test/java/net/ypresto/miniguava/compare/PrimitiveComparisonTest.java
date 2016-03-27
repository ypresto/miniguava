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

package net.ypresto.miniguava.compare;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;

import junit.framework.TestCase;

import net.ypresto.miniguava.annotations.MiniGuavaSpecific;

// miniguava: This test consists of tests in primitives package.
@MiniGuavaSpecific
public class PrimitiveComparisonTest extends TestCase {

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.IntsTest")
  private static final int[] INT_VALUES =
      { Integer.MIN_VALUE, (int) -1, (int) 0, (int) 1, Integer.MAX_VALUE };

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.IntsTest")
  public void testCompareInts() {
    for (int x : INT_VALUES) {
      for (int y : INT_VALUES) {
        // note: spec requires only that the sign is the same
        assertEquals(x + ", " + y,
            Integer.valueOf(x).compareTo(y),
            PrimitiveComparison.compareInts(x, y));
      }
    }
  }

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.LongsTest")
  private static final long[] LONG_VALUES =
      { Long.MIN_VALUE, (long) -1, (long) 0, (long) 1, Long.MAX_VALUE };

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.LongsTest")
  public void testCompareLongs() {
    for (long x : LONG_VALUES) {
      for (long y : LONG_VALUES) {
        // note: spec requires only that the sign is the same
        assertEquals(x + ", " + y,
            Long.valueOf(x).compareTo(y),
            PrimitiveComparison.compareLongs(x, y));
      }
    }
  }

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.FloatsTest")
  private static final float[] FLOAT_NUMBERS = new float[] {
      Float.NEGATIVE_INFINITY, -Float.MAX_VALUE, -1f, -0f, 0f, 1f, Float.MAX_VALUE, Float.POSITIVE_INFINITY,
      Float.MIN_NORMAL, -Float.MIN_NORMAL,  Float.MIN_VALUE, -Float.MIN_VALUE,
      Integer.MIN_VALUE, Integer.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE
  };

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.FloatsTest")
  private static final float[] FLOAT_VALUES
      = Floats.concat(FLOAT_NUMBERS, new float[] {Float.NaN});

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.FloatsTest")
  public void testCompareFloats() {
    for (float x : FLOAT_VALUES) {
      for (float y : FLOAT_VALUES) {
        // note: spec requires only that the sign is the same
        assertEquals(x + ", " + y,
            Float.valueOf(x).compareTo(y),
            PrimitiveComparison.compareFloats(x, y));
      }
    }
  }

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.DoublesTest")
  private static final double[] NUMBERS = new double[] {
      Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, -1.0, -0.5, -0.1, -0.0, 0.0, 0.1, 0.5, 1.0,
      Double.MAX_VALUE, Double.POSITIVE_INFINITY, Double.MIN_NORMAL, -Double.MIN_NORMAL,
      Double.MIN_VALUE, -Double.MIN_VALUE, Integer.MIN_VALUE,
      Integer.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE
  };

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.DoublesTest")
  private static final double[] DOUBLE_VALUES
      = Doubles.concat(NUMBERS, new double[] {Double.NaN});

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.DoublesTest")
  public void testCompareDoubles() {
    for (double x : DOUBLE_VALUES) {
      for (double y : DOUBLE_VALUES) {
        // note: spec requires only that the sign is the same
        assertEquals(x + ", " + y,
            Double.valueOf(x).compareTo(y),
            PrimitiveComparison.compareDoubles(x, y));
      }
    }
  }

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.ShortsTest")
  private static final short[] SHORT_VALUES =
      {Short.MIN_VALUE, (short) -1, (short) 0, (short) 1, Short.MAX_VALUE};

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.ShortsTest")
  public void testCompareShorts() {
    for (short x : SHORT_VALUES) {
      for (short y : SHORT_VALUES) {
        // Only compare the sign of the result of compareTo().
        int expected = Short.valueOf(x).compareTo(y);
        int actual = PrimitiveComparison.compareShorts(x, y);
        if (expected == 0) {
          assertEquals(x + ", " + y, expected, actual);
        } else if (expected < 0) {
          assertTrue(x + ", " + y + " (expected: " + expected + ", actual" + actual + ")",
              actual < 0);
        } else {
          assertTrue(x + ", " + y + " (expected: " + expected + ", actual" + actual + ")",
              actual > 0);
        }
      }
    }
  }

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.CharsTest")
  private static final char[] CHAR_VALUES =
      {Character.MIN_VALUE, 'a', '\u00e0', '\udcaa', Character.MAX_VALUE};

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.CharsTest")
  public void testCompareChars() {
    for (char x : CHAR_VALUES) {
      for (char y : CHAR_VALUES) {
        // note: spec requires only that the sign is the same
        assertEquals(x + ", " + y,
            Character.valueOf(x).compareTo(y),
            PrimitiveComparison.compareChars(x, y));
      }
    }
  }

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.BooleansTest")
  private static final boolean[] BOOLEAN_VALUES = {false, true};

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.BooleansTest")
  public void testCompareBooleans() {
    for (boolean x : BOOLEAN_VALUES) {
      for (boolean y : BOOLEAN_VALUES) {
        // note: spec requires only that the sign is the same
        assertEquals(x + ", " + y,
            Boolean.valueOf(x).compareTo(y),
            PrimitiveComparison.compareBooleans(x, y));
      }
    }
  }
}
