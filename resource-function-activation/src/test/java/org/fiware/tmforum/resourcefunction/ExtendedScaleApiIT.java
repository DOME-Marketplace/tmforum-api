package org.fiware.tmforum.resourcefunction;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.resourcefunction.api.ScaleApiTestClient;
import org.fiware.resourcefunction.api.ext.ScaleExtensionApiTestClient;
import org.fiware.resourcefunction.api.ext.ScaleExtensionApiTestSpec;
import org.fiware.resourcefunction.model.ScaleCreateVO;
import org.fiware.resourcefunction.model.ScaleCreateVOTestExample;
import org.fiware.resourcefunction.model.ScaleVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.resourcefunction.domain.Scale;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest(packages = {"org.fiware.tmforum.resourcefunction"})
@Property(name = "apiExtension.enabled", value = "true")
@Property(name = "apiExtension.putEnabled", value = "true")
@Property(name = "apiExtension.deleteEnabled", value = "true")
public class ExtendedScaleApiIT extends AbstractApiIT implements ScaleExtensionApiTestSpec {

    private final ScaleExtensionApiTestClient extensionTestClient;
    private final ScaleApiTestClient baseTestClient;

    public ExtendedScaleApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
            GeneralProperties generalProperties,
            ScaleExtensionApiTestClient extensionTestClient,
            ScaleApiTestClient baseTestClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.extensionTestClient = extensionTestClient;
        this.baseTestClient = baseTestClient;
    }

    @MockBean(TMForumEventHandler.class)
    public TMForumEventHandler eventHandler() {
        TMForumEventHandler eventHandler = mock(TMForumEventHandler.class);
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());
        when(eventHandler.handleUpdateEvent(any(), any())).thenReturn(Mono.empty());
        return eventHandler;
    }

    @Override
    protected String getEntityType() {
        return Scale.TYPE_SCALE;
    }

    private static ScaleCreateVO buildCreateVO() {
        return ScaleCreateVOTestExample.build().atSchemaLocation(null).resourceFunction(null);
    }

    @Test
    @Override
    public void createScaleWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), Scale.TYPE_SCALE).toString();

        HttpResponse<ScaleVO> response = callAndCatch(
                () -> extensionTestClient.createScaleWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "Scale should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createScaleWithId400() throws Exception {
        HttpResponse<ScaleVO> response = callAndCatch(
                () -> extensionTestClient.createScaleWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createScaleWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createScaleWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createScaleWithId405() throws Exception {
    }

    @Test
    @Override
    public void createScaleWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), Scale.TYPE_SCALE).toString();

        HttpResponse<ScaleVO> firstResponse = callAndCatch(
                () -> extensionTestClient.createScaleWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<ScaleVO> secondResponse = callAndCatch(
                () -> extensionTestClient.createScaleWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createScaleWithId500() throws Exception {
    }

    @Test
    @Override
    public void deleteScale204() throws Exception {
        HttpResponse<ScaleVO> createResponse = callAndCatch(
                () -> baseTestClient.createScale(null, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, createResponse.getStatus(), "Scale should have been created.");
        String id = createResponse.body().getId();

        assertEquals(HttpStatus.NO_CONTENT,
                callAndCatch(() -> extensionTestClient.deleteScale(null, id)).getStatus(),
                "Scale should have been deleted.");

        assertEquals(HttpStatus.NOT_FOUND,
                callAndCatch(() -> baseTestClient.retrieveScale(null, id, null)).getStatus(),
                "Scale should not exist anymore.");
    }

    @Disabled("400 is impossible to happen on deletion with the current implementation.")
    @Test
    @Override
    public void deleteScale400() throws Exception {
    }

    @Disabled("Security is handled externally, thus 401 and 403 cannot happen.")
    @Test
    @Override
    public void deleteScale401() throws Exception {
    }

    @Disabled("Security is handled externally, thus 401 and 403 cannot happen.")
    @Test
    @Override
    public void deleteScale403() throws Exception {
    }

    @Test
    @Override
    public void deleteScale404() throws Exception {
        HttpResponse<?> notFoundResponse = callAndCatch(
                () -> extensionTestClient.deleteScale(null, "urn:ngsi-ld:scale:no-such-entity"));
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatus(), "No such entity should exist.");
        Optional<ErrorDetails> optionalErrorDetails = notFoundResponse.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");

        notFoundResponse = callAndCatch(() -> extensionTestClient.deleteScale(null, "invalid-id"));
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatus(), "No such entity should exist.");
        optionalErrorDetails = notFoundResponse.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Override
    public void deleteScale500() throws Exception {
    }
}
