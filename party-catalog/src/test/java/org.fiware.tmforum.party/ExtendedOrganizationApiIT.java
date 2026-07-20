package org.fiware.tmforum.party;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.party.api.ext.OrganizationExtensionApiTestClient;
import org.fiware.party.api.ext.OrganizationExtensionApiTestSpec;
import org.fiware.party.model.OrganizationCreateVO;
import org.fiware.party.model.OrganizationCreateVOTestExample;
import org.fiware.party.model.OrganizationVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.party.domain.organization.Organization;
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

@MicronautTest(packages = {"org.fiware.tmforum.party"})
@Property(name = "apiExtension.enabled", value = "true")
@Property(name = "apiExtension.putEnabled", value = "true")
public class ExtendedOrganizationApiIT extends AbstractApiIT implements OrganizationExtensionApiTestSpec {

    private final OrganizationExtensionApiTestClient testClient;

    public ExtendedOrganizationApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                     GeneralProperties generalProperties,
                                     OrganizationExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
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
        return Organization.TYPE_ORGANIZATION;
    }

    private static OrganizationCreateVO buildCreateVO() {
        return OrganizationCreateVOTestExample.build().atSchemaLocation(null)
                .organizationParentRelationship(null);
    }

    @Test
    @Override
    public void createOrganizationWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), Organization.TYPE_ORGANIZATION).toString();

        HttpResponse<OrganizationVO> response = callAndCatch(
                () -> testClient.createOrganizationWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "Organization should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createOrganizationWithId400() throws Exception {
        HttpResponse<OrganizationVO> response = callAndCatch(
                () -> testClient.createOrganizationWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createOrganizationWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createOrganizationWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createOrganizationWithId405() throws Exception {
    }

    @Test
    @Override
    public void createOrganizationWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), Organization.TYPE_ORGANIZATION).toString();

        HttpResponse<OrganizationVO> firstResponse = callAndCatch(
                () -> testClient.createOrganizationWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<OrganizationVO> secondResponse = callAndCatch(
                () -> testClient.createOrganizationWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createOrganizationWithId500() throws Exception {
    }
}
