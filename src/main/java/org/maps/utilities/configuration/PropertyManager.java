package org.maps.utilities.configuration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class PropertyManager {

  protected final ConfigurationProperties properties;

  public PropertyManager(){
    properties = new ConfigurationProperties();
  }

  protected abstract void load();

  protected abstract void store(String name);

  public abstract void copy(PropertyManager propertyManager);

  public @NotNull JSONObject getPropertiesJSON(@NotNull String name) {
    Object config = properties.get(name);
    if(config instanceof Map){
      Map<String, Object> root = (Map<String, Object>) config;
      root.get("JSON");
    }
    return new JSONObject();
  }

  public void loadPropertiesJSON(@NotNull String name, @NotNull JSONObject config) {
    properties.remove(name);

    JSONParser parser = new JSONParser(config);


    JSONArray array = config.getJSONArray(name);
    Map<String, Object> globalProperties = new LinkedHashMap<>();
    if(config.has("global")) {
      globalProperties = fromJSON(config.getJSONObject("global"));
    }
    for(int x=0;x<array.length();x++){
      Map<String, Object> configEntries = fromJSON(array.getJSONObject(x));
      properties.putAll(configEntries);
    }
    properties.setGlobal(new ConfigurationProperties(globalProperties));
  }

  private Map<String, Object> fromJSON(JSONObject object){
    Map<String, Object> response = new LinkedHashMap<>();
    for(String key:object.keySet()){
      response.put(key, object.getString(key));
    }
    return response;
  }

  public @NotNull ConfigurationProperties getProperties(String name) {
    Object obj = properties.get(name);
    if(obj instanceof ConfigurationProperties){
      return (ConfigurationProperties)obj;
    }
    return new ConfigurationProperties();
  }

}
