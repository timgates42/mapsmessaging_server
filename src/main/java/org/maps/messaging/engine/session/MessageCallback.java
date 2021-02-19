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

package org.maps.messaging.engine.session;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.maps.messaging.api.SubscribedEventManager;
import org.maps.messaging.api.message.Message;
import org.maps.messaging.engine.destination.DestinationImpl;

public interface MessageCallback {

  default void sendMessage(@NonNull @NotNull DestinationImpl destination, @NonNull @NotNull SubscribedEventManager subscription,@NonNull @NotNull Message message,@NonNull @NotNull Runnable completionTask) {

  }

}
