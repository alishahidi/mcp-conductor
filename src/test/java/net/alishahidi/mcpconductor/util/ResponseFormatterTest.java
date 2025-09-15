package net.alishahidi.mcpconductor.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import net.alishahidi.mcpconductor.model.CommandResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class ResponseFormatterTest {

    private ResponseFormatter responseFormatter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        responseFormatter = new ResponseFormatter(objectMapper);
    }

    @Test
    void testFormatSuccess() throws Exception {
        String result = responseFormatter.formatSuccess("Operation completed successfully");
        
        JsonNode jsonNode = objectMapper.readTree(result);
        assertThat(jsonNode.get("success").asBoolean()).isTrue();
        assertThat(jsonNode.get("message").asText()).isEqualTo("Operation completed successfully");
        assertThat(jsonNode.has("timestamp")).isTrue();
    }

    @Test
    void testFormatError() throws Exception {
        String result = responseFormatter.formatError("Something went wrong");
        
        JsonNode jsonNode = objectMapper.readTree(result);
        assertThat(jsonNode.get("success").asBoolean()).isFalse();
        assertThat(jsonNode.get("error").asText()).isEqualTo("Something went wrong");
        assertThat(jsonNode.has("timestamp")).isTrue();
    }

    @Test
    void testFormatCommandResult() throws Exception {
        CommandResult commandResult = CommandResult.success("Command output");
        String result = responseFormatter.formatCommandResult(commandResult);
        
        JsonNode jsonNode = objectMapper.readTree(result);
        assertThat(jsonNode.get("success").asBoolean()).isTrue();
        assertThat(jsonNode.get("output").asText()).isEqualTo("Command output");
    }

    @Test
    void testFormatList() throws Exception {
        List<String> items = List.of("item1", "item2", "item3");
        String result = responseFormatter.formatList(items);
        
        JsonNode jsonNode = objectMapper.readTree(result);
        assertThat(jsonNode.get("success").asBoolean()).isTrue();
        assertThat(jsonNode.get("count").asInt()).isEqualTo(3);
        assertThat(jsonNode.get("items").isArray()).isTrue();
    }

    @Test
    void testFormatMap() throws Exception {
        Map<String, Object> data = Map.of(
            "key1", "value1",
            "key2", 42,
            "key3", true
        );
        String result = responseFormatter.formatMap(data);
        
        JsonNode jsonNode = objectMapper.readTree(result);
        assertThat(jsonNode.get("success").asBoolean()).isTrue();
        assertThat(jsonNode.get("data").has("key1")).isTrue();
        assertThat(jsonNode.get("data").get("key1").asText()).isEqualTo("value1");
    }

    @Test
    void testFormatProgress() throws Exception {
        String result = responseFormatter.formatProgress("Copying files", 5, 10);
        
        JsonNode jsonNode = objectMapper.readTree(result);
        assertThat(jsonNode.get("success").asBoolean()).isTrue();
        assertThat(jsonNode.get("operation").asText()).isEqualTo("Copying files");
        assertThat(jsonNode.get("completed").asInt()).isEqualTo(5);
        assertThat(jsonNode.get("total").asInt()).isEqualTo(10);
        assertThat(jsonNode.get("percentage").asDouble()).isEqualTo(50.0);
    }
}