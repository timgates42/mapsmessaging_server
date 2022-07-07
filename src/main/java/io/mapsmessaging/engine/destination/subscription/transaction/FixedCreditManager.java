/*
 *    Copyright [ 2020 - 2022 ] [Matthew Buckton]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *
 */

package io.mapsmessaging.engine.destination.subscription.transaction;

public class FixedCreditManager extends CreditManager {

  public FixedCreditManager(int initialCredit) {
    super(initialCredit);
    if (initialCredit < 1) {
      // We can not have fixed credit less than 1 since that would make the subscription never receive any messages
      currentCredit = 32;
    }
  }

  public void increment() {
    // This is a fixed credit based manager, so this doesn't change
  }

  public void decrement() {
    // This is a fixed credit based manager, so this doesn't change
  }

}
