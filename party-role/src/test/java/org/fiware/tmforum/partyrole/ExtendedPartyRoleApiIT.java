package org.fiware.tmforum.partyrole;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.partyRole.api.ext.PartyRoleExtensionApiTestClient;
import org.fiware.partyRole.api.ext.PartyRoleExtensionApiTestSpec;
import org.fiware.partyRole.model.PartyRoleCreateVO;
import org.fiware.partyRole.model.PartyRoleCreateVOTestExample;
import org.fiware.partyRole.model.PartyRoleVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.partyrole.domain.PartyRole;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(packages = {"org.fiware.tmforum.partyRole"})
@Property(name = "apiExtension.enabled", value = "true")
@Property(name = "apiExtension.putEnabled", value = "true")
public class ExtendedPartyRoleApiIT extends AbstractApiIT implements PartyRoleExtensionApiTestSpec {

    private final PartyRoleExtensionApiTestClient testClient;

    public ExtendedPartyRoleApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                  GeneralProperties generalProperties,
                                  PartyRoleExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @Override
    protected String getEntityType() {
        return PartyRole.TYPE_PR;
    }

    private static PartyRoleCreateVO buildCreateVO() {
        return PartyRoleCreateVOTestExample.build().atSchemaLocation(null).engagedParty(null);
    }

    @Test
    @Override
    public void createPartyRoleWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), PartyRole.TYPE_PR).toString();

        HttpResponse<PartyRoleVO> response = callAndCatch(
                () -> testClient.createPartyRoleWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "PartyRole should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createPartyRoleWithId400() throws Exception {
        HttpResponse<PartyRoleVO> response = callAndCatch(
                () -> testClient.createPartyRoleWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createPartyRoleWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createPartyRoleWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createPartyRoleWithId405() throws Exception {
    }

    @Test
    @Override
    public void createPartyRoleWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), PartyRole.TYPE_PR).toString();

        HttpResponse<PartyRoleVO> firstResponse = callAndCatch(
                () -> testClient.createPartyRoleWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<PartyRoleVO> secondResponse = callAndCatch(
                () -> testClient.createPartyRoleWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createPartyRoleWithId500() throws Exception {
    }
}
