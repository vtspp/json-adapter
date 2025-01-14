package com.br.vtspp.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public final class JsonAdapter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Map<Object, Object> readValues;

    private JsonAdapter() {}

    private JsonAdapter(final Map<Object, Object> readValues) {
        this.readValues = readValues;
    }

    public static String adapterSingleValue(String json, final String target, final Function<Object, Object> function) {
        try {
            var map = MAPPER.readValue(json, Map.class);

            if (map.containsKey(target)) {
                final var valueByFunction = function.apply(map.get(target));
                map.replace(target, valueByFunction);
                return MAPPER.writeValueAsString(map);
            }

            return json;
        } catch (IOException e) {
            log.warn("Ocorreu um erro ao tentar converter o JSON {}. Nenhuma informação será alterada e será usado o mesmo JSON de origem.", json, e);
            return json;
        }
    }

    public static JsonAdapter createJsonAdapterBuild(String json) throws IOException {
        final var readValues = MAPPER.readValue(json, Map.class);
        return new JsonAdapter(readValues);
    }

    public JsonAdapter adapterByFunction(final String target, final Function<Object, Object> function) {
        if (this.readValues.containsKey(target)) {
            final var valueByFunction = function.apply(this.readValues.get(target));
            this.readValues.replace(target, valueByFunction);
        }
        return this;
    }

    public String buildToJson() throws JsonProcessingException {
        return MAPPER.writeValueAsString(this.readValues);
    }



    public static Function<Object, Object> functionMaxSizeAccepted(int maxSize) {
        final var initPosition = 0;
        final var endPosition = Math.max(maxSize, 1);
        return o -> o.toString().substring(initPosition, endPosition);
    }
}
