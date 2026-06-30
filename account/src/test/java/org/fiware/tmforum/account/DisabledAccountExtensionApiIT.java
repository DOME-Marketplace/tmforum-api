package org.fiware.tmforum.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.account.api.ext.BillingAccountExtensionApiTestClient;
import org.fiware.account.api.ext.BillingCycleSpecificationExtensionApiTestClient;
import org.fiware.account.api.ext.FinancialAccountExtensionApiTestClient;
import org.fiware.account.api.ext.SettlementAccountExtensionApiTestClient;
import org.fiware.account.model.*;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.tmforum.account.domain.BillingAccount;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(packages = {"org.fiware.tmforum.account"})
public class DisabledAccountExtensionApiIT extends AbstractApiIT {

    private final BillingAccountExtensionApiTestClient billingAccountExtClient;
    private final BillingCycleSpecificationExtensionApiTestClient billingCycleSpecExtClient;
    private final FinancialAccountExtensionApiTestClient financialAccountExtClient;
    private final SettlementAccountExtensionApiTestClient settlementAccountExtClient;

    protected DisabledAccountExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                            GeneralProperties generalProperties,
                                            BillingAccountExtensionApiTestClient billingAccountExtClient,
                                            BillingCycleSpecificationExtensionApiTestClient billingCycleSpecExtClient,
                                            FinancialAccountExtensionApiTestClient financialAccountExtClient,
                                            SettlementAccountExtensionApiTestClient settlementAccountExtClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.billingAccountExtClient = billingAccountExtClient;
        this.billingCycleSpecExtClient = billingCycleSpecExtClient;
        this.financialAccountExtClient = financialAccountExtClient;
        this.settlementAccountExtClient = settlementAccountExtClient;
    }

    @Override
    protected String getEntityType() {
        return BillingAccount.TYPE_BILLINGAC;
    }

    @Test
    public void createBillingAccountWithId405() throws Exception {
        BillingAccountCreateVO createVO = BillingAccountCreateVOTestExample.build().atSchemaLocation(null)
                .defaultPaymentMethod(null).financialAccount(null);
        HttpResponse<BillingAccountVO> response = callAndCatch(
                () -> billingAccountExtClient.createBillingAccountWithId(null, "urn:ngsi-ld:billing-account:test", createVO));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }

    @Test
    public void createBillingCycleSpecificationWithId405() throws Exception {
        BillingCycleSpecificationCreateVO createVO = BillingCycleSpecificationCreateVOTestExample.build().atSchemaLocation(null);
        HttpResponse<BillingCycleSpecificationVO> response = callAndCatch(
                () -> billingCycleSpecExtClient.createBillingCycleSpecificationWithId(null, "urn:ngsi-ld:billingCycleSpecification:test", createVO));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }

    @Test
    public void createFinancialAccountWithId405() throws Exception {
        FinancialAccountCreateVO createVO = FinancialAccountCreateVOTestExample.build().atSchemaLocation(null);
        HttpResponse<FinancialAccountVO> response = callAndCatch(
                () -> financialAccountExtClient.createFinancialAccountWithId(null, "urn:ngsi-ld:financialAccount:test", createVO));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }

    @Test
    public void createSettlementAccountWithId405() throws Exception {
        SettlementAccountCreateVO createVO = SettlementAccountCreateVOTestExample.build().atSchemaLocation(null)
                .defaultPaymentMethod(null).financialAccount(null);
        HttpResponse<SettlementAccountVO> response = callAndCatch(
                () -> settlementAccountExtClient.createSettlementAccountWithId(null, "urn:ngsi-ld:settlement-account:test", createVO));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }
}
