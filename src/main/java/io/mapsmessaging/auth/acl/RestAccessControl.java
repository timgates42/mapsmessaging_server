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

package io.mapsmessaging.auth.acl;

import io.mapsmessaging.security.access.AccessControlMapping;

public enum RestAccessControl implements AccessControlMapping {
  READ_ONLY,
  WRITE;

  private final long value;

  RestAccessControl() {
    this.value = 1L << this.ordinal(); // Shift 1 left by 'ordinal' positions
  }

  @Override
  public Long getAccessValue(String accessControl) {
    if (accessControl == null) {
      return 0L;
    }
    try {
      return valueOf(accessControl.toUpperCase()).value;
    } catch (IllegalArgumentException e) {
      return 0L;
    }
  }

  @Override
  public String getAccessName(long value) {
    for (RestAccessControl ac : values()) {
      if (ac.value == value) {
        return ac.name().toLowerCase();
      }
    }
    return null;
  }
}

