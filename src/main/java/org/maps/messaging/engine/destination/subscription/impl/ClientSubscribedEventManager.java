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

package org.maps.messaging.engine.destination.subscription.impl;

import java.util.List;
import org.maps.messaging.api.SubscribedEventManager;
import org.maps.messaging.engine.destination.DestinationImpl;
import org.maps.messaging.engine.destination.subscription.SubscriptionContext;
import org.maps.messaging.engine.destination.subscription.tasks.CreditUpdateTask;
import org.maps.messaging.engine.destination.subscription.tasks.SubscriptionTransactionTask;

public class ClientSubscribedEventManager implements SubscribedEventManager {

  private final DestinationImpl destination;
  private SubscribedEventManager subscription;

  public ClientSubscribedEventManager(DestinationImpl destination, SubscribedEventManager subscription) {
    this.subscription = subscription;
    this.destination = destination;
  }

  @Override
  public void rollbackReceived(long messageId) {
    destination.submit(new SubscriptionTransactionTask(subscription, messageId, false));
  }

  @Override
  public void ackReceived(long messageId) {
    destination.submit(new SubscriptionTransactionTask(subscription, messageId, true));
  }

  @Override
  public SubscriptionContext getContext() {
    return subscription.getContext();
  }

  @Override
  public List<SubscriptionContext> getContexts() {
    return subscription.getContexts();
  }

  @Override
  public void updateCredit(int credit) {
    destination.submit(new CreditUpdateTask(subscription, credit));
  }

  @Override
  public boolean isEmpty() {
    return subscription.isEmpty();
  }

  @Override
  public int getPending() {
    return subscription.getPending();
  }


  @Override
  public int getDepth() {
    return subscription.getDepth();
  }

  public void setSubscription(SubscribedEventManager subscription) {
    this.subscription = subscription;
  }

}
