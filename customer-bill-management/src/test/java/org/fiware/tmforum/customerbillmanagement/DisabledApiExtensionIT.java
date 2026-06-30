package org.fiware.tmforum.customerbillmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.customerbillmanagement.api.ext.AppliedCustomerBillingRateExtensionApiTestClient;
import org.fiware.customerbillmanagement.api.ext.CustomerBillExtensionApiTestClient;
import org.fiware.customerbillmanagement.api.ext.CustomerBillOnDemandExtensionApiTestClient;
import org.fiware.customerbillmanagement.model.*;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.customerbillmanagement.domain.AppliedCustomerBillingRate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(packages = {"org.fiware.tmforum.customerbillmanagement"})
public class DisabledApiExtensionIT extends AbstractApiIT {

    private final AppliedCustomerBillingRateExtensionApiTestClient appliedCustomerBillingRateExtensionApiTestClient;
    private final CustomerBillExtensionApiTestClient customerBillExtensionApiTestClient;
    private final CustomerBillOnDemandExtensionApiTestClient customerBillOnDemandExtensionApiTestClient;

    protected DisabledApiExtensionIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                     GeneralProperties generalProperties,
                                     AppliedCustomerBillingRateExtensionApiTestClient appliedCustomerBillingRateExtensionApiTestClient,
                                     CustomerBillExtensionApiTestClient customerBillExtensionApiTestClient,
                                     CustomerBillOnDemandExtensionApiTestClient customerBillOnDemandExtensionApiTestClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.appliedCustomerBillingRateExtensionApiTestClient = appliedCustomerBillingRateExtensionApiTestClient;
        this.customerBillExtensionApiTestClient = customerBillExtensionApiTestClient;
        this.customerBillOnDemandExtensionApiTestClient = customerBillOnDemandExtensionApiTestClient;
    }

    @Test
    public void createAppliedCustomerBillingRate405() throws Exception {
        HttpResponse<AppliedCustomerBillingRateVO> response = callAndCatch(
                () -> appliedCustomerBillingRateExtensionApiTestClient.createAppliedCustomerBillingRate(null,
                        AppliedCustomerBillingRateCreateVOTestExample.build().atSchemaLocation(null)));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When the extension API is not enabled, creation should not be supported.");
    }

    @Test
    public void updateAppliedCustomerBillingRate405() throws Exception {
        AppliedCustomerBillingRateUpdateVO updateVO = AppliedCustomerBillingRateUpdateVOTestExample.build().atSchemaLocation(null);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED,
                callAndCatch(() -> appliedCustomerBillingRateExtensionApiTestClient.updateAppliedCustomerBillingRate(null,
                        "urn:ngsi-ld:applied-customer-billing-rate:some-id", updateVO)).getStatus(),
                "When the extension API is not enabled, updates should not be supported.");
    }

    @Test
    public void createAppliedCustomerBillingRateWithId405() throws Exception {
        HttpResponse<AppliedCustomerBillingRateVO> response = callAndCatch(
                () -> appliedCustomerBillingRateExtensionApiTestClient.createAppliedCustomerBillingRateWithId(null,
                        "urn:ngsi-ld:applied-customer-billing-rate:test",
                        AppliedCustomerBillingRateCreateVOTestExample.build().atSchemaLocation(null)));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }

    @Test
    public void createCustomerBillWithId405() throws Exception {
        HttpResponse<CustomerBillVO> response = callAndCatch(
                () -> customerBillExtensionApiTestClient.createCustomerBillWithId(null,
                        "urn:ngsi-ld:customer-bill:test",
                        CustomerBillCreateVOTestExample.build().atSchemaLocation(null)
                                .billingAccount(null).financialAccount(null).paymentMethod(null)));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }

    @Test
    public void createCustomerBillOnDemandWithId405() throws Exception {
        HttpResponse<CustomerBillOnDemandVO> response = callAndCatch(
                () -> customerBillOnDemandExtensionApiTestClient.createCustomerBillOnDemandWithId(null,
                        "urn:ngsi-ld:customer-bill-on-demand:test",
                        CustomerBillOnDemandCreateVOTestExample.build().atSchemaLocation(null)
                                .billingAccount(null).relatedParty(null).customerBill(null)));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }

    @Override
    protected String getEntityType() {
        return AppliedCustomerBillingRate.TYPE_APPLIED_CUSTOMER_BILLING_RATE;
    }
}
