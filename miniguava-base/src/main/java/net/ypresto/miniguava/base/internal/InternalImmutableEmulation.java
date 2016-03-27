/*
 * Copyright (C) 2016 Yuya Tanaka
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

package net.ypresto.miniguava.base.internal;

import static net.ypresto.miniguava.base.Preconditions.checkNotNull;

import net.ypresto.miniguava.annotations.MiniGuavaSpecific;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * ImmutableList does not allows null.
 * This class helps emulate such a behavior without ImmutableList.
 *
 * @author Yuya Tanaka
 */
@MiniGuavaSpecific
public class InternalImmutableEmulation {
  @MiniGuavaSpecific
  public static void checkElementsNotNull(Collection<?> collection) {
    for (Object object : collection) {
      checkNotNull(object, "Elements in collection is null.");
    }
  }

  @MiniGuavaSpecific
  public static <T> List<T> immutableCopy(List<? extends T> list) {
    checkElementsNotNull(list);
    return Collections.unmodifiableList(new ArrayList<T>(list));
  }
}
