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

package io.mapsmessaging.api;

import io.mapsmessaging.api.message.Message;
import io.mapsmessaging.engine.destination.DestinationImpl;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class Metrics extends Destination {

  Metrics(@NonNull @NotNull DestinationImpl impl) {
    super(impl);
  }

  @Override
  public int storeMessage(@NonNull @NotNull Message message) throws IOException {
    // No we don't store events for metrics
    return 0;
  }

  @Override
  public long getStoredMessages() throws IOException {
    return 0; // We have no persistent stored messages
  }

}