package io.mapsmessaging.engine.schema;

import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.NativeSchemaConfig;
import io.mapsmessaging.schemas.config.impl.NativeSchemaConfig.TYPE;
import io.mapsmessaging.schemas.config.impl.RawSchemaConfig;
import io.mapsmessaging.schemas.formatters.MessageFormatter;
import io.mapsmessaging.schemas.formatters.MessageFormatterFactory;
import io.mapsmessaging.schemas.repository.SchemaRepository;
import io.mapsmessaging.schemas.repository.impl.SimpleSchemaRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SchemaManager implements SchemaRepository {

  public static final String DEFAULT_RAW_UUID = UUID.fromString("10000000-0000-1000-a000-100000000000").toString();
  public static final String DEFAULT_NUMERIC_STRING_SCHEMA = UUID.fromString("10000000-0000-1000-a000-100000000001").toString();
  public static final String DEFAULT_STRING_SCHEMA = UUID.fromString("10000000-0000-1000-a000-100000000002").toString();

  private static final SchemaManager instance;

  public static SchemaManager getInstance() {
    return instance;
  }

  static {
    instance = new SchemaManager();
  }

  private final SchemaRepository repository;
  private final Map<String, MessageFormatter> loadedFormatter;

  @Override
  public synchronized SchemaConfig addSchema(String s, SchemaConfig schemaConfig) {
    try {
      MessageFormatter messageFormatter = MessageFormatterFactory.getInstance().getFormatter(schemaConfig);
      loadedFormatter.put(schemaConfig.getUniqueId(), messageFormatter);
    } catch (Exception e) {
      // Unable to load the formatter
    }
    return repository.addSchema(s, schemaConfig);
  }

  public MessageFormatter getMessageFormatter(String uniqueId) {
    return loadedFormatter.get(uniqueId);

  }

  @Override
  public synchronized SchemaConfig getSchema(String uniqueId) {
    return repository.getSchema(uniqueId);
  }

  @Override
  public synchronized List<SchemaConfig> getSchemaByContext(String s) {
    return repository.getSchemaByContext(s);
  }

  @Override
  public synchronized List<SchemaConfig> getSchemas(String s) {
    return repository.getSchemas(s);
  }

  @Override
  public synchronized List<SchemaConfig> getAll() {
    return repository.getAll();
  }

  @Override
  public synchronized void removeSchema(String uniqueId) {
    repository.removeSchema(uniqueId);
    loadedFormatter.remove(uniqueId);
  }

  @Override
  public synchronized void removeAllSchemas() {
    loadedFormatter.clear();
    repository.removeAllSchemas();
  }

  public void start() {
    SchemaConfig rawConfig = new RawSchemaConfig();
    rawConfig.setUniqueId(DEFAULT_RAW_UUID);
    addSchema("", rawConfig);

    NativeSchemaConfig nativeSchemaConfig = new NativeSchemaConfig();
    nativeSchemaConfig.setUniqueId(DEFAULT_NUMERIC_STRING_SCHEMA);
    nativeSchemaConfig.setType(TYPE.NUMERIC_STRING);
    addSchema("$SYS", nativeSchemaConfig);

    nativeSchemaConfig = new NativeSchemaConfig();
    nativeSchemaConfig.setUniqueId(DEFAULT_STRING_SCHEMA);
    nativeSchemaConfig.setType(TYPE.STRING);
    addSchema("$SYS", nativeSchemaConfig);

    MessageFormatterFactory.getInstance();
  }

  private SchemaManager() {
    repository = new SimpleSchemaRepository();
    loadedFormatter = new LinkedHashMap<>();
  }

}
