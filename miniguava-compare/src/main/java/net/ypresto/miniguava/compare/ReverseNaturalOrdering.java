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

package net.ypresto.miniguava.compare;

import static net.ypresto.miniguava.base.Preconditions.checkNotNull;

/** An ordering that uses the reverse of the natural order of the values. */
// miniguava: Removed serialization support.
@SuppressWarnings("unchecked") // TODO(kevinb): the right way to explain this??
final class ReverseNaturalOrdering extends Ordering<Comparable>  {
  static final ReverseNaturalOrdering INSTANCE = new ReverseNaturalOrdering();

  @Override
  public int compare(Comparable left, Comparable right) {
    checkNotNull(left); // right null is caught later
    if (left == right) {
      return 0;
    }

    return right.compareTo(left);
  }

  @Override
  public <S extends Comparable> Ordering<S> reverse() {
    return Ordering.natural();
  }

  @Override
  public String toString() {
    return "Ordering.natural().reverse()";
  }

  private ReverseNaturalOrdering() {}
}
