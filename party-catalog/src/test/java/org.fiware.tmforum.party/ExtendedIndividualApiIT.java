package org.fiware.tmforum.party;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.party.api.ext.IndividualExtensionApiTestClient;
import org.fiware.party.api.ext.IndividualExtensionApiTestSpec;
import org.fiware.party.model.IndividualCreateVO;
import org.fiware.party.model.IndividualCreateVOTestExample;
import org.fiware.party.model.IndividualVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.party.domain.individual.Individual;
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
public class ExtendedIndividualApiIT extends AbstractApiIT implements IndividualExtensionApiTestSpec {

    private final IndividualExtensionApiTestClient testClient;

    public ExtendedIndividualApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                   GeneralProperties generalProperties,
                                   IndividualExtensionApiTestClient testClient) {
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
        return Individual.TYPE_INDIVIDUAL;
    }

    private static IndividualCreateVO buildCreateVO() {
        return IndividualCreateVOTestExample.build().atSchemaLocation(null);
    }

    @Test
    @Override
    public void createIndividualWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), Individual.TYPE_INDIVIDUAL).toString();

        HttpResponse<IndividualVO> response = callAndCatch(
                () -> testClient.createIndividualWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "Individual should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createIndividualWithId400() throws Exception {
        HttpResponse<IndividualVO> response = callAndCatch(
                () -> testClient.createIndividualWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createIndividualWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createIndividualWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createIndividualWithId405() throws Exception {
    }

    @Test
    @Override
    public void createIndividualWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), Individual.TYPE_INDIVIDUAL).toString();

        HttpResponse<IndividualVO> firstResponse = callAndCatch(
                () -> testClient.createIndividualWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<IndividualVO> secondResponse = callAndCatch(
                () -> testClient.createIndividualWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createIndividualWithId500() throws Exception {
    }
}
