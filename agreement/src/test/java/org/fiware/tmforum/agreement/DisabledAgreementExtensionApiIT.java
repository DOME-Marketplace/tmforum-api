package org.fiware.tmforum.agreement;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.agreement.api.ext.AgreementExtensionApiTestClient;
import org.fiware.agreement.model.AgreementCreateVO;
import org.fiware.agreement.model.AgreementCreateVOTestExample;
import org.fiware.agreement.model.AgreementVO;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.tmforum.agreement.domain.Agreement;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(packages = {"org.fiware.tmforum.agreement"})
public class DisabledAgreementExtensionApiIT extends AbstractApiIT {

    private final AgreementExtensionApiTestClient testClient;

    protected DisabledAgreementExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                              GeneralProperties generalProperties,
                                              AgreementExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @Override
    protected String getEntityType() {
        return Agreement.TYPE_AGREEMENT;
    }

    @Test
    public void createAgreementWithId405() throws Exception {
        AgreementCreateVO createVO = AgreementCreateVOTestExample.build().atSchemaLocation(null).agreementSpecification(null);
        HttpResponse<AgreementVO> response = callAndCatch(
                () -> testClient.createAgreementWithId(null, "urn:ngsi-ld:agreement:test", createVO));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }
}
