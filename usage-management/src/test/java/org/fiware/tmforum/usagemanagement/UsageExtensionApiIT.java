package org.fiware.tmforum.usagemanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.usagemanagement.domain.Usage;
import org.fiware.usagemanagement.api.ext.UsageExtensionApiTestClient;
import org.fiware.usagemanagement.api.ext.UsageExtensionApiTestSpec;
import org.fiware.usagemanagement.model.UsageCreateVO;
import org.fiware.usagemanagement.model.UsageCreateVOTestExample;
import org.fiware.usagemanagement.model.UsageVO;
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

@MicronautTest(packages = {"org.fiware.tmforum.usagemanagement"})
@Property(name = "apiExtension.enabled", value = "true")
@Property(name = "apiExtension.putEnabled", value = "true")
public class UsageExtensionApiIT extends AbstractApiIT implements UsageExtensionApiTestSpec {

    private final UsageExtensionApiTestClient testClient;

    @MockBean(TMForumEventHandler.class)
    public TMForumEventHandler eventHandler() {
        TMForumEventHandler eventHandler = mock(TMForumEventHandler.class);
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());
        when(eventHandler.handleUpdateEvent(any(), any())).thenReturn(Mono.empty());
        return eventHandler;
    }

    public UsageExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
            GeneralProperties generalProperties,
            UsageExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @Override
    protected String getEntityType() {
        return Usage.TYPE_U;
    }

    private static UsageCreateVO buildCreateVO() {
        return UsageCreateVOTestExample.build().atSchemaLocation(null).usageSpecification(null);
    }

    @Test
    @Override
    public void createUsageWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), Usage.TYPE_U).toString();

        HttpResponse<UsageVO> response = callAndCatch(
                () -> testClient.createUsageWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "Usage should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createUsageWithId400() throws Exception {
        HttpResponse<UsageVO> response = callAndCatch(
                () -> testClient.createUsageWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createUsageWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createUsageWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createUsageWithId405() throws Exception {
    }

    @Test
    @Override
    public void createUsageWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), Usage.TYPE_U).toString();

        HttpResponse<UsageVO> firstResponse = callAndCatch(
                () -> testClient.createUsageWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<UsageVO> secondResponse = callAndCatch(
                () -> testClient.createUsageWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createUsageWithId500() throws Exception {
    }
}
