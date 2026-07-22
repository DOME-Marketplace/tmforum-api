package org.fiware.tmforum.resourcecatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.resourcecatalog.api.ext.ResourceCandidateExtensionApiTestClient;
import org.fiware.resourcecatalog.api.ext.ResourceCandidateExtensionApiTestSpec;
import org.fiware.resourcecatalog.model.ResourceCandidateCreateVO;
import org.fiware.resourcecatalog.model.ResourceCandidateCreateVOTestExample;
import org.fiware.resourcecatalog.model.ResourceCandidateVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.resource.ResourceCandidate;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest(packages = {"org.fiware.tmforum.resourcecatalog"})
@Property(name = "apiExtension.enabled", value = "true")
@Property(name = "apiExtension.putEnabled", value = "true")
public class ExtendedResourceCandidateApiIT extends AbstractApiIT implements ResourceCandidateExtensionApiTestSpec {

    private final ResourceCandidateExtensionApiTestClient testClient;

    private Clock clock = mock(Clock.class);

    @MockBean(Clock.class)
    public Clock clock() {
        return clock;
    }

    @MockBean(TMForumEventHandler.class)
    public TMForumEventHandler eventHandler() {
        TMForumEventHandler eventHandler = mock(TMForumEventHandler.class);
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());
        when(eventHandler.handleUpdateEvent(any(), any())).thenReturn(Mono.empty());
        return eventHandler;
    }

    public ExtendedResourceCandidateApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                          GeneralProperties generalProperties,
                                          ResourceCandidateExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @Override
    protected String getEntityType() {
        return ResourceCandidate.TYPE_RESOURCE_CANDIDATE;
    }

    private static ResourceCandidateCreateVO buildCreateVO() {
        return ResourceCandidateCreateVOTestExample.build().atSchemaLocation(null).resourceSpecification(null);
    }

    @Test
    @Override
    public void createResourceCandidateWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), ResourceCandidate.TYPE_RESOURCE_CANDIDATE).toString();

        HttpResponse<ResourceCandidateVO> response = callAndCatch(
                () -> testClient.createResourceCandidateWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "ResourceCandidate should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createResourceCandidateWithId400() throws Exception {
        HttpResponse<ResourceCandidateVO> response = callAndCatch(
                () -> testClient.createResourceCandidateWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createResourceCandidateWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createResourceCandidateWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createResourceCandidateWithId405() throws Exception {
    }

    @Test
    @Override
    public void createResourceCandidateWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), ResourceCandidate.TYPE_RESOURCE_CANDIDATE).toString();

        HttpResponse<ResourceCandidateVO> firstResponse = callAndCatch(
                () -> testClient.createResourceCandidateWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<ResourceCandidateVO> secondResponse = callAndCatch(
                () -> testClient.createResourceCandidateWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createResourceCandidateWithId500() throws Exception {
    }
}
