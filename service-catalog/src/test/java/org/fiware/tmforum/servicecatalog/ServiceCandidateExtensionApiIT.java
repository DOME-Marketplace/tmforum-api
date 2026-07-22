package org.fiware.tmforum.servicecatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.servicecatalog.api.ext.ServiceCandidateExtensionApiTestClient;
import org.fiware.servicecatalog.api.ext.ServiceCandidateExtensionApiTestSpec;
import org.fiware.servicecatalog.model.ServiceCandidateCreateVO;
import org.fiware.servicecatalog.model.ServiceCandidateCreateVOTestExample;
import org.fiware.servicecatalog.model.ServiceCandidateVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.service.ServiceCandidate;
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
@Property(name = "apiExtension.putEnabled", value = "true")
public class ServiceCandidateExtensionApiIT extends AbstractApiIT implements ServiceCandidateExtensionApiTestSpec {

    private final ServiceCandidateExtensionApiTestClient testClient;

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

    public ServiceCandidateExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
            GeneralProperties generalProperties,
            ServiceCandidateExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @Override
    protected String getEntityType() {
        return ServiceCandidate.TYPE_SERVICE_CANDIDATE;
    }

    private static ServiceCandidateCreateVO buildCreateVO() {
        return ServiceCandidateCreateVOTestExample.build().atSchemaLocation(null).serviceSpecification(null);
    }

    @Test
    @Override
    public void createServiceCandidateWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), ServiceCandidate.TYPE_SERVICE_CANDIDATE).toString();

        HttpResponse<ServiceCandidateVO> response = callAndCatch(
                () -> testClient.createServiceCandidateWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "ServiceCandidate should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createServiceCandidateWithId400() throws Exception {
        HttpResponse<ServiceCandidateVO> response = callAndCatch(
                () -> testClient.createServiceCandidateWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createServiceCandidateWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createServiceCandidateWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createServiceCandidateWithId405() throws Exception {
    }

    @Test
    @Override
    public void createServiceCandidateWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), ServiceCandidate.TYPE_SERVICE_CANDIDATE).toString();

        HttpResponse<ServiceCandidateVO> firstResponse = callAndCatch(
                () -> testClient.createServiceCandidateWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<ServiceCandidateVO> secondResponse = callAndCatch(
                () -> testClient.createServiceCandidateWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createServiceCandidateWithId500() throws Exception {
    }
}
