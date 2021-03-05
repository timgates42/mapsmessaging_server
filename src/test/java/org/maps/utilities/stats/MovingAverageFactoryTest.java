/*
 *    Copyright [ 2020 - 2021 ] [Matthew Buckton]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *
 */

package org.maps.utilities.stats;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.maps.utilities.stats.MovingAverageFactory.ACCUMULATOR;

class MovingAverageFactoryTest {

  @Test
  void getInstance() {
    assertNotNull(MovingAverageFactory.getInstance());
  }

  @Test
  void close() {
    LinkedMovingAverages linked = MovingAverageFactory.getInstance().createLinked(ACCUMULATOR.ADD, "Test", 1, 10, 6,  TimeUnit.SECONDS, "Tests");
    assertNotNull(linked);
    MovingAverageFactory.getInstance().close(linked);
    for(LinkedMovingAverages movingAverage:MovingAverageFactory.getInstance().movingAverages){
      assertNotEquals(linked, movingAverage);
    }
  }

  @Test
  void createLinked() {
    LinkedMovingAverages linked = MovingAverageFactory.getInstance().createLinked(ACCUMULATOR.ADD, "Test", 1, 10, 6,  TimeUnit.SECONDS, "Tests");
    assertNotNull(linked);
    MovingAverageFactory.getInstance().close(linked);
  }

  @Test
  void testCreateLinked() {
    int[] entries = {1, 2, 4, 8, 16, 32, 64};
    LinkedMovingAverages linked = MovingAverageFactory.getInstance().createLinked(ACCUMULATOR.ADD, "Test", entries,  TimeUnit.SECONDS, "Tests");
    assertNotNull(linked);
    MovingAverageFactory.getInstance().close(linked);
  }

  @Test
  void accumulator(){
    assertEquals("Average", ACCUMULATOR.AVE.getName());
    assertEquals("Adder", ACCUMULATOR.ADD.getName());
    assertEquals("Difference", ACCUMULATOR.DIFF.getName());
    assertEquals("Summer", ACCUMULATOR.SUM.getName());
  }
}