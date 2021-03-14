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
import java.util.Iterator;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.maps.messaging.api.message.Message;
import org.maps.messaging.engine.destination.DestinationImpl;
import org.maps.messaging.engine.destination.subscription.Subscription;
import org.maps.messaging.engine.destination.subscription.SubscriptionBuilder;
import org.maps.messaging.engine.destination.subscription.SubscriptionContext;
import org.maps.messaging.engine.destination.subscription.impl.DestinationSubscription;
import org.maps.messaging.engine.destination.subscription.impl.SelectorDestinationSubscription;
import org.maps.messaging.engine.destination.subscription.state.IteratorStateManagerImpl;
import org.maps.messaging.engine.destination.subscription.state.MessageStateManager;
import org.maps.messaging.engine.destination.subscription.state.MessageStateManagerImpl;
import org.maps.messaging.engine.destination.subscription.transaction.AcknowledgementController;
import org.maps.messaging.engine.session.SessionImpl;
import org.maps.messaging.engine.tasks.EngineTask;
import org.maps.messaging.engine.tasks.Response;
import org.maps.messaging.engine.tasks.VoidResponse;
import org.maps.selector.ParseException;
import org.maps.selector.operators.ParserExecutor;

public class BrowserSubscriptionBuilder extends SubscriptionBuilder {

  private final DestinationSubscription parent;

  public BrowserSubscriptionBuilder(DestinationImpl destination, SubscriptionContext context, DestinationSubscription parent) throws IOException {
    super(destination, context, parent.getContext());
    this.parent = parent;
  }

  @Override
  public Subscription construct(SessionImpl session, String sessionId) throws IOException {
    AcknowledgementController acknowledgementController = createAcknowledgementController(context.getAcknowledgementController());
    MessageStateManager stateManager;
    if (parserExecutor == null) {
      stateManager = new IteratorStateManagerImpl(context.getAlias(), (MessageStateManagerImpl) parent.getMessageStateManager(), true);
      return new DestinationSubscription(destination, context, session, sessionId, acknowledgementController, stateManager);
    } else {
      if(selectorHasChanged(parent.getContext().getSelector(), context.getSelector())){
        // Need task to filter the messages from the parent to the current state manager
        stateManager = new IteratorStateManagerImpl(context.getAlias(), (MessageStateManagerImpl) parent.getMessageStateManager(), false);
        StateManagerFilterTask task = new StateManagerFilterTask(destination, parent.getMessageStateManager(), stateManager, parserExecutor);
        destination.submit(task);
      }
      else{
        stateManager = new IteratorStateManagerImpl(context.getAlias(), (MessageStateManagerImpl) parent.getMessageStateManager(), true);
      }
      return new SelectorDestinationSubscription(destination, context, session, sessionId, acknowledgementController, stateManager, parserExecutor);
    }
  }


  private boolean selectorHasChanged(String parentSelector, String selector) throws IOException {
    if(selector == null || selector.length() == 0){
      return false;
    }
    if(parentSelector == null || parentSelector.length() == 0){
      return true; // Its true, since the selector has in fact been set by the new one
    }
    ParserExecutor executor = compileParser(selector);
    ParserExecutor parentExecutor = compileParser(parentSelector);
    return !parentExecutor.equals(executor);
  }


  private static class StateManagerFilterTask extends EngineTask {

    private final DestinationImpl destination;
    private final Iterator<Long> source;
    private final MessageStateManager stateManager;
    private final ParserExecutor executor;

    public StateManagerFilterTask(@NonNull @NotNull DestinationImpl destination,@NonNull @NotNull MessageStateManager parent,@NonNull @NotNull MessageStateManager child,@NonNull @NotNull ParserExecutor executor){
      this.destination = destination;
      source = parent.getAll().iterator();
      stateManager = child;
      this.executor = executor;
    }

    protected StateManagerFilterTask(StateManagerFilterTask task){
      destination = task.destination;
      source = task.source;
      stateManager = task.stateManager;
      executor = task.executor;
    }

    @Override
    public Response taskCall() throws IOException, ParseException {
      long endTime = System.currentTimeMillis() + 100;
      boolean interrupted = false;
      while(System.currentTimeMillis() < endTime && source.hasNext() && !destination.isClosed() && !interrupted) {
        long nextMessage = source.next();
        Message message = destination.getMessage(nextMessage);
        if (message != null && executor.evaluate(message)) {
          stateManager.register(message); // Message matches the current selector
        }
        if(Thread.currentThread().isInterrupted()){
          interrupted = true;
        }
      }
      if(source.hasNext() && !destination.isClosed()){
        destination.submit(new StateManagerFilterTask(this));
      }
      return new VoidResponse();
    }
  }
}
