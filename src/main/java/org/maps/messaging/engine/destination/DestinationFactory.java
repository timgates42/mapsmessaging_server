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

package org.maps.messaging.engine.destination;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.maps.messaging.api.features.DestinationType;
import org.maps.messaging.engine.destination.subscription.SubscriptionController;

public interface DestinationFactory {

  String getRoot();

  List<DestinationImpl> getDestinations();

  DestinationImpl find(String name);

  DestinationImpl findOrCreate(String name) throws IOException;

  DestinationImpl findOrCreate(String name, DestinationType destinationType) throws IOException;

  DestinationImpl create(@NotNull String name, @NotNull DestinationType destinationType) throws IOException;

  DestinationImpl delete(DestinationImpl destinationImpl);

  Map<String, DestinationImpl> get();

  void addListener(DestinationManagerListener subscriptionController);

  void removeListener(DestinationManagerListener subscriptionController);
}
