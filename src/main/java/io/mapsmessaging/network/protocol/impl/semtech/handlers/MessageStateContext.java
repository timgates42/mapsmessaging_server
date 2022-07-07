package io.mapsmessaging.network.protocol.impl.semtech.handlers;

import io.mapsmessaging.api.MessageEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

public class MessageStateContext {

  private final Map<Integer, MessageEvent> inflight;


  public MessageStateContext() {
    inflight = new ConcurrentHashMap<>();
  }

  public void complete(int token) {
    MessageEvent messageEvent = inflight.remove(token);
    if (messageEvent != null) {
      messageEvent.getCompletionTask().run();
    }
  }

  public void push(int token, @NotNull @NonNull MessageEvent messageEvent) {
    inflight.put(token, messageEvent);
  }
}
