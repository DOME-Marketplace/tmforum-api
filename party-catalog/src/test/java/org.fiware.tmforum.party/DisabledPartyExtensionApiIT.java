package org.fiware.tmforum.party;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.party.api.ext.IndividualExtensionApiTestClient;
import org.fiware.party.api.ext.OrganizationExtensionApiTestClient;
import org.fiware.party.model.IndividualCreateVOTestExample;
import org.fiware.party.model.IndividualVO;
import org.fiware.party.model.OrganizationCreateVOTestExample;
import org.fiware.party.model.OrganizationVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.party.domain.individual.Individual;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(packages = {"org.fiware.tmforum.party"})
public class DisabledPartyExtensionApiIT extends AbstractApiIT {

    private final IndividualExtensionApiTestClient individualExtensionApiTestClient;
    private final OrganizationExtensionApiTestClient organizationExtensionApiTestClient;

    protected DisabledPartyExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                          GeneralProperties generalProperties,
                                          IndividualExtensionApiTestClient individualExtensionApiTestClient,
                                          OrganizationExtensionApiTestClient organizationExtensionApiTestClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.individualExtensionApiTestClient = individualExtensionApiTestClient;
        this.organizationExtensionApiTestClient = organizationExtensionApiTestClient;
    }

    @Override
    protected String getEntityType() {
        return Individual.TYPE_INDIVIDUAL;
    }

    @Test
    public void createIndividualWithId405() throws Exception {
        HttpResponse<IndividualVO> response = callAndCatch(
                () -> individualExtensionApiTestClient.createIndividualWithId(null,
                        "urn:ngsi-ld:individual:test",
                        IndividualCreateVOTestExample.build().atSchemaLocation(null)));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }

    @Test
    public void createOrganizationWithId405() throws Exception {
        HttpResponse<OrganizationVO> response = callAndCatch(
                () -> organizationExtensionApiTestClient.createOrganizationWithId(null,
                        "urn:ngsi-ld:organization:test",
                        OrganizationCreateVOTestExample.build().atSchemaLocation(null)
                                .organizationParentRelationship(null)));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }
}
