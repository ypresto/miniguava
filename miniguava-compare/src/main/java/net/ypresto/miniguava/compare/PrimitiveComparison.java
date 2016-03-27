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

import net.ypresto.miniguava.annotations.MiniGuavaSpecific;

/**
 * This is miniguava specific class which contains compare() methods from primitives package.
 *
 * <p><b>Note for Java 7 and later:</b> this method should be treated as
 * deprecated; use the equivalent {@code compare} methods like {@link Integer#compare} instead.
 */
@MiniGuavaSpecific
public class PrimitiveComparison {
  /**
   * Compares the two specified {@code int} values. The sign of the value
   * returned is the same as that of {@code ((Integer) a).compareTo(b)}.
   *
   * <p><b>Note for Java 7 and later:</b> this method should be treated as
   * deprecated; use the equivalent {@link Integer#compare} method instead.
   *
   * @param a the first {@code int} to compare
   * @param b the second {@code int} to compare
   * @return a negative value if {@code a} is less than {@code b}; a positive
   *     value if {@code a} is greater than {@code b}; or zero if they are equal
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.Ints")
  public static int compareInts(int a, int b) {
    return (a < b) ? -1 : ((a > b) ? 1 : 0);
  }

  /**
   * Compares the two specified {@code long} values. The sign of the value
   * returned is the same as that of {@code ((Long) a).compareTo(b)}.
   *
   * <p><b>Note for Java 7 and later:</b> this method should be treated as
   * deprecated; use the equivalent {@link Long#compare} method instead.
   *
   * @param a the first {@code long} to compare
   * @param b the second {@code long} to compare
   * @return a negative value if {@code a} is less than {@code b}; a positive
   *     value if {@code a} is greater than {@code b}; or zero if they are equal
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.Longs")
  public static int compareLongs(long a, long b) {
    return (a < b) ? -1 : ((a > b) ? 1 : 0);
  }

  /**
   * Compares the two specified {@code float} values using {@link
   * Float#compare(float, float)}. You may prefer to invoke that method
   * directly; this method exists only for consistency with the other utilities
   * in this package.
   *
   * <p><b>Note:</b> this method simply delegates to the JDK method {@link
   * Float#compare}. It is provided for consistency with the other primitive
   * types, whose compare methods were not added to the JDK until JDK 7.
   *
   * @param a the first {@code float} to compare
   * @param b the second {@code float} to compare
   * @return the result of invoking {@link Float#compare(float, float)}
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.Floats")
  public static int compareFloats(float a, float b) {
    return Float.compare(a, b);
  }

  /**
   * Compares the two specified {@code double} values. The sign of the value
   * returned is the same as that of <code>((Double) a).{@linkplain
   * Double#compareTo compareTo}(b)</code>. As with that method, {@code NaN} is
   * treated as greater than all other values, and {@code 0.0 > -0.0}.
   *
   * <p><b>Note:</b> this method simply delegates to the JDK method {@link
   * Double#compare}. It is provided for consistency with the other primitive
   * types, whose compare methods were not added to the JDK until JDK 7.
   *
   * @param a the first {@code double} to compare
   * @param b the second {@code double} to compare
   * @return a negative value if {@code a} is less than {@code b}; a positive
   *     value if {@code a} is greater than {@code b}; or zero if they are equal
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.Doubles")
  public static int compareDoubles(double a, double b) {
    return Double.compare(a, b);
  }

  /**
   * Compares the two specified {@code char} values. The sign of the value
   * returned is the same as that of {@code ((Character) a).compareTo(b)}.
   *
   * <p><b>Note for Java 7 and later:</b> this method should be treated as
   * deprecated; use the equivalent {@link Character#compare} method instead.
   *
   * @param a the first {@code char} to compare
   * @param b the second {@code char} to compare
   * @return a negative value if {@code a} is less than {@code b}; a positive
   *     value if {@code a} is greater than {@code b}; or zero if they are equal
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.Chars")
  public static int compareChars(char a, char b) {
    return a - b; // safe due to restricted range
  }

  /**
   * Compares the two specified {@code short} values. The sign of the value
   * returned is the same as that of {@code ((Short) a).compareTo(b)}.
   *
   * <p><b>Note for Java 7 and later:</b> this method should be treated as
   * deprecated; use the equivalent {@link Short#compare} method instead.
   *
   * @param a the first {@code short} to compare
   * @param b the second {@code short} to compare
   * @return a negative value if {@code a} is less than {@code b}; a positive
   *     value if {@code a} is greater than {@code b}; or zero if they are equal
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.Shorts")
  public static int compareShorts(short a, short b) {
    return a - b; // safe due to restricted range
  }

  /**
   * Compares the two specified {@code boolean} values in the standard way
   * ({@code false} is considered less than {@code true}). The sign of the
   * value returned is the same as that of {@code ((Boolean) a).compareTo(b)}.
   *
   * <p><b>Note for Java 7 and later:</b> this method should be treated as
   * deprecated; use the equivalent {@link Boolean#compare} method instead.
   *
   * @param a the first {@code boolean} to compare
   * @param b the second {@code boolean} to compare
   * @return a positive number if only {@code a} is {@code true}, a negative
   *     number if only {@code b} is true, or zero if {@code a == b}
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.MOVED, from = "primitives.Booleans")
  public static int compareBooleans(boolean a, boolean b) {
    return (a == b) ? 0 : (a ? 1 : -1);
  }
}
