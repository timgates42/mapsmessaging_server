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

package org.maps.messaging.engine.destination.subscription;

import java.io.IOException;
import org.maps.logging.LogMessages;
import org.maps.logging.Logger;
import org.maps.logging.LoggerFactory;
import org.maps.messaging.api.features.ClientAcknowledgement;
import org.maps.messaging.engine.destination.DestinationImpl;
import org.maps.messaging.engine.destination.subscription.transaction.AcknowledgementController;
import org.maps.messaging.engine.destination.subscription.transaction.AutoAcknowledgementController;
import org.maps.messaging.engine.destination.subscription.transaction.ClientAcknowledgementController;
import org.maps.messaging.engine.destination.subscription.transaction.ClientCreditManager;
import org.maps.messaging.engine.destination.subscription.transaction.CreditManager;
import org.maps.messaging.engine.destination.subscription.transaction.FixedCreditManager;
import org.maps.messaging.engine.destination.subscription.transaction.IndividualAcknowledgementController;
import org.maps.messaging.engine.session.SessionImpl;
import org.maps.selector.ParseException;
import org.maps.selector.SelectorParser;
import org.maps.selector.operators.ParserExecutor;

public abstract class SubscriptionBuilder {
  private static final Logger logger = LoggerFactory.getLogger(SubscriptionBuilder.class);

  protected final SubscriptionContext context;
  protected final DestinationImpl destination;
  protected final ParserExecutor parserExecutor;

  protected SubscriptionBuilder( DestinationImpl destination, SubscriptionContext context) throws IOException {
    this.context = context;
    this.destination = destination;
    this.parserExecutor = compileParser(context.getSelector());
  }

  protected SubscriptionBuilder( DestinationImpl destination, SubscriptionContext context, SubscriptionContext parent) throws IOException {
    this.context = context;
    this.destination = destination;
    String selector = context.getSelector();
    String parentSelector = parent.getSelector();
    this.parserExecutor = compileParser(combineSelectors(selector, parentSelector));
  }

  private String combineSelectors(String lhs, String rhs){
    StringBuilder sb = new StringBuilder();
    boolean hasLhs = false;
    if(lhs != null && lhs.length() > 0){
      sb.append(lhs).append(" ");
      hasLhs = true;
    }

    if(rhs != null && rhs.length() > 0){
      if(hasLhs){
        sb.append( " and ");
      }
      sb.append(rhs);
    }
    return sb.toString();
  }

  public abstract Subscription construct(SessionImpl session, String sessionId) throws IOException;

  protected AcknowledgementController createAcknowledgementController(ClientAcknowledgement acknowledgementController) {
    CreditManager creditManager = createCreditManager(context);
    switch (acknowledgementController) {
      case INDIVIDUAL:
        return new IndividualAcknowledgementController(creditManager);

      case BLOCK:
        return new ClientAcknowledgementController(creditManager);

      case AUTO:
      default:
        return new AutoAcknowledgementController(creditManager);
    }
  }

  protected CreditManager createCreditManager(SubscriptionContext context){
    switch(context.getCreditHandler()){
      case CLIENT:
        return new ClientCreditManager(context.getReceiveMaximum());

      case AUTO:
      default:
        return new FixedCreditManager(context.getReceiveMaximum());
    }
  }

  protected ParserExecutor compileParser(String selector) throws IOException {
    ParserExecutor parser;
    if (selector != null && selector.length() > 0) {
      try {
        parser = SelectorParser.doParse(selector, null);
      } catch (ParseException e) {
        logger.log(LogMessages.SUBSCRIPTION_MGR_SELECTOR_EXCEPTION, context.getSelector(), e);
        throw new IOException("Failed to parse selector", e);
      }
    }
    else{
      parser = null;
    }
    return parser;
  }


}
