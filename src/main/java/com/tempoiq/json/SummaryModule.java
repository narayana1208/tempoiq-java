package com.tempoiq.json;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

import com.tempoiq.Sensor;
import com.tempoiq.Summary;


public class SummaryModule extends SimpleModule {

  public SummaryModule() {
    addDeserializer(Summary.class, new SummaryDeserializer());
  }

  private static class SummaryDeserializer extends StdScalarDeserializer<Summary> {
    public SummaryDeserializer() { super(Summary.class); }

    @Override
    public Summary deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
      JsonNode root = parser.readValueAsTree();
      JsonNode tzNode = root.get("tz");
      JsonNode dataNode = root.get("summary");
      JsonNode startNode = root.get("start");
      JsonNode endNode = root.get("end");
      JsonNode sensorNode = root.get("sensor");

      if(tzNode == null) {
        throw context.mappingException("Missing 'tz' field in Summary.");
      }
      if(dataNode == null) {
        throw context.mappingException("Missing 'summary' field in Summary.");
      }
      if(startNode == null) {
        throw context.mappingException("Missing 'start' field in Summary.");
      }
      if(endNode == null) {
        throw context.mappingException("Missing 'end' field in Summary.");
      }
      if(sensorNode == null) {
        throw context.mappingException("Missing 'sensor' field in Summary.");
      }

      DateTimeZone timezone = Json.getObjectMapper()
                                  .reader()
                                  .withType(DateTimeZone.class)
                                  .readValue(tzNode);

      DateTime start = new DateTime(startNode.textValue(), timezone);
      DateTime end = new DateTime(endNode.textValue(), timezone);
      Map<String, Number> data = Json.getObjectMapper()
                                     .reader()
                                     .withType(new TypeReference<Map<String, Number>>() {})
                                     .readValue(dataNode);
      Sensor sensor = Json.getObjectMapper()
                          .reader()
                          .withType(Sensor.class)
                          .readValue(sensorNode);

      Summary summary = new Summary(sensor, new Interval(start, end), data);
      return summary;
    }
  }

  @Override
  public String getModuleName() {
    return "summary";
  }

  @Override
  public Version version() {
    return Version.unknownVersion();
  }
}

