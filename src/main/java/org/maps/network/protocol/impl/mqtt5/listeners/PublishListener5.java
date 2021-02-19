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

package org.maps.network.protocol.impl.mqtt5.listeners;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.maps.logging.LogMessages;
import org.maps.messaging.api.Destination;
import org.maps.messaging.api.MessageBuilder;
import org.maps.messaging.api.Session;
import org.maps.messaging.api.Transaction;
import org.maps.messaging.api.TransactionException;
import org.maps.messaging.api.features.Priority;
import org.maps.messaging.api.features.QualityOfService;
import org.maps.messaging.api.message.Message;
import org.maps.messaging.api.message.TypedData;
import org.maps.network.io.EndPoint;
import org.maps.network.protocol.ProtocolImpl;
import org.maps.network.protocol.ProtocolMessageTransformation;
import org.maps.network.protocol.impl.mqtt.packet.MQTTPacket;
import org.maps.network.protocol.impl.mqtt.packet.MalformedException;
import org.maps.network.protocol.impl.mqtt5.MQTT5Protocol;
import org.maps.network.protocol.impl.mqtt5.TopicAliasMapping;
import org.maps.network.protocol.impl.mqtt5.packet.Disconnect5;
import org.maps.network.protocol.impl.mqtt5.packet.MQTTPacket5;
import org.maps.network.protocol.impl.mqtt5.packet.PubAck5;
import org.maps.network.protocol.impl.mqtt5.packet.PubRec5;
import org.maps.network.protocol.impl.mqtt5.packet.Publish5;
import org.maps.network.protocol.impl.mqtt5.packet.PublishMonitorPacket5;
import org.maps.network.protocol.impl.mqtt5.packet.StatusCode;
import org.maps.network.protocol.impl.mqtt5.packet.properties.ContentType;
import org.maps.network.protocol.impl.mqtt5.packet.properties.CorrelationData;
import org.maps.network.protocol.impl.mqtt5.packet.properties.MessageExpiryInterval;
import org.maps.network.protocol.impl.mqtt5.packet.properties.MessageProperty;
import org.maps.network.protocol.impl.mqtt5.packet.properties.MessagePropertyFactory;
import org.maps.network.protocol.impl.mqtt5.packet.properties.PayloadFormatIndicator;
import org.maps.network.protocol.impl.mqtt5.packet.properties.ResponseTopic;
import org.maps.network.protocol.impl.mqtt5.packet.properties.TopicAlias;
import org.maps.network.protocol.impl.mqtt5.packet.properties.UserProperty;
import org.maps.utilities.threads.SimpleTaskScheduler;

public class PublishListener5 extends PacketListener5 {

  public static Message createMessage(String sessionId, Collection<MessageProperty> properties, Priority priority, boolean isRetain, byte[] payload, QualityOfService qos, ProtocolMessageTransformation transformation) {
    HashMap<String, String> meta = new LinkedHashMap<>();
    meta.put("protocol", "MQTT");
    meta.put("version", "5");
    meta.put("time_ms", "" + System.currentTimeMillis());
    meta.put("sessionId", sessionId);

    MessageBuilder mb = new MessageBuilder();
    mb.setPriority(priority)
        .setRetain(isRetain)
        .setOpaqueData(payload)
        .setMeta(meta)
        .setQoS(qos)
        .setTransformation(transformation)
        .storeOffline(qos.isStoreOffLine());

    HashMap<String, TypedData> dataHashMap = new LinkedHashMap<>();
    for (MessageProperty property : properties) {
      switch (property.getId()) {
        case MessagePropertyFactory.PAYLOAD_FORMAT_INDICATOR:
          mb.setPayloadIndicator(((PayloadFormatIndicator) property).getPayloadFormatIndicator());
          break;

        case MessagePropertyFactory.MESSAGE_EXPIRY_INTERVAL:
          mb.setMessageExpiryInterval(
              ((MessageExpiryInterval) property).getMessageExpiryInterval(),  TimeUnit.SECONDS);
          break;

        case MessagePropertyFactory.CONTENT_TYPE:
          mb.setContentType(((ContentType) property).getContentType());
          break;

        case MessagePropertyFactory.CORRELATION_DATA:
          mb.setCorrelationData(((CorrelationData) property).getCorrelationData());
          break;

        case MessagePropertyFactory.USER_PROPERTY:
          UserProperty property1 = (UserProperty) property;
          dataHashMap.put(
              property1.getUserPropertyName(), new TypedData(property1.getUserPropertyValue()));
          break;

        case MessagePropertyFactory.RESPONSE_TOPIC:
          ResponseTopic responseTopic = (ResponseTopic) property;
          mb.setResponseTopic(responseTopic.getResponseTopicString());
          break;

        default:
          break; // Nothing to do, it might be that the properties are inside a different packet
      }
    }
    return mb.setDataMap(dataHashMap).build();
  }

  // unfortunately MQTT publishing has a large number of permutations and exceeds the base limit
  @java.lang.SuppressWarnings("squid:S3776")
  @Override
  public MQTTPacket5 handlePacket(MQTTPacket5 mqttPacket, Session session, EndPoint endPoint, ProtocolImpl protocol) throws MalformedException {
    Publish5 publish = (Publish5) mqttPacket;
    PublishMonitorPacket5 response = null;
    if (publish.getQos().equals(QualityOfService.AT_LEAST_ONCE)) {
      response = new PubAck5(publish.getPacketId(), StatusCode.SUCCESS);
    } else if (publish.getQos().equals(QualityOfService.EXACTLY_ONCE)) {
      response = new PubRec5(publish.getPacketId());
      long id = publish.getPacketId();
      List<Long> outstanding = ((MQTT5Protocol) protocol).getClientOutstanding();
      if (outstanding.size() >= ((MQTT5Protocol) protocol).getServerReceiveMaximum()) {
        logger.log(LogMessages.MQTT5_EXCEED_MAXIMUM, outstanding.size(), ((MQTT5Protocol) protocol).getServerReceiveMaximum());
        ((MQTT5Protocol) protocol).setClosing(true);
        Disconnect5 disconnect5 = new Disconnect5(StatusCode.RECEIVE_MAXIMUM_EXCEEDED);
        disconnect5.setCallback(() -> SimpleTaskScheduler.getInstance().schedule(() -> {
          try {
            protocol.close();
          } catch (IOException e) {
            // Ignore, we are in an error state now
          }
        }, 100, TimeUnit.MILLISECONDS));
        return disconnect5;
      }
      outstanding.add(id);
    }

    if (!publish.getDestinationName().startsWith("$")) {
      TopicAliasMapping topicAliasMapping = ((MQTT5Protocol) protocol).getClientTopicAliasMapping();
      String destinationName = publish.getDestinationName();
      if (destinationName == null || destinationName.length() == 0) {
        for (MessageProperty property : publish.getProperties().values()) {
          if (property.getId() == MessagePropertyFactory.TOPIC_ALIAS) {
            TopicAlias topicAlias = (TopicAlias) property;
            destinationName = topicAliasMapping.find(topicAlias.getTopicAlias());
            publish.getProperties().remove(topicAlias); // Processed need to remove it
            break;
          }
        }
        if (destinationName == null) {
          Disconnect5 disconnect5 = new Disconnect5(StatusCode.TOPIC_ALIAS_INVALID);
          disconnect5.setCallback(() -> SimpleTaskScheduler.getInstance().schedule(() -> {
            try {
              protocol.close();
            } catch (IOException e) {
              // we are in the midst of a close, more on
            }
          }, 100, TimeUnit.MILLISECONDS));
          ((MQTT5Protocol) protocol).setClosing(true);
          return disconnect5;
        }
      } else {
        for (MessageProperty property : publish.getProperties().values()) {
          if (property.getId() == MessagePropertyFactory.TOPIC_ALIAS) {
            TopicAlias topicAlias = (TopicAlias) property;
            if (!topicAliasMapping.add(destinationName, topicAlias)) {
              Disconnect5 disconnect5 = new Disconnect5(StatusCode.TOPIC_ALIAS_INVALID);
              disconnect5.setCallback(() -> SimpleTaskScheduler.getInstance().schedule(() -> {
                try {
                  protocol.close();
                } catch (IOException e) {
                  // we are in the midst of a close, more on
                }
              }, 100, TimeUnit.MILLISECONDS));
              ((MQTT5Protocol) protocol).setClosing(true);
              return disconnect5;
            }
            publish.getProperties().remove(topicAlias); // Processed need to remove it
            break;
          }
        }
      }

      String duplicateReport = publish.getProperties().getDuplicateReport();
      if (duplicateReport.length() > 0) {
        logger.log(LogMessages.MQTT5_DUPLICATE_PROPERTIES_DETECTED, duplicateReport);
      }

      try {
        Destination destination = session.findDestination(destinationName);
        int sent = 0;
        if(destination != null) {
          Message message =
              createMessage(
                  session.getName(),
                  publish.getProperties().values(),
                  publish.getPriority(),
                  publish.isRetain(),
                  publish.getPayload(),
                  publish.getQos(),
                  protocol.getTransformation());
          sent = processMessage(message, publish, session, response, destination);
        }

        if (response != null) {
          if (sent == 0) {
            response.setStatusCode(StatusCode.NO_MATCHING_SUBSCRIBERS);
          }
          else if(sent < 0){
            response.setStatusCode(StatusCode.PACKET_IDENTIFIER_INUSE);
          }
        }
      } catch (IOException e) {
        try {
          endPoint.close();
        } catch (IOException ioException) {
          // we are in the midst of a close, more on
        }
        throw new MalformedException("[MQTT-3.3.5-2]");
      }
    } else {
      if (response != null) {
        response.setStatusCode(StatusCode.NOT_AUTHORISED); // Can not publish to $ topics
      }
    }
    return response;
  }

  private int processMessage(Message message, Publish5 publish, Session session, MQTTPacket response, Destination destination) throws IOException {
    if(response != null){
      Transaction transaction;
      try {

        transaction = session.startTransaction(session.getName()+"_"+publish.getPacketId());
      } catch (TransactionException e) {
        logger.log(LogMessages.MQTT_DUPLICATE_EVENT_RECEIVED, publish.getPacketId());
        return -1;
      }
      transaction.add(destination, message);
      if(publish.getQos().equals(QualityOfService.AT_LEAST_ONCE)){
        transaction.commit();
      }
      return 1;
    }
    else {
      return destination.storeMessage(message);
    }
  }
}
