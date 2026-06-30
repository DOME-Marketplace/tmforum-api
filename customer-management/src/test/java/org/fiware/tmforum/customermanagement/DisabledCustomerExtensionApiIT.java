package org.fiware.tmforum.customermanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.customermanagement.api.ext.CustomerExtensionApiTestClient;
import org.fiware.customermanagement.model.CustomerCreateVO;
import org.fiware.customermanagement.model.CustomerCreateVOTestExample;
import org.fiware.customermanagement.model.CustomerVO;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.customermanagement.domain.Customer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(packages = {"org.fiware.tmforum.customermanagement"})
public class DisabledCustomerExtensionApiIT extends AbstractApiIT {

    private final CustomerExtensionApiTestClient customerExtensionApiTestClient;

    protected DisabledCustomerExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                             GeneralProperties generalProperties,
                                             CustomerExtensionApiTestClient customerExtensionApiTestClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.customerExtensionApiTestClient = customerExtensionApiTestClient;
    }

    @Override
    protected String getEntityType() {
        return Customer.TYPE_CUSTOMER;
    }

    @Test
    public void createCustomerWithId405() throws Exception {
        CustomerCreateVO createVO = CustomerCreateVOTestExample.build().atSchemaLocation(null).engagedParty(null);
        HttpResponse<CustomerVO> response = callAndCatch(
                () -> customerExtensionApiTestClient.createCustomerWithId(null, "urn:ngsi-ld:customer:test", createVO));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }
}
