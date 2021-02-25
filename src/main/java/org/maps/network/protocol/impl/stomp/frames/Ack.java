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

package org.maps.network.protocol.impl.stomp.frames;


/**
 * Implements the STOMP Connect frame as per https://stomp.github.io/stomp-specification-1.2.html#ACK
 */
public class Ack extends ClientSubscriptionTransaction {

  @Override
  byte[] getCommand() {
    return "ACK".getBytes();
  }

  @Override
  public Frame instance() {
    return new Ack();
  }

  @Override
  public String toString() {
    return "STOMP Ack[ Transaction:" + getTransaction() + ", Header:" + getHeaderAsString() + "]";
  }
}
