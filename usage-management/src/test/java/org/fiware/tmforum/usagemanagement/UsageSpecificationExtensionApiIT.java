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
import org.fiware.tmforum.usagemanagement.domain.UsageSpecification;
import org.fiware.usagemanagement.api.ext.UsageSpecificationExtensionApiTestClient;
import org.fiware.usagemanagement.api.ext.UsageSpecificationExtensionApiTestSpec;
import org.fiware.usagemanagement.model.UsageSpecificationCreateVO;
import org.fiware.usagemanagement.model.UsageSpecificationCreateVOTestExample;
import org.fiware.usagemanagement.model.UsageSpecificationVO;
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
public class UsageSpecificationExtensionApiIT extends AbstractApiIT implements UsageSpecificationExtensionApiTestSpec {

    private final UsageSpecificationExtensionApiTestClient testClient;

    @MockBean(TMForumEventHandler.class)
    public TMForumEventHandler eventHandler() {
        TMForumEventHandler eventHandler = mock(TMForumEventHandler.class);
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());
        when(eventHandler.handleUpdateEvent(any(), any())).thenReturn(Mono.empty());
        return eventHandler;
    }

    public UsageSpecificationExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
            GeneralProperties generalProperties,
            UsageSpecificationExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @Override
    protected String getEntityType() {
        return UsageSpecification.TYPE_USP;
    }

    private static UsageSpecificationCreateVO buildCreateVO() {
        return UsageSpecificationCreateVOTestExample.build().atSchemaLocation(null).validFor(null).targetEntitySchema(null);
    }

    @Test
    @Override
    public void createUsageSpecificationWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), UsageSpecification.TYPE_USP).toString();

        HttpResponse<UsageSpecificationVO> response = callAndCatch(
                () -> testClient.createUsageSpecificationWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "UsageSpecification should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createUsageSpecificationWithId400() throws Exception {
        HttpResponse<UsageSpecificationVO> response = callAndCatch(
                () -> testClient.createUsageSpecificationWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createUsageSpecificationWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createUsageSpecificationWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createUsageSpecificationWithId405() throws Exception {
    }

    @Test
    @Override
    public void createUsageSpecificationWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), UsageSpecification.TYPE_USP).toString();

        HttpResponse<UsageSpecificationVO> firstResponse = callAndCatch(
                () -> testClient.createUsageSpecificationWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<UsageSpecificationVO> secondResponse = callAndCatch(
                () -> testClient.createUsageSpecificationWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createUsageSpecificationWithId500() throws Exception {
    }
}
