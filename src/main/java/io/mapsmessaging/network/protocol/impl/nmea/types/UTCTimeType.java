/*
 *    Copyright [ 2020 - 2022 ] [Matthew Buckton]
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
 */

package io.mapsmessaging.network.protocol.impl.nmea.types;

import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

public class UTCTimeType implements Type {

  private final OffsetTime time;

  public UTCTimeType(String utcTime) {
    double numeric = Double.parseDouble(utcTime);
    int hour = ((int) numeric / 10000) % 100;
    int min = ((int) numeric / 100) % 100;
    int sec = (int) numeric % 100;
    int nano = (int) (numeric * 1000) % 1000;
    nano = nano * 1000000;
    LocalTime localTime = LocalTime.of(hour, min, sec, nano);
    time = OffsetTime.of(localTime, ZoneOffset.UTC);
  }

  public OffsetTime getTime() {
    return time;
  }

  @Override
  public String toString() {
    return time.toString();
  }

  @Override
  public Object jsonPack() {
    return time;
  }
}
