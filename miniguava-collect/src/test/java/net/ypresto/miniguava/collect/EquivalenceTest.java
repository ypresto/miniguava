/*
 * Copyright (C) 2010 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * diOBJECTibuted under the License is diOBJECTibuted on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ypresto.miniguava.collect;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.EquivalenceTester;
import com.google.common.testing.NullPointerTester;

import junit.framework.TestCase;

import net.ypresto.miniguava.annotations.MiniGuavaSpecific;

/**
 * Unit test for {@link Equivalence}.
 *
 * @author Jige Yu
 */
@MiniGuavaSpecific(MiniGuavaSpecific.Reason.MOVED) // miniguava: Moved from base package.
public class EquivalenceTest extends TestCase {

  public void testEqualsEquivalent() {
    EquivalenceTester.of(toGuava(Equivalence.equals()))
        .addEquivalenceGroup(new Integer(42), 42)
        .addEquivalenceGroup("a")
        .test();
  }

  public void testIdentityEquivalent() {
    EquivalenceTester.of(toGuava(Equivalence.identity()))
        .addEquivalenceGroup(new Integer(42))
        .addEquivalenceGroup(new Integer(42))
        .addEquivalenceGroup("a")
        .test();
  }
  
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(Equivalence.equals(), Equivalence.equals())
        .addEqualityGroup(Equivalence.identity(), Equivalence.identity())
        .testEquals();
  }

  public void testNulls() {
    new NullPointerTester().testAllPublicStaticMethods(Equivalence.class);
    new NullPointerTester().testAllPublicInstanceMethods(Equivalence.equals());
    new NullPointerTester().testAllPublicInstanceMethods(Equivalence.identity());
  }

  @MiniGuavaSpecific
  private <T> com.google.common.base.Equivalence<T> toGuava(final Equivalence<T> equivalence) {
    return new com.google.common.base.Equivalence<T>() {
      @Override
      protected boolean doEquivalent(T a, T b) {
        return equivalence.doEquivalent(a, b);
      }

      @Override
      protected int doHash(T t) {
        return equivalence.doHash(t);
      }
    };
  }
}
