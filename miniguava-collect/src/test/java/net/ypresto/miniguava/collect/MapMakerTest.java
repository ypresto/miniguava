/*
 * Copyright (C) 2011 The Guava Authors
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

import com.google.common.testing.NullPointerTester;

import junit.framework.TestCase;

/**
 * @author Charles Fry
 */
public class MapMakerTest extends TestCase {

  public void testNullParameters() throws Exception {
    NullPointerTester tester = new NullPointerTester();
    tester.testAllPublicInstanceMethods(new MapMaker());
  }

  /*
   * TODO(cpovirk): eliminate duplication between these tests and those in LegacyMapMakerTests and
   * anywhere else
   */

  /** Tests for the builder. */
  public static class MakerTest extends TestCase {
    public void testInitialCapacity_negative() {
      MapMaker maker = new MapMaker();
      try {
        maker.initialCapacity(-1);
        fail();
      } catch (IllegalArgumentException expected) {
      }
    }

    // TODO(cpovirk): enable when ready
    public void xtestInitialCapacity_setTwice() {
      MapMaker maker = new MapMaker().initialCapacity(16);
      try {
        // even to the same value is not allowed
        maker.initialCapacity(16);
        fail();
      } catch (IllegalArgumentException expected) {
      }
    }
  }
}
