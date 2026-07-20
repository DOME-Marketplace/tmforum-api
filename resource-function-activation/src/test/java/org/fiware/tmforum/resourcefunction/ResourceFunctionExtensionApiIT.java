package org.fiware.tmforum.resourcefunction;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.resourcefunction.api.ext.ResourceFunctionExtensionApiTestClient;
import org.fiware.resourcefunction.api.ext.ResourceFunctionExtensionApiTestSpec;
import org.fiware.resourcefunction.model.ResourceFunctionCreateVO;
import org.fiware.resourcefunction.model.ResourceFunctionCreateVOTestExample;
import org.fiware.resourcefunction.model.ResourceFunctionVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.resourcefunction.domain.ResourceFunction;
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
public class ResourceFunctionExtensionApiIT extends AbstractApiIT implements ResourceFunctionExtensionApiTestSpec {

    private final ResourceFunctionExtensionApiTestClient testClient;

    @MockBean(TMForumEventHandler.class)
    public TMForumEventHandler eventHandler() {
        TMForumEventHandler eventHandler = mock(TMForumEventHandler.class);
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());
        when(eventHandler.handleUpdateEvent(any(), any())).thenReturn(Mono.empty());
        return eventHandler;
    }

    public ResourceFunctionExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
            GeneralProperties generalProperties,
            ResourceFunctionExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @Override
    protected String getEntityType() {
        return ResourceFunction.TYPE_RESOURCE_FUNCTION;
    }

    private static ResourceFunctionCreateVO buildCreateVO() {
        return ResourceFunctionCreateVOTestExample.build().atSchemaLocation(null).place(null).resourceSpecification(null);
    }

    @Test
    @Override
    public void createResourceFunctionWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), ResourceFunction.TYPE_RESOURCE_FUNCTION).toString();

        HttpResponse<ResourceFunctionVO> response = callAndCatch(
                () -> testClient.createResourceFunctionWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "ResourceFunction should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createResourceFunctionWithId400() throws Exception {
        HttpResponse<ResourceFunctionVO> response = callAndCatch(
                () -> testClient.createResourceFunctionWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createResourceFunctionWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createResourceFunctionWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createResourceFunctionWithId405() throws Exception {
    }

    @Test
    @Override
    public void createResourceFunctionWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), ResourceFunction.TYPE_RESOURCE_FUNCTION).toString();

        HttpResponse<ResourceFunctionVO> firstResponse = callAndCatch(
                () -> testClient.createResourceFunctionWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<ResourceFunctionVO> secondResponse = callAndCatch(
                () -> testClient.createResourceFunctionWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createResourceFunctionWithId500() throws Exception {
    }
}
