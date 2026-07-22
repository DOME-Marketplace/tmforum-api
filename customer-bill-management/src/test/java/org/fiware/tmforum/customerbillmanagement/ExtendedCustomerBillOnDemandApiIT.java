package org.fiware.tmforum.customerbillmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.customerbillmanagement.api.CustomerBillOnDemandApiTestClient;
import org.fiware.customerbillmanagement.api.ext.CustomerBillOnDemandExtensionApiTestClient;
import org.fiware.customerbillmanagement.api.ext.CustomerBillOnDemandExtensionApiTestSpec;
import org.fiware.customerbillmanagement.model.CustomerBillOnDemandCreateVO;
import org.fiware.customerbillmanagement.model.CustomerBillOnDemandCreateVOTestExample;
import org.fiware.customerbillmanagement.model.CustomerBillOnDemandVO;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.customerbillmanagement.domain.CustomerBillOnDemand;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest(packages = {"org.fiware.tmforum.customerbillmanagement"})
@Property(name = "apiExtension.enabled", value = "true")
@Property(name = "apiExtension.putEnabled", value = "true")
@Property(name = "apiExtension.deleteEnabled", value = "true")
public class ExtendedCustomerBillOnDemandApiIT extends AbstractApiIT
        implements CustomerBillOnDemandExtensionApiTestSpec {

    private final CustomerBillOnDemandExtensionApiTestClient extensionTestClient;
    private final CustomerBillOnDemandApiTestClient baseTestClient;

    public ExtendedCustomerBillOnDemandApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
            GeneralProperties generalProperties,
            CustomerBillOnDemandExtensionApiTestClient extensionTestClient,
            CustomerBillOnDemandApiTestClient baseTestClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.extensionTestClient = extensionTestClient;
        this.baseTestClient = baseTestClient;
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
        return CustomerBillOnDemand.TYPE_CUSTOMER_BILL_ON_DEMAND;
    }

    @Test
    @Override
    public void createCustomerBillOnDemandWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), CustomerBillOnDemand.TYPE_CUSTOMER_BILL_ON_DEMAND).toString();
        CustomerBillOnDemandCreateVO createVO = CustomerBillOnDemandCreateVOTestExample.build()
                .atSchemaLocation(null)
                .lastUpdate(null)
                .billingAccount(null)
                .relatedParty(null)
                .customerBill(null);

        HttpResponse<CustomerBillOnDemandVO> response = callAndCatch(
                () -> extensionTestClient.createCustomerBillOnDemandWithId(null, id, createVO));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "CustomerBillOnDemand should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createCustomerBillOnDemandWithId400() throws Exception {
        HttpResponse<CustomerBillOnDemandVO> response = callAndCatch(
                () -> extensionTestClient.createCustomerBillOnDemandWithId(null, "invalid-id",
                        CustomerBillOnDemandCreateVOTestExample.build()
                                .atSchemaLocation(null).billingAccount(null).relatedParty(null).customerBill(null)));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally, thus 401 and 403 cannot happen.")
    @Test
    @Override
    public void createCustomerBillOnDemandWithId401() throws Exception {
    }

    @Disabled("Security is handled externally, thus 401 and 403 cannot happen.")
    @Test
    @Override
    public void createCustomerBillOnDemandWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createCustomerBillOnDemandWithId405() throws Exception {
    }

    @Test
    @Override
    public void createCustomerBillOnDemandWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), CustomerBillOnDemand.TYPE_CUSTOMER_BILL_ON_DEMAND).toString();
        CustomerBillOnDemandCreateVO createVO = CustomerBillOnDemandCreateVOTestExample.build()
                .atSchemaLocation(null).lastUpdate(null).billingAccount(null).relatedParty(null).customerBill(null);

        HttpResponse<CustomerBillOnDemandVO> first = callAndCatch(
                () -> extensionTestClient.createCustomerBillOnDemandWithId(null, id, createVO));
        assertEquals(HttpStatus.CREATED, first.getStatus(), "First creation should succeed.");

        HttpResponse<CustomerBillOnDemandVO> second = callAndCatch(
                () -> extensionTestClient.createCustomerBillOnDemandWithId(null, id, createVO));
        assertEquals(HttpStatus.CONFLICT, second.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createCustomerBillOnDemandWithId500() throws Exception {
    }

    @Test
    @Override
    public void deleteCustomerBillOnDemand204() throws Exception {
        CustomerBillOnDemandCreateVO createVO = CustomerBillOnDemandCreateVOTestExample.build()
                .atSchemaLocation(null)
                .lastUpdate(Instant.MAX.toString())
                .billingAccount(null)
                .relatedParty(null)
                .customerBill(null);

        HttpResponse<CustomerBillOnDemandVO> createResponse = callAndCatch(
                () -> baseTestClient.createCustomerBillOnDemand(null, createVO));
        assertEquals(HttpStatus.CREATED, createResponse.getStatus(), "CustomerBillOnDemand should have been created.");
        String id = createResponse.body().getId();

        assertEquals(HttpStatus.NO_CONTENT,
                callAndCatch(() -> extensionTestClient.deleteCustomerBillOnDemand(null, id)).getStatus(),
                "CustomerBillOnDemand should have been deleted.");

        assertEquals(HttpStatus.NOT_FOUND,
                callAndCatch(() -> baseTestClient.retrieveCustomerBillOnDemand(null, id, null)).getStatus(),
                "CustomerBillOnDemand should not exist anymore.");
    }

    @Disabled("400 is impossible to happen on deletion with the current implementation.")
    @Test
    @Override
    public void deleteCustomerBillOnDemand400() throws Exception {
    }

    @Disabled("Security is handled externally, thus 401 and 403 cannot happen.")
    @Test
    @Override
    public void deleteCustomerBillOnDemand401() throws Exception {
    }

    @Disabled("Security is handled externally, thus 401 and 403 cannot happen.")
    @Test
    @Override
    public void deleteCustomerBillOnDemand403() throws Exception {
    }

    @Test
    @Override
    public void deleteCustomerBillOnDemand404() throws Exception {
        HttpResponse<?> notFoundResponse = callAndCatch(
                () -> extensionTestClient.deleteCustomerBillOnDemand(null,
                        "urn:ngsi-ld:customer-bill-on-demand:no-such-entity"));
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatus(), "No such entity should exist.");
        Optional<ErrorDetails> optionalErrorDetails = notFoundResponse.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");

        notFoundResponse = callAndCatch(
                () -> extensionTestClient.deleteCustomerBillOnDemand(null, "invalid-id"));
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatus(), "No such entity should exist.");
        optionalErrorDetails = notFoundResponse.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Override
    public void deleteCustomerBillOnDemand500() throws Exception {
    }
}
