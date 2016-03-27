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

package net.ypresto.miniguava.collect;

import static net.ypresto.miniguava.base.Preconditions.checkState;

import net.ypresto.miniguava.annotations.MiniGuavaSpecific;
import net.ypresto.miniguava.base.Joiner;

@MiniGuavaSpecific
class InternalUtils {
  private InternalUtils() {}

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "Collections2")
  static final Joiner STANDARD_JOINER = Joiner.on(", ").useForNull("null");

  /**
   * The largest power of two that can be represented as an {@code int}.
   *
   * @since 10.0
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "primitives.Ints")
  public static final int MAX_POWER_OF_TWO = 1 << (Integer.SIZE - 2);

  /**
   * Returns the {@code int} nearest in value to {@code value}.
   *
   * @param value any {@code long} value
   * @return the same value cast to {@code int} if it is in the range of the
   *     {@code int} type, {@link Integer#MAX_VALUE} if it is too large,
   *     or {@link Integer#MIN_VALUE} if it is too small
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "primitives.Ints#saturatedCast")
  static int saturatedCastToInt(long value) {
    if (value > Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    }
    if (value < Integer.MIN_VALUE) {
      return Integer.MIN_VALUE;
    }
    return (int) value;
  }

  /**
   * Precondition tester for {@code Iterator.remove()} that throws an exception with a consistent
   * error message.
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "CollectPreconditions")
  static void checkRemove(boolean canRemove) {
    checkState(canRemove, "no calls to next() since the last call to remove()");
  }

  /**
   * Returns the product of {@code a} and {@code b}, provided it does not overflow.
   *
   * @throws ArithmeticException if {@code a * b} overflows in signed {@code int} arithmetic
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "math.IntMath")
  public static int checkedMultiply(int a, int b) {
    long result = (long) a * b;
    checkNoOverflow(result == (int) result);
    return (int) result;
  }

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "math.MathPreconditions")
  static void checkNoOverflow(boolean condition) {
    if (!condition) {
      throw new ArithmeticException("overflow");
    }
  }

  /**
   * Returns best-effort-sized StringBuilder based on the given collection size.
   */
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "Collections2")
  static StringBuilder newStringBuilderForCollection(int size) {
    checkNonnegative(size, "size");
    return new StringBuilder((int) Math.min(size * 8L, MAX_POWER_OF_TWO));
  }

  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "CollectPreconditions")
  static int checkNonnegative(int value, String name) {
    if (value < 0) {
      throw new IllegalArgumentException(name + " cannot be negative but was: " + value);
    }
    return value;
  }
}
