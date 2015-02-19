package org.openscore.lang.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.Test;
import org.openscore.lang.entities.bindings.Input;
import org.openscore.lang.entities.bindings.Output;
import org.openscore.lang.entities.bindings.Result;

import java.io.IOException;

import static org.junit.Assert.*;

public class DeserializeTest {

    private ObjectMapper mapper = new ObjectMapper();
    Gson gson = new Gson();


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

    @Test
    public void testDeserializeOutput() throws IOException {
        Output output = new Output(
                "new_output",
                "some_expression");
        testToAndFromJson(output, Output.class);
    }

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

}