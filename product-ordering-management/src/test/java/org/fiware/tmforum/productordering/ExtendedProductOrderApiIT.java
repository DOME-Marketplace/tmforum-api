package org.fiware.tmforum.productordering;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.productordering.api.ext.ProductOrderExtensionApiTestClient;
import org.fiware.productordering.api.ext.ProductOrderExtensionApiTestSpec;
import org.fiware.productordering.model.ProductOrderCreateVO;
import org.fiware.productordering.model.ProductOrderCreateVOTestExample;
import org.fiware.productordering.model.ProductOrderVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.productordering.domain.ProductOrder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest(packages = {"org.fiware.tmforum.productordering"})
@Property(name = "apiExtension.enabled", value = "true")
public class ExtendedProductOrderApiIT extends AbstractApiIT implements ProductOrderExtensionApiTestSpec {

    private final ProductOrderExtensionApiTestClient testClient;

    private Clock clock = mock(Clock.class);

    @MockBean(Clock.class)
    public Clock clock() {
        return clock;
    }

    @MockBean(TMForumEventHandler.class)
    public TMForumEventHandler eventHandler() {
        TMForumEventHandler eventHandler = mock(TMForumEventHandler.class);
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());
        when(eventHandler.handleUpdateEvent(any(), any())).thenReturn(Mono.empty());
        return eventHandler;
    }

    public ExtendedProductOrderApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                     GeneralProperties generalProperties,
                                     ProductOrderExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @Override
    protected String getEntityType() {
        return ProductOrder.TYPE_PRODUCT_ORDER;
    }

    private static ProductOrderCreateVO buildCreateVO() {
        return ProductOrderCreateVOTestExample.build().atSchemaLocation(null).billingAccount(null);
    }

    @Test
    @Override
    public void createProductOrderWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), ProductOrder.TYPE_PRODUCT_ORDER).toString();

        HttpResponse<ProductOrderVO> response = callAndCatch(
                () -> testClient.createProductOrderWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "ProductOrder should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createProductOrderWithId400() throws Exception {
        HttpResponse<ProductOrderVO> response = callAndCatch(
                () -> testClient.createProductOrderWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createProductOrderWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createProductOrderWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createProductOrderWithId405() throws Exception {
    }

    @Test
    @Override
    public void createProductOrderWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), ProductOrder.TYPE_PRODUCT_ORDER).toString();

        HttpResponse<ProductOrderVO> firstResponse = callAndCatch(
                () -> testClient.createProductOrderWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<ProductOrderVO> secondResponse = callAndCatch(
                () -> testClient.createProductOrderWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createProductOrderWithId500() throws Exception {
    }
}
