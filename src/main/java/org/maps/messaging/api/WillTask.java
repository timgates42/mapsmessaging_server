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

package org.maps.messaging.api;

import org.jetbrains.annotations.NotNull;
import org.maps.messaging.api.features.QualityOfService;
import org.maps.messaging.engine.session.will.WillTaskImpl;

/**
 * THis class manages the updating of the last Will task. The Will task is executed if the client is dosconnected
 * rather than closes a session. This enables a message to be sent to notify others of a disruption to message flow
 */
public class WillTask {

  private final WillTaskImpl willTaskImpl;

  WillTask(@NotNull WillTaskImpl impl) {
    willTaskImpl = impl;
  }

  /**
   * Update the data to send when the session is disconnected
   *
   * @param payload to send
   */
  public void updateMessage(@NotNull byte[] payload) {
    willTaskImpl.updateMessage(payload);
  }

  /**
   * Cancel and close the will task so it is removed from the session disconnected
   */
  public void cancel() {
    willTaskImpl.cancel();
  }

  /**
   * Updates the Quality Of Service for the message to be sent
   *
   * @see QualityOfService
   *
   * @param qos QualityOfService to be used
   */
  public void updateQoS(@NotNull QualityOfService qos) {
    willTaskImpl.updateQoS(qos);
  }

  /**
   * Updates the destination to send the message to
   *
   * @param destination name of a valid destination to send the message to
   */
  public void updateTopic(@NotNull String destination) {
    willTaskImpl.updateTopic(destination);
  }
}
