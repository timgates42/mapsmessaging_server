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

package org.maps.messaging.engine.destination.subscription.impl.shared;

import org.maps.logging.LogMessages;
import org.maps.messaging.api.message.Message;
import org.maps.messaging.engine.destination.DestinationImpl;
import org.maps.messaging.engine.destination.subscription.SubscriptionContext;
import org.maps.messaging.engine.destination.subscription.state.MessageStateManager;
import org.maps.messaging.engine.destination.subscription.transaction.AcknowledgementController;
import org.maps.selector.ParseException;
import org.maps.selector.operators.ParserExecutor;

public class SharedSelectorSubscription extends SharedSubscription {
  private final ParserExecutor parserExecutor;

  public SharedSelectorSubscription(DestinationImpl destinationImpl,
      SubscriptionContext info,
      ParserExecutor parserExecutor,
      String id,
      MessageStateManager messageStateManager,
      AcknowledgementController acknowledgementController,
      String name) {
    super(destinationImpl, info, id, messageStateManager, acknowledgementController, name);
    this.parserExecutor = parserExecutor;
  }

  @Override
  public int register(Message message) {
    try {
      if(parserExecutor == null || parserExecutor.evaluate(message)) {
        return super.register(message);
      }
    } catch (ParseException e) {
      logger.log(LogMessages.DESTINATION_SUBSCRIPTION_EXCEPTION_SELECTOR, parserExecutor.toString(), e);
    }
    return 0;
  }
}
