package org.fiware.tmforum.resourceinventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.resourceinventory.api.ext.ResourceExtensionApiTestClient;
import org.fiware.resourceinventory.api.ext.ResourceExtensionApiTestSpec;
import org.fiware.resourceinventory.model.ResourceCreateVO;
import org.fiware.resourceinventory.model.ResourceCreateVOTestExample;
import org.fiware.resourceinventory.model.ResourceVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.resource.Resource;
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

@MicronautTest(packages = {"org.fiware.tmforum.resourceinventory"})
@Property(name = "apiExtension.enabled", value = "true")
public class ResourceExtensionApiIT extends AbstractApiIT implements ResourceExtensionApiTestSpec {

    private final ResourceExtensionApiTestClient testClient;

    @MockBean(TMForumEventHandler.class)
    public TMForumEventHandler eventHandler() {
        TMForumEventHandler eventHandler = mock(TMForumEventHandler.class);
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());
        when(eventHandler.handleUpdateEvent(any(), any())).thenReturn(Mono.empty());
        return eventHandler;
    }

    public ResourceExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
            GeneralProperties generalProperties,
            ResourceExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @Override
    protected String getEntityType() {
        return Resource.TYPE_RESOURCE;
    }

    private static ResourceCreateVO buildCreateVO() {
        return ResourceCreateVOTestExample.build().atSchemaLocation(null).place(null).resourceSpecification(null);
    }

    @Test
    @Override
    public void createResourceWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), Resource.TYPE_RESOURCE).toString();

        HttpResponse<ResourceVO> response = callAndCatch(
                () -> testClient.createResourceWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "Resource should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createResourceWithId400() throws Exception {
        HttpResponse<ResourceVO> response = callAndCatch(
                () -> testClient.createResourceWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createResourceWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createResourceWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createResourceWithId405() throws Exception {
    }

    @Test
    @Override
    public void createResourceWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), Resource.TYPE_RESOURCE).toString();

        HttpResponse<ResourceVO> firstResponse = callAndCatch(
                () -> testClient.createResourceWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<ResourceVO> secondResponse = callAndCatch(
                () -> testClient.createResourceWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createResourceWithId500() throws Exception {
    }
}
