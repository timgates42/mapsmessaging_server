/*
 *
 *   Copyright [ 2020 - 2021 ] [Matthew Buckton]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.maps.utilities.collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.maps.BaseTest;
import org.maps.utilities.collections.bitset.ByteBufferBitSetFactoryImpl;

public class NaturalOrderedLongListTest extends BaseTest {


  @Test
  public void simpleTest(){
    ByteBufferBitSetFactoryImpl factory = new ByteBufferBitSetFactoryImpl(4096);
    NaturalOrderedLongQueue noll = new NaturalOrderedLongQueue(0, factory);
    for(long x=8192;x<2*8192;x++) {
      noll.offer(x);
    }

    for(long x=0;x<8192;x++) {
      noll.offer(x);
    }
    int counter = noll.size();
    int start = 0;
    while(!noll.isEmpty()){
      Assertions.assertEquals(start, noll.poll());
      start++;
    }
  }
}
