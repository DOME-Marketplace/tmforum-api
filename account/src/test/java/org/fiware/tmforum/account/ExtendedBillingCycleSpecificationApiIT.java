package org.fiware.tmforum.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.account.api.ext.BillingCycleSpecificationExtensionApiTestClient;
import org.fiware.account.api.ext.BillingCycleSpecificationExtensionApiTestSpec;
import org.fiware.account.model.BillingCycleSpecificationCreateVO;
import org.fiware.account.model.BillingCycleSpecificationCreateVOTestExample;
import org.fiware.account.model.BillingCycleSpecificationVO;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.tmforum.account.domain.BillingCycleSpecification;
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
public class ExtendedBillingCycleSpecificationApiIT extends AbstractApiIT implements BillingCycleSpecificationExtensionApiTestSpec {

    private final BillingCycleSpecificationExtensionApiTestClient testClient;

    public ExtendedBillingCycleSpecificationApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                                   GeneralProperties generalProperties,
                                                   BillingCycleSpecificationExtensionApiTestClient testClient) {
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
        return BillingCycleSpecification.TYPE_BILLCL;
    }

    private static BillingCycleSpecificationCreateVO buildCreateVO() {
        return BillingCycleSpecificationCreateVOTestExample.build().atSchemaLocation(null);
    }

    @Test
    @Override
    public void createBillingCycleSpecificationWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), BillingCycleSpecification.TYPE_BILLCL).toString();

        HttpResponse<BillingCycleSpecificationVO> response = callAndCatch(
                () -> testClient.createBillingCycleSpecificationWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "BillingCycleSpecification should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createBillingCycleSpecificationWithId400() throws Exception {
        HttpResponse<BillingCycleSpecificationVO> response = callAndCatch(
                () -> testClient.createBillingCycleSpecificationWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createBillingCycleSpecificationWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createBillingCycleSpecificationWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createBillingCycleSpecificationWithId405() throws Exception {
    }

    @Test
    @Override
    public void createBillingCycleSpecificationWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), BillingCycleSpecification.TYPE_BILLCL).toString();

        HttpResponse<BillingCycleSpecificationVO> firstResponse = callAndCatch(
                () -> testClient.createBillingCycleSpecificationWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<BillingCycleSpecificationVO> secondResponse = callAndCatch(
                () -> testClient.createBillingCycleSpecificationWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createBillingCycleSpecificationWithId500() throws Exception {
    }
}
