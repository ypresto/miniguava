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

import java.lang.reflect.Array;

// miniguava: Moved from collect package and changed from public to package-private.
// miniguava: Removed unused methods.
/**
 * Static utility methods pertaining to object arrays.
 *
 * @author Kevin Bourrillion
 * @since 2.0
 */
final class ObjectArrays {
  static final Object[] EMPTY_ARRAY = new Object[0];

  private ObjectArrays() {}

  /**
   * Returns a new array of the given length with the same type as a reference
   * array.
   *
   * @param reference any array of the desired type
   * @param length the length of the new array
   */
  // miniguava: Inlined from Platform.java
  public static <T> T[] newArray(T[] reference, int length) {
    Class<?> type = reference.getClass().getComponentType();

    // the cast is safe because
    // result.getClass() == reference.getClass().getComponentType()
    @SuppressWarnings("unchecked")
    T[] result = (T[]) Array.newInstance(type, length);
    return result;
  }

  /** GWT safe version of Arrays.copyOf. */
  static <T> T[] arraysCopyOf(T[] original, int newLength) {
    T[] copy = newArray(original, newLength);
    System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
    return copy;
  }

  static Object[] checkElementsNotNull(Object... array) {
    return checkElementsNotNull(array, array.length);
  }

  static Object[] checkElementsNotNull(Object[] array, int length) {
    for (int i = 0; i < length; i++) {
      checkElementNotNull(array[i], i);
    }
    return array;
  }

  // We do this instead of Preconditions.checkNotNull to save boxing and array
  // creation cost.
  static Object checkElementNotNull(Object element, int index) {
    if (element == null) {
      throw new NullPointerException("at index " + index);
    }
    return element;
  }
}
