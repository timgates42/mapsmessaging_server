/*
 * Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.mapsmessaging.network.protocol.impl.semtech.json;

import lombok.Getter;
import lombok.Setter;

public class StatPacket {

  @Getter
  @Setter
  private String time;
  @Getter
  @Setter
  private double lati;
  @Getter
  @Setter
  private double longitude;
  @Getter
  @Setter
  private long alti;
  @Getter
  @Setter
  private long rxnb;
  @Getter
  @Setter
  private long rxok;
  @Getter
  @Setter
  private long rxfw;
  @Getter
  @Setter
  private double ackr;
  @Getter
  @Setter
  private long dwnb;
  @Getter
  @Setter
  private long txnb;
}

