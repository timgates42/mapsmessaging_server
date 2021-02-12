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

package org.maps.messaging.engine.destination.subscription.tasks;

import java.util.concurrent.atomic.AtomicLong;
import org.maps.messaging.engine.destination.DestinationImpl;
import org.maps.messaging.engine.destination.subscription.Subscription;
import org.maps.messaging.engine.destination.subscription.SubscriptionContext;
import org.maps.messaging.engine.destination.subscription.SubscriptionController;
import org.maps.messaging.engine.destination.subscription.impl.ClientSubscribedEventManager;
import org.maps.messaging.engine.tasks.EngineTask;
import org.maps.messaging.engine.tasks.Response;
import org.maps.messaging.engine.tasks.SubscriptionResponse;

public class SubscriptionTask extends EngineTask {
  private final DestinationImpl destination;
  private final SubscriptionController controller;
  private final SubscriptionContext context;
  private final AtomicLong counter;

  public SubscriptionTask(SubscriptionController controller, SubscriptionContext context,  DestinationImpl destination, AtomicLong counter){
    super();
    this.controller = controller;
    this.destination = destination;
    this.context = context;
    this.counter = counter;
  }

  @Override
  public Response taskCall() throws Exception {
    Subscription subscription;
    try {
      if(context.isBrowser() && destination.getResourceType().isQueue()){
        // We are now looking at the base queue so we need to find "shared_<Name Of Queue>_normal"
        subscription = controller.createBrowserSubscription(context, destination.getSubscription(destination.getName()), destination);
      }
      else {
        subscription = controller.get(destination);
        if (subscription != null) {
          subscription.addContext(context);
        } else {
          subscription = controller.createSubscription(context, destination);
        }
      }
    } finally {
      counter.decrementAndGet();
    }
    return new SubscriptionResponse( new ClientSubscribedEventManager(destination, subscription));
  }
}
