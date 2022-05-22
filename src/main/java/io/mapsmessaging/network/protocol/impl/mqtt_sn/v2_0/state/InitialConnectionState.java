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

package io.mapsmessaging.network.protocol.impl.mqtt_sn.v2_0.state;

import io.mapsmessaging.api.Session;
import io.mapsmessaging.api.SessionContextBuilder;
import io.mapsmessaging.network.io.EndPoint;
import io.mapsmessaging.network.protocol.impl.mqtt_sn.DefaultConstants;
import io.mapsmessaging.network.protocol.impl.mqtt_sn.v1_2.MQTT_SNProtocol;
import io.mapsmessaging.network.protocol.impl.mqtt_sn.v1_2.packet.MQTT_SNPacket;
import io.mapsmessaging.network.protocol.impl.mqtt_sn.v1_2.packet.ReasonCodes;
import io.mapsmessaging.network.protocol.impl.mqtt_sn.v1_2.packet.WillTopicRequest;
import io.mapsmessaging.network.protocol.impl.mqtt_sn.v1_2.state.State;
import io.mapsmessaging.network.protocol.impl.mqtt_sn.v1_2.state.StateEngine;
import io.mapsmessaging.network.protocol.impl.mqtt_sn.v2_0.packet.ConnAck;
import io.mapsmessaging.network.protocol.impl.mqtt_sn.v2_0.packet.Connect;
import io.mapsmessaging.network.protocol.transformation.TransformationManager;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Protocol dictates that we need to a) Receive a Connect packet b) Respond with a Will Topic Request c) Receive a Will Topic Response d) Respond with a Will Message Request e)
 * Receive a Will Message response f) At this point we can establish a valid MQTT session and we respond with a ConAck response
 */
public class InitialConnectionState implements State {

  @Override
  public String getName() {
    return "Initial";
  }

  @Override
  public MQTT_SNPacket handleMQTTEvent(MQTT_SNPacket mqtt, Session oldSession, EndPoint endPoint, MQTT_SNProtocol protocol, StateEngine stateEngine) {
    if (mqtt.getControlPacketId() == MQTT_SNPacket.CONNECT) {
      Connect connect = (Connect) mqtt;
      stateEngine.setMaxBufferSize(connect.getMaxPacketSize());
      SessionContextBuilder scb = new SessionContextBuilder(connect.getClientId(), protocol);
      scb.setPersistentSession(true);
      scb.setResetState(connect.isCleanStart());
      scb.setKeepAlive(connect.getKeepAlive());
      scb.setReceiveMaximum(DefaultConstants.RECEIVE_MAXIMUM);
      scb.setSessionExpiry(connect.getSessionExpiry());
      if (connect.isWill()) {
        stateEngine.setSessionContextBuilder(scb);
        WillTopicRequest topicRequest = new WillTopicRequest();
        InitialWillTopicState nextState = new InitialWillTopicState(topicRequest);
        stateEngine.setState(nextState);
        return topicRequest;
      } else{
        CompletableFuture<Session> sessionFuture = stateEngine.createSession(scb, protocol);
        sessionFuture.thenApply(session ->{
          protocol.setSession(session);
          ConnAck response = new ConnAck(ReasonCodes.Success, 0, scb.getId(), session.isRestored());
          response.setCallback(session::resumeState);
          protocol.setTransformation(TransformationManager.getInstance().getTransformation(protocol.getName(), session.getSecurityContext().getUsername()));
          try {
            session.login();
            stateEngine.setState(new ConnectedState(response));
            protocol.writeFrame(response);
            return session;
          } catch (IOException e) {
            sendErrorResponse(protocol);
            return null;
          }
        }).exceptionally(exception -> {
          sendErrorResponse(protocol);
          return null;
        });
      }
    }
    return null;
  }

  private void sendErrorResponse(MQTT_SNProtocol protocol){
    io.mapsmessaging.network.protocol.impl.mqtt_sn.v1_2.packet.ConnAck response = new io.mapsmessaging.network.protocol.impl.mqtt_sn.v1_2.packet.ConnAck(ReasonCodes.NotSupported);
    response.setCallback(() -> {
      try {
        protocol.close();
      } catch (IOException ioException) {
        // we can ignore this, we are about to close it since we have no idea what it is
      }
    });
  }
}
