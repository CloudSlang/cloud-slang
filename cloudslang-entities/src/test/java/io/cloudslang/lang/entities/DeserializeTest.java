package io.cloudslang.lang.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.cloudslang.lang.entities.bindings.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class DeserializeTest {

    private ObjectMapper mapper;
    private Gson gson;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper().enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Input.class, new GsonStringInputDeserializer());
        gson = gsonBuilder.create();
    }

    private <T> void testToAndFromJson(Object objToTest, Class<T> type) throws IOException {
        //jackson
        String objAsString = mapper.writeValueAsString(objToTest);
        T objAfterDeserialize = mapper.readValue(objAsString, type);
        assertEquals(objToTest, objAfterDeserialize);

        //gson
        String json = gson.toJson(objToTest);
        T fromJson = gson.fromJson(json, type);
        assertEquals(objToTest, fromJson);
    }

    @Test
    public void testDeserializeInput() throws IOException {
        Input input = new Input(
                "new_input",
                "some_expression",
                true,
                true,
                true,
                "system_property_ok_a_kind");
        testToAndFromJson(input, Input.class);
    }

    @Ignore // TODO: adapter
    @Test
    public void testDeserializeArgument() throws IOException {
        Argument argument = new Argument(
                "new_argument",
                "some_expression"
        );
        testToAndFromJson(argument, Argument.class);
    }

    @Ignore // TODO: adapter
    @Test
    public void testDeserializeOutput() throws IOException {
        Output output = new Output(
                "new_output",
                "some_expression");
        testToAndFromJson(output, Output.class);
    }

    @Ignore // TODO: adapter
    @Test
    public void testDeserializeResult() throws IOException {
        Result result = new Result(
                "new_result",
                "some_expression");
        testToAndFromJson(result, Result.class);
    }

    @Test
    public void testDeserializeResultNavigation() throws IOException {
        ResultNavigation resultNavigation = new ResultNavigation(
                1L,
                "a preset result");
        testToAndFromJson(resultNavigation, ResultNavigation.class);
    }

    @Test
    public void testDeserializeListForLoopStatement() throws IOException {
        LoopStatement listForLoopStatement = new ListForLoopStatement("varName", "expression");
        testToAndFromJson(listForLoopStatement, ListForLoopStatement.class);
    }

    @Test
    public void testDeserializeMapForLoopStatement() throws IOException {
        MapForLoopStatement mapForLoopStatement = new MapForLoopStatement("keyName", "valueName", "expression");
        testToAndFromJson(mapForLoopStatement, MapForLoopStatement.class);
    }

    @Test
    public void testDeserializeAsyncLoopStatement() throws IOException {
        AsyncLoopStatement asyncLoopStatement = new AsyncLoopStatement("varName", "expression");
        testToAndFromJson(asyncLoopStatement, AsyncLoopStatement.class);
    }

    private class GsonStringInputDeserializer extends TypeAdapter<Input> {

        @Override
        public void write(JsonWriter out, Input input) throws IOException {
            out.beginObject();
            out.name("name").value(input.getName());
            out.name("expression").value(String.valueOf(input.getExpression()));
            out.name("encrypted").value(input.isEncrypted());
            out.name("required").value(input.isRequired());
            out.name("overridable").value(input.isOverridable());
            out.name("systemPropertyName").value(input.getSystemPropertyName());
            out.endObject();
        }

        @Override
        public Input read(JsonReader in) throws IOException {
            String name = null;
            String expression = null;
            boolean encrypted = false;
            boolean required = false;
            boolean overridable = false;
            String systemPropertyName = null;

            in.beginObject();
            while (in.hasNext()) {
                String property = in.nextName();
                switch (property) {
                    case "name":
                        name = in.nextString();
                        break;
                    case "expression":
                        expression = in.nextString();
                        break;
                    case "required":
                        encrypted = in.nextBoolean();
                        break;
                    case "overridable":
                        required = in.nextBoolean();
                        break;
                    case "encrypted":
                        overridable = in.nextBoolean();
                        break;
                    case "systemPropertyName":
                        systemPropertyName = in.nextString();
                        break;
                    default:
                        throw new RuntimeException("Deserialization error: property '" + property + "' is not valid");
                }
            }
            in.endObject();
            return new Input(name, expression, encrypted, required, overridable, systemPropertyName);
        }
    }

}
