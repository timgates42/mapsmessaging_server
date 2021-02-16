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

package org.maps.network.protocol.impl.stomp;

import java.io.IOException;
import org.maps.network.io.EndPoint;
import org.maps.network.io.Packet;
import org.maps.network.protocol.ProtocolImpl;
import org.maps.network.protocol.ProtocolImplFactory;
import org.maps.network.protocol.detection.MultiByteArrayDetection;

public class StompProtocolFactory extends ProtocolImplFactory {

  private static final byte[][] stompConnect = {"CONNECT\n".getBytes(), "STOMP\n".getBytes()};

  public StompProtocolFactory() {
    super("STOMP", "STOMP protocol support as per https://stomp.github.io/ ",new MultiByteArrayDetection(stompConnect, 0));
  }

  @Override
  public ProtocolImpl connect(EndPoint endPoint, String sessionId, String username, String password) throws IOException {
    StompProtocol protocol = new StompProtocol(endPoint);
    protocol.connect(sessionId, username, password);
    return protocol;
  }

  public void create(EndPoint endPoint, Packet packet) throws IOException {
    new StompProtocol(endPoint, packet);
  }
}
