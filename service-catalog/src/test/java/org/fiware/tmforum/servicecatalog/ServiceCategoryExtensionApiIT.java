package org.fiware.tmforum.servicecatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.servicecatalog.api.ext.ServiceCategoryExtensionApiTestClient;
import org.fiware.servicecatalog.api.ext.ServiceCategoryExtensionApiTestSpec;
import org.fiware.servicecatalog.model.ServiceCategoryCreateVO;
import org.fiware.servicecatalog.model.ServiceCategoryCreateVOTestExample;
import org.fiware.servicecatalog.model.ServiceCategoryVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.service.ServiceCategory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest(packages = {"org.fiware.tmforum.servicecatalog"})
@Property(name = "apiExtension.enabled", value = "true")
public class ServiceCategoryExtensionApiIT extends AbstractApiIT implements ServiceCategoryExtensionApiTestSpec {

    private final ServiceCategoryExtensionApiTestClient testClient;

    @MockBean(TMForumEventHandler.class)
    public TMForumEventHandler eventHandler() {
        TMForumEventHandler eventHandler = mock(TMForumEventHandler.class);
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());
        when(eventHandler.handleUpdateEvent(any(), any())).thenReturn(Mono.empty());
        return eventHandler;
    }

    @MockBean(Clock.class)
    public Clock clock() {
        Clock clock = mock(Clock.class);
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(10000));
        return clock;
    }

    public ServiceCategoryExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
            GeneralProperties generalProperties,
            ServiceCategoryExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @Override
    protected String getEntityType() {
        return ServiceCategory.TYPE_SERVICE_CATEGORY;
    }

    private static ServiceCategoryCreateVO buildCreateVO() {
        return ServiceCategoryCreateVOTestExample.build().atSchemaLocation(null).parentId(null);
    }

    @Test
    @Override
    public void createServiceCategoryWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), ServiceCategory.TYPE_SERVICE_CATEGORY).toString();

        HttpResponse<ServiceCategoryVO> response = callAndCatch(
                () -> testClient.createServiceCategoryWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "ServiceCategory should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createServiceCategoryWithId400() throws Exception {
        HttpResponse<ServiceCategoryVO> response = callAndCatch(
                () -> testClient.createServiceCategoryWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createServiceCategoryWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createServiceCategoryWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createServiceCategoryWithId405() throws Exception {
    }

    @Test
    @Override
    public void createServiceCategoryWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), ServiceCategory.TYPE_SERVICE_CATEGORY).toString();

        HttpResponse<ServiceCategoryVO> firstResponse = callAndCatch(
                () -> testClient.createServiceCategoryWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<ServiceCategoryVO> secondResponse = callAndCatch(
                () -> testClient.createServiceCategoryWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createServiceCategoryWithId500() throws Exception {
    }
}
