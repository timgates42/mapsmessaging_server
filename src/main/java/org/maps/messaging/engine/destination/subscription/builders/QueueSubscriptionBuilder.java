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

package org.maps.messaging.engine.destination.subscription.builders;

import java.io.IOException;
import org.maps.messaging.engine.destination.DestinationImpl;
import org.maps.messaging.engine.destination.subscription.Subscription;
import org.maps.messaging.engine.destination.subscription.SubscriptionContext;
import org.maps.messaging.engine.session.SessionImpl;

public class QueueSubscriptionBuilder extends CommonSubscriptionBuilder {

  public QueueSubscriptionBuilder(DestinationImpl destination, SubscriptionContext context) throws IOException {
    super(destination, context);
  }

  @Override
  public Subscription construct(SessionImpl session, String sessionId) throws IOException {
    return construct(destination.getName(), session, sessionId);
  }
}
