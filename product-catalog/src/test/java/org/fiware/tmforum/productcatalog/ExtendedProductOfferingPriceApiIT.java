package org.fiware.tmforum.productcatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.productcatalog.api.ext.ProductOfferingPriceExtensionApiTestClient;
import org.fiware.productcatalog.api.ext.ProductOfferingPriceExtensionApiTestSpec;
import org.fiware.productcatalog.model.ProductOfferingPriceCreateVO;
import org.fiware.productcatalog.model.ProductOfferingPriceCreateVOTestExample;
import org.fiware.productcatalog.model.ProductOfferingPriceVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.product.ProductOfferingPrice;
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

@MicronautTest(packages = {"org.fiware.tmforum.productcatalog"})
@Property(name = "apiExtension.enabled", value = "true")
@Property(name = "apiExtension.putEnabled", value = "true")
public class ExtendedProductOfferingPriceApiIT extends AbstractApiIT implements ProductOfferingPriceExtensionApiTestSpec {

    private final ProductOfferingPriceExtensionApiTestClient testClient;

    public ExtendedProductOfferingPriceApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                             GeneralProperties generalProperties,
                                             ProductOfferingPriceExtensionApiTestClient testClient) {
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
        return ProductOfferingPrice.TYPE_PRODUCT_OFFERING_PRICE;
    }

    private static ProductOfferingPriceCreateVO buildCreateVO() {
        return ProductOfferingPriceCreateVOTestExample.build().atSchemaLocation(null);
    }

    @Test
    @Override
    public void createProductOfferingPriceWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), ProductOfferingPrice.TYPE_PRODUCT_OFFERING_PRICE).toString();

        HttpResponse<ProductOfferingPriceVO> response = callAndCatch(
                () -> testClient.createProductOfferingPriceWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "ProductOfferingPrice should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createProductOfferingPriceWithId400() throws Exception {
        HttpResponse<ProductOfferingPriceVO> response = callAndCatch(
                () -> testClient.createProductOfferingPriceWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createProductOfferingPriceWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createProductOfferingPriceWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createProductOfferingPriceWithId405() throws Exception {
    }

    @Test
    @Override
    public void createProductOfferingPriceWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), ProductOfferingPrice.TYPE_PRODUCT_OFFERING_PRICE).toString();

        HttpResponse<ProductOfferingPriceVO> firstResponse = callAndCatch(
                () -> testClient.createProductOfferingPriceWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<ProductOfferingPriceVO> secondResponse = callAndCatch(
                () -> testClient.createProductOfferingPriceWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createProductOfferingPriceWithId500() throws Exception {
    }
}
