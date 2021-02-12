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

package org.maps.network;

public class EndPointURLFactory {

  private static final EndPointURLFactory instance = new EndPointURLFactory();

  public static EndPointURLFactory getInstance(){
    return instance;
  }

  EndPointURLFactory(){}

  public EndPointURL create(String urlString){
    EndPointURL endPoint;
    if(urlString.toLowerCase().startsWith("lora")){
      endPoint = new LoRaEndPointURL(urlString);
    }
    else if(urlString.toLowerCase().startsWith("serial")){
      endPoint = new SerialEndPointURL(urlString);
    }
    else{
      endPoint = new EndPointURL(urlString);
    }
    return endPoint;
  }
}

