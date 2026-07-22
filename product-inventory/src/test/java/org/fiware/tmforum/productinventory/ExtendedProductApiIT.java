package org.fiware.tmforum.productinventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.productinventory.api.ext.ProductExtensionApiTestClient;
import org.fiware.productinventory.api.ext.ProductExtensionApiTestSpec;
import org.fiware.productinventory.model.ProductCreateVO;
import org.fiware.productinventory.model.ProductCreateVOTestExample;
import org.fiware.productinventory.model.ProductVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.product.Product;
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

@MicronautTest(packages = {"org.fiware.tmforum.productinventory"})
@Property(name = "apiExtension.enabled", value = "true")
@Property(name = "apiExtension.putEnabled", value = "true")
public class ExtendedProductApiIT extends AbstractApiIT implements ProductExtensionApiTestSpec {

    private final ProductExtensionApiTestClient testClient;

    public ExtendedProductApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                GeneralProperties generalProperties,
                                ProductExtensionApiTestClient testClient) {
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
        return Product.TYPE_PRODUCT;
    }

    private static ProductCreateVO buildCreateVO() {
        return ProductCreateVOTestExample.build().atSchemaLocation(null)
                .productSpecification(null)
                .billingAccount(null)
                .productOffering(null);
    }

    @Test
    @Override
    public void createProductWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), Product.TYPE_PRODUCT).toString();

        HttpResponse<ProductVO> response = callAndCatch(
                () -> testClient.createProductWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "Product should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createProductWithId400() throws Exception {
        HttpResponse<ProductVO> response = callAndCatch(
                () -> testClient.createProductWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createProductWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createProductWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createProductWithId405() throws Exception {
    }

    @Test
    @Override
    public void createProductWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), Product.TYPE_PRODUCT).toString();

        HttpResponse<ProductVO> firstResponse = callAndCatch(
                () -> testClient.createProductWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<ProductVO> secondResponse = callAndCatch(
                () -> testClient.createProductWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createProductWithId500() throws Exception {
    }
}
