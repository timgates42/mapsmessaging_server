/*
 * Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.mapsmessaging.consul;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.NotRegisteredException;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;
import com.orbitz.consul.monitoring.ClientEventCallback;
import io.mapsmessaging.BuildInfo;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;
import io.mapsmessaging.logging.ServerLogMessages;
import io.mapsmessaging.network.EndPointURL;
import io.mapsmessaging.network.io.EndPointServer;
import io.mapsmessaging.rest.RestApiServerManager;
import io.mapsmessaging.utilities.scheduler.SimpleTaskScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ConsulManager implements Runnable, ClientEventCallback {

  private final Logger logger = LoggerFactory.getLogger(ConsulManager.class);
  private final Consul client;
  private final AgentClient agentClient;
  private final List<String> serviceIds;
  private final String uniqueName;
  private Future<?> scheduledTask;

  public ConsulManager(String serverId) {
    String consulUrl = System.getProperty("ConsulUrl");
    Consul.Builder builder = Consul.builder();
    String consulToken = System.getProperty("ConsulToken");
    if (consulToken!=null) builder.withTokenAuth(consulToken);
    if(consulUrl != null){
      builder.withUrl(consulUrl);
    }
    else{
      String host = System.getProperty("ConsulHost", "127.0.0.1");
      String acl = System.getProperty("ConsulAcl");
      int port = Integer.parseInt(System.getProperty("ConsulPort", "8500"));
      HostAndPort hostAndPort = HostAndPort.fromParts(host, port);
        builder.withClientEventCallback(this)
          .withHostAndPort(hostAndPort);
      if(acl != null){
        builder.withAclToken(acl);
      }
    }
    client = builder.build();
    agentClient = client.agentClient();
    serviceIds = new ArrayList<>();
    uniqueName = serverId;
    serviceIds.add(serverId);
    logger.log(ServerLogMessages.CONSUL_STARTUP);
  }

  public KeyValueClient getKeyValueManager() {
    return client.keyValueClient();
  }

  public String scanForDefaultConfig(String namespace){
    if(!namespace.endsWith("/")){
      namespace = namespace+"/";
    }
    KeyValueClient keyValueClient = getKeyValueManager();
    while(namespace.contains("/") ){ // we have a depth
      String lookup = namespace+"default";
      List<String> keys = keyValueClient.getKeys(lookup);
      if(keys != null && !keys.isEmpty()){
        return lookup;
      }
      namespace = namespace.substring(0, namespace.length()-1); // Remove the "/"
      int idx = namespace.lastIndexOf("/");
      if(idx >=0 && namespace.length() > 1){
        namespace = namespace.substring(0, idx+1); // Include the /
      }
      else{
        break;
      }
    }
    return "";
  }

  public void register(Map<String, String> meta) {
    List<String> propertyNames = new ArrayList<>();
    meta.put("version", BuildInfo.getBuildVersion());
    meta.put("build-Date", BuildInfo.getBuildDate());
    logger.log(ServerLogMessages.CONSUL_REGISTER);

    Registration service = ImmutableRegistration.builder()
        .id(uniqueName)
        .name(Constants.NAME)
        .port(Constants.CONSUL_PORT)
        .check(Registration.RegCheck.ttl(Constants.PING_TIME))
        .tags(propertyNames)
        .meta(meta)
        .build();

    agentClient.register(service);
    scheduledTask = SimpleTaskScheduler.getInstance().scheduleAtFixedRate(this, Constants.HEALTH_TIME, Constants.HEALTH_TIME, TimeUnit.SECONDS);
  }

  public void register(EndPointServer endPointServer){
    EndPointURL endPointURL = new EndPointURL(endPointServer.getConfig().getUrl());

    String host = endPointURL.getHost();
    if(host.equals("0.0.0.0")){
      return; // Not Yet Supported
    }
    int port = endPointURL.getPort();
    String protocol = endPointServer.getConfig().getProtocols();
    String id = uniqueName+"-"+endPointServer.getConfig().getProperties().getProperty("name");
    Registration service = ImmutableRegistration.builder()
        .id(id)
        .name(Constants.NAME+"-"+protocol)
        .port(port)
        .check(Registration.RegCheck.tcp(host+":"+port, Constants.PING_TIME, Constants.PING_TIME/2))
        .build();
    agentClient.register(service);
    serviceIds.add(id);
  }

  public void register(RestApiServerManager restApiServerManager){
    String host = restApiServerManager.getHost();
    if(host.equals("0.0.0.0")){
      return; // Not Yet Supported
    }
    int port = restApiServerManager.getPort();
    String protocol = "http";
    if(restApiServerManager.isSecure()) protocol = "https";
    String url = protocol+"://"+host+":"+port+"/api/v1/ping";
    Registration service = ImmutableRegistration.builder()
        .id(uniqueName+"-RestApi")
        .name(Constants.NAME+"-RestApi")
        .port(restApiServerManager.getPort())
        .check(Registration.RegCheck.http(url, Constants.PING_TIME))
        .build();
    agentClient.register(service);
    serviceIds.add(uniqueName+"-RestApi");
  }

  public void stop() {
    for(String id:serviceIds){
      agentClient.deregister(id);
    }
    if (scheduledTask != null) {
      logger.log(ServerLogMessages.CONSUL_SHUTDOWN);
      scheduledTask.cancel(false);
    }
  }

  public void run() {
    agentClient.ping();
    try {
      agentClient.pass(uniqueName);
    } catch (NotRegisteredException e) {
      logger.log(ServerLogMessages.CONSUL_PING_EXCEPTION, e);
    }
  }
}
