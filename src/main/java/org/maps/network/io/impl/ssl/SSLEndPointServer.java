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

package org.maps.network.io.impl.ssl;

import java.io.IOException;
import java.net.InetSocketAddress;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import org.maps.logging.LogMessages;
import org.maps.logging.Logger;
import org.maps.logging.LoggerFactory;
import org.maps.network.EndPointURL;
import org.maps.network.NetworkConfig;
import org.maps.network.admin.EndPointManagerJMX;
import org.maps.network.io.AcceptHandler;
import org.maps.network.io.Selectable;
import org.maps.network.io.impl.Selector;
import org.maps.network.io.impl.SelectorLoadManager;
import org.maps.network.io.impl.tcp.TCPEndPointServer;

public class SSLEndPointServer extends TCPEndPointServer {

  private final SSLContext sslContext;
  private final boolean requiresClientAuth;

  public SSLEndPointServer(
      InetSocketAddress bindAddr,
      SelectorLoadManager sel,
      AcceptHandler accept,
      NetworkConfig config,
      EndPointURL url,
      EndPointManagerJMX managerMBean)
      throws IOException {
    super(bindAddr, sel, accept, config, url, managerMBean);
    logger.log(LogMessages.SSL_SERVER_START);
    requiresClientAuth = Boolean.parseBoolean(config.getProperties().getProperty("ssl_clientCertificateRequired", "false"));

    try{
      sslContext = SSLHelper.getInstance().createContext(config.getProperties(), logger);
    } finally {
      logger.log(LogMessages.SSL_SERVER_COMPLETED);
    }
  }

  @Override
  public void selected(Selectable selectable, Selector sel, int selection) {
    try {
      SSLEngine sslEngine = sslContext.createSSLEngine();
      sslEngine.setNeedClientAuth(requiresClientAuth);

      SSLEndPoint sslEndPoint =
          new SSLEndPoint(
              generateID(),
              sslEngine,
              serverSocket.accept().socket(),
              selector,
              authenticationConfig,
              this,
              managerMBean);
      handleNewEndPoint(sslEndPoint);
    } catch (IOException e) {
      logger.log(LogMessages.SSL_SERVER_ACCEPT_FAILED);
    }
  }

  @Override
  protected Logger createLogger(String url) {
    return LoggerFactory.getLogger(SSLEndPointServer.class.getName() + "_" + url);
  }
}
