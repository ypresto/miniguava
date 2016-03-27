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

import static net.ypresto.miniguava.base.Preconditions.checkArgument;

import net.ypresto.miniguava.annotations.MiniGuavaSpecific;
import net.ypresto.miniguava.base.internal.InternalImmutableEmulation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// miniguava: A collection of helper methods used in this module.
@MiniGuavaSpecific
class InternalUtils {
  private InternalUtils() {}

  /**
   * Returns a map from the ith element of list to i.
   */
  // miniguava: From Maps.java and modified not to use ImmutableMap.
  // miniguava: Please update compare.InternalUtils.indexMap too.
  @MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "collect.Maps")
  static <E> Map<E, Integer> indexMap(Collection<E> list) {
    InternalImmutableEmulation.checkElementsNotNull(list);
    Map<E, Integer> map = new HashMap<E, Integer>(list.size());
    int i = 0;
    for (E e : list) {
      checkArgument(!map.containsKey(e), "Multiple elements with same value: %s", e);
      map.put(e, i++);
    }
    return Collections.unmodifiableMap(map);
  }
}
