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

import static net.ypresto.miniguava.base.Preconditions.checkNotNull;

import net.ypresto.miniguava.annotations.MiniGuavaSpecific;
import net.ypresto.miniguava.base.Predicate;

import java.util.Collection;

import javax.annotation.Nullable;

// miniguava: Removed Serializable, equality and toString support, because they are internal use only.
@MiniGuavaSpecific(value = MiniGuavaSpecific.Reason.COPIED, from = "base.Predicates")
final class InternalPredicates {
  private InternalPredicates() {}

  /** @see InternalPredicates#not(Predicate) */
  private static class NotPredicate<T> implements Predicate<T> {
    final Predicate<T> predicate;

    NotPredicate(Predicate<T> predicate) {
      this.predicate = checkNotNull(predicate);
    }

    @Override
    public boolean apply(@Nullable T t) {
      return !predicate.apply(t);
    }

    @Override
    public int hashCode() {
      return ~predicate.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (obj instanceof NotPredicate) {
        NotPredicate<?> that = (NotPredicate<?>) obj;
        return predicate.equals(that.predicate);
      }
      return false;
    }

    @Override
    public String toString() {
      return "Predicates.not(" + predicate + ")";
    }
  }

  /** @see InternalPredicates#in(Collection) */
  private static class InPredicate<T> implements Predicate<T> {
    private final Collection<?> target;

    private InPredicate(Collection<?> target) {
      this.target = checkNotNull(target);
    }

    @Override
    public boolean apply(@Nullable T t) {
      try {
        return target.contains(t);
      } catch (NullPointerException e) {
        return false;
      } catch (ClassCastException e) {
        return false;
      }
    }
  }

  /**
   * Returns a predicate that evaluates to {@code true} if the given predicate
   * evaluates to {@code false}.
   */
  public static <T> Predicate<T> not(Predicate<T> predicate) {
    return new NotPredicate<T>(predicate);
  }

  /**
   * Returns a predicate that evaluates to {@code true} if the object reference
   * being tested is a member of the given collection. It does not defensively
   * copy the collection passed in, so future changes to it will alter the
   * behavior of the predicate.
   *
   * <p>This method can technically accept any {@code Collection<?>}, but using
   * a typed collection helps prevent bugs. This approach doesn't block any
   * potential users since it is always possible to use {@code
   * Predicates.<Object>in()}.
   *
   * @param target the collection that may contain the function input
   */
  public static <T> Predicate<T> in(Collection<? extends T> target) {
    return new InPredicate<T>(target);
  }
}
