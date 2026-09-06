package ai.anvaya.prajna.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JsonSchemaValidator {

    private final ObjectMapper objectMapper;
    private JsonSchema schema;

    public JsonSchemaValidator() {
        this.objectMapper = new ObjectMapper();
        loadSchema();
    }

    private void loadSchema() {
        try (InputStream is = getClass().getResourceAsStream("/schemas/explanation-ir.schema.json")) {
            if (is != null) {
                JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
                this.schema = factory.getSchema(is);
            }
        } catch (Exception ignored) {
        }
    }

    public ValidationResult validateJson(String json) {
        List<ValidationViolation> violations = new ArrayList<>();

        try {
            JsonNode node = objectMapper.readTree(json);
            if (schema != null) {
                Set<ValidationMessage> messages = schema.validate(node);
                for (ValidationMessage msg : messages) {
                    violations.add(ValidationViolation.builder()
                            .code("JSON_SCHEMA_VIOLATION")
                            .message(msg.getMessage())
                            .severity(ValidationSeverity.ERROR)
                            .build());
                }
            } else {
                // Fallback check required root fields
                if (!node.has("schemaVersion") || !node.has("explanationId") || !node.has("steps")) {
                    violations.add(ValidationViolation.builder()
                            .code("JSON_SCHEMA_VIOLATION")
                            .message("Missing required root IR fields (schemaVersion, explanationId, steps)")
                            .severity(ValidationSeverity.ERROR)
                            .build());
                }
            }
        } catch (Exception e) {
            violations.add(ValidationViolation.builder()
                    .code("INVALID_JSON")
                    .message("Failed to parse JSON: " + e.getMessage())
                    .severity(ValidationSeverity.ERROR)
                    .build());
        }

        ValidationStatus status = violations.isEmpty() ? ValidationStatus.PASSED : ValidationStatus.FAILED;
        return ValidationResult.builder()
                .status(status)
                .score(violations.isEmpty() ? 1.0 : 0.0)
                .violations(violations)
                .build();
    }
}
