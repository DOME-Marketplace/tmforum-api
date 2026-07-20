package org.fiware.tmforum.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.account.api.ext.SettlementAccountExtensionApiTestClient;
import org.fiware.account.api.ext.SettlementAccountExtensionApiTestSpec;
import org.fiware.account.model.*;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.tmforum.account.domain.SettlementAccount;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
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

@MicronautTest(packages = {"org.fiware.tmforum.account"})
@Property(name = "apiExtension.enabled", value = "true")
@Property(name = "apiExtension.putEnabled", value = "true")
public class ExtendedSettlementAccountApiIT extends AbstractApiIT implements SettlementAccountExtensionApiTestSpec {

    private final SettlementAccountExtensionApiTestClient testClient;

    public ExtendedSettlementAccountApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                          GeneralProperties generalProperties,
                                          SettlementAccountExtensionApiTestClient testClient) {
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
        return SettlementAccount.TYPE_SETTLEMENTAC;
    }

    private static SettlementAccountCreateVO buildCreateVO() {
        SettlementAccountCreateVO createVO = SettlementAccountCreateVOTestExample.build()
                .atSchemaLocation(null)
                .defaultPaymentMethod(null)
                .financialAccount(null);
        fixBillStructure(createVO);
        return createVO;
    }

    private static void fixBillStructure(SettlementAccountCreateVO settlementAccount) {
        BillingCycleSpecificationRefOrValueVO cycleSpec = BillingCycleSpecificationRefOrValueVOTestExample.build().atSchemaLocation(null);
        cycleSpec.setHref("http://my-ref.de");
        cycleSpec.setId("http://my-url.de");
        BillFormatRefOrValueVO billFormat = BillFormatRefOrValueVOTestExample.build().atSchemaLocation(null);
        billFormat.setHref("http://my-ref.de");
        billFormat.setId("http://my-url.de");
        BillStructureVO billStructure = BillStructureVOTestExample.build().atSchemaLocation(null);
        billStructure.setCycleSpecification(cycleSpec);
        billStructure.setFormat(billFormat);
        settlementAccount.setBillStructure(billStructure);
    }

    @Test
    @Override
    public void createSettlementAccountWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), SettlementAccount.TYPE_SETTLEMENTAC).toString();
        SettlementAccountCreateVO createVO = buildCreateVO();

        HttpResponse<SettlementAccountVO> response = callAndCatch(
                () -> testClient.createSettlementAccountWithId(null, id, createVO));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "SettlementAccount should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createSettlementAccountWithId400() throws Exception {
        HttpResponse<SettlementAccountVO> response = callAndCatch(
                () -> testClient.createSettlementAccountWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createSettlementAccountWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createSettlementAccountWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createSettlementAccountWithId405() throws Exception {
    }

    @Test
    @Override
    public void createSettlementAccountWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), SettlementAccount.TYPE_SETTLEMENTAC).toString();
        SettlementAccountCreateVO createVO = buildCreateVO();

        HttpResponse<SettlementAccountVO> firstResponse = callAndCatch(
                () -> testClient.createSettlementAccountWithId(null, id, createVO));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<SettlementAccountVO> secondResponse = callAndCatch(
                () -> testClient.createSettlementAccountWithId(null, id, createVO));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createSettlementAccountWithId500() throws Exception {
    }
}
