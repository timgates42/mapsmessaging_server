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

package org.maps.utilities.threads.tasks;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This is a simple Map of type String, Object, that is used to store thread local key|values
 *
 *  @since 1.0
 *  @author Matthew Buckton
 *  @version 1.0
 */
public class ThreadStateContext {

  private final Map<String, Object> map;

  public ThreadStateContext(){
    map = new LinkedHashMap<>();
  }

  public void add(@NotNull String key,@NotNull Object val){
    map.put(key, val);
  }

  public @Nullable Object get(@NotNull String key){
    return map.get(key);
  }

  public @Nullable Object remove(@NotNull String key){
    return map.remove(key);
  }

  public void clear(){
    map.clear();
  }

}
