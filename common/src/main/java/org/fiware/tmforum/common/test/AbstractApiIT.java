package org.fiware.tmforum.common.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.wistefan.mapping.AdditionalPropertyMixin;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.ngsi.model.AdditionalPropertyVO;
import org.fiware.ngsi.model.EntityListVO;
import org.fiware.ngsi.model.EntityVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.junit.jupiter.api.BeforeEach;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Common super class for the api tests
 */
public abstract class AbstractApiIT {

    private static final Set<String> DEFAULT_GOLDEN_IGNORED_FIELDS = Set.of("id", "href", "lastUpdate");
    private static final String GOLDEN_UPDATE_PROPERTY = "golden.update";
    private static final Path GOLDEN_DIR = Path.of("src/test/resources/golden");

    private final EntitiesApiClient entitiesApiClient;
    private final GeneralProperties generalProperties;
    protected final ObjectMapper objectMapper;

    /**
     * Needs to return the type of the handled entities, to allow automated cleanup before each test.
     *
     * @return the entity type to be cleaned up
     */
    protected abstract String getEntityType();

    protected AbstractApiIT(EntitiesApiClient entitiesApiClient,
                            ObjectMapper objectMapper, GeneralProperties generalProperties) {
        this.entitiesApiClient = entitiesApiClient;
        this.objectMapper = objectMapper;
        this.generalProperties = generalProperties;
    }

    @BeforeEach
    public void cleanUp() {
        this.objectMapper
                .addMixIn(AdditionalPropertyVO.class, AdditionalPropertyMixin.class);
        this.objectMapper.findAndRegisterModules();
        EntityListVO entityVOS = entitiesApiClient.queryEntities(null,
                null,
                null,
                getEntityType(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                1000,
                0,
                false,
                null,
                getLinkHeader(generalProperties.getContextUrl()))
                .map(HttpResponse::body)
                .block();
        entityVOS.stream()
                .filter(Objects::nonNull)
                .map(EntityVO::getId)
                .filter(Objects::nonNull)
                .forEach(eId -> entitiesApiClient.removeEntityById(eId, null, null).block());
    }

    // Helper method to catch potential http exceptions and return the status code.
    public <T> HttpResponse<T> callAndCatch(Callable<HttpResponse<T>> request) throws Exception {
        try {
            return request.call();
        } catch (HttpClientResponseException e) {
            return (HttpResponse<T>) e.getResponse();
        }
    }

    protected String getLinkHeader(URL contextUrl) {
        return String.format("<%s>; rel=\"http://www.w3.org/ns/json-ld#context\"; type=\"application/ld+json",
                contextUrl);
    }

    /**
     * Compares the given response body against a golden fixture (src/test/resources/golden/{goldenName}.json),
     * ignoring the fields in {@link #DEFAULT_GOLDEN_IGNORED_FIELDS}. Run with -Dgolden.update=true to (re)record
     * the fixture instead of asserting against it.
     */
    protected void assertMatchesGolden(String goldenName, Object actualBody) throws IOException, JSONException {
        assertMatchesGolden(goldenName, actualBody, DEFAULT_GOLDEN_IGNORED_FIELDS);
    }

    protected void assertMatchesGolden(String goldenName, Object actualBody, Set<String> ignoredFields)
            throws IOException, JSONException {
        JsonNode actualNode = stripIgnoredFields(objectMapper.valueToTree(actualBody), ignoredFields);
        String actualJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(actualNode);

        Path goldenPath = GOLDEN_DIR.resolve(goldenName + ".json");
        if (Boolean.getBoolean(GOLDEN_UPDATE_PROPERTY) || Files.notExists(goldenPath)) {
            Files.createDirectories(goldenPath.getParent());
            Files.writeString(goldenPath, actualJson);
            return;
        }
        JSONAssert.assertEquals(Files.readString(goldenPath), actualJson, JSONCompareMode.STRICT);
    }

    private JsonNode stripIgnoredFields(JsonNode node, Set<String> ignoredFields) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            ignoredFields.forEach(objectNode::remove);
            objectNode.fields().forEachRemaining(entry -> stripIgnoredFields(entry.getValue(), ignoredFields));
        } else if (node.isArray()) {
            node.forEach(child -> stripIgnoredFields(child, ignoredFields));
        }
        return node;
    }
}
