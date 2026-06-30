package org.fiware.tmforum.partyrole;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.partyRole.api.ext.PartyRoleExtensionApiTestClient;
import org.fiware.partyRole.model.PartyRoleCreateVOTestExample;
import org.fiware.partyRole.model.PartyRoleVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.partyrole.domain.PartyRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(packages = {"org.fiware.tmforum.partyRole"})
public class DisabledPartyRoleExtensionApiIT extends AbstractApiIT {

    private final PartyRoleExtensionApiTestClient partyRoleExtensionApiTestClient;

    protected DisabledPartyRoleExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                              GeneralProperties generalProperties,
                                              PartyRoleExtensionApiTestClient partyRoleExtensionApiTestClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.partyRoleExtensionApiTestClient = partyRoleExtensionApiTestClient;
    }

    @Override
    protected String getEntityType() {
        return PartyRole.TYPE_PR;
    }

    @Test
    public void createPartyRoleWithId405() throws Exception {
        HttpResponse<PartyRoleVO> response = callAndCatch(
                () -> partyRoleExtensionApiTestClient.createPartyRoleWithId(null,
                        "urn:ngsi-ld:party-role:test",
                        PartyRoleCreateVOTestExample.build().atSchemaLocation(null).engagedParty(null)));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }
}
