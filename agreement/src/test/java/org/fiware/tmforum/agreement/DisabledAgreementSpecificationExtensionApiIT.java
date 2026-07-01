package org.fiware.tmforum.agreement;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.agreement.api.ext.AgreementSpecificationExtensionApiTestClient;
import org.fiware.agreement.model.AgreementSpecificationCreateVO;
import org.fiware.agreement.model.AgreementSpecificationCreateVOTestExample;
import org.fiware.agreement.model.AgreementSpecificationVO;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.tmforum.agreement.domain.AgreementSpecification;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(packages = {"org.fiware.tmforum.agreement"})
public class DisabledAgreementSpecificationExtensionApiIT extends AbstractApiIT {

    private final AgreementSpecificationExtensionApiTestClient testClient;

    protected DisabledAgreementSpecificationExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                                           GeneralProperties generalProperties,
                                                           AgreementSpecificationExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @Override
    protected String getEntityType() {
        return AgreementSpecification.TYPE_AGREEMENT_SPECIFICATION;
    }

    @Test
    public void createAgreementSpecificationWithId405() throws Exception {
        AgreementSpecificationCreateVO createVO = AgreementSpecificationCreateVOTestExample.build().atSchemaLocation(null).serviceCategory(null);
        HttpResponse<AgreementSpecificationVO> response = callAndCatch(
                () -> testClient.createAgreementSpecificationWithId(null, "urn:ngsi-ld:agreementSpecification:test", createVO));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }
}
