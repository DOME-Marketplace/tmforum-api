package org.fiware.tmforum.productcatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.productcatalog.api.ext.ProductSpecificationExtensionApiTestClient;
import org.fiware.productcatalog.api.ext.ProductSpecificationExtensionApiTestSpec;
import org.fiware.productcatalog.model.ProductSpecificationCreateVO;
import org.fiware.productcatalog.model.ProductSpecificationCreateVOTestExample;
import org.fiware.productcatalog.model.ProductSpecificationVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.product.ProductSpecification;
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
public class ExtendedProductSpecificationApiIT extends AbstractApiIT implements ProductSpecificationExtensionApiTestSpec {

    private final ProductSpecificationExtensionApiTestClient testClient;

    public ExtendedProductSpecificationApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                             GeneralProperties generalProperties,
                                             ProductSpecificationExtensionApiTestClient testClient) {
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
        return ProductSpecification.TYPE_PRODUCT_SPECIFICATION;
    }

    private static ProductSpecificationCreateVO buildCreateVO() {
        return ProductSpecificationCreateVOTestExample.build().atSchemaLocation(null).targetProductSchema(null);
    }

    @Test
    @Override
    public void createProductSpecificationWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), ProductSpecification.TYPE_PRODUCT_SPECIFICATION).toString();

        HttpResponse<ProductSpecificationVO> response = callAndCatch(
                () -> testClient.createProductSpecificationWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "ProductSpecification should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createProductSpecificationWithId400() throws Exception {
        HttpResponse<ProductSpecificationVO> response = callAndCatch(
                () -> testClient.createProductSpecificationWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createProductSpecificationWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createProductSpecificationWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createProductSpecificationWithId405() throws Exception {
    }

    @Test
    @Override
    public void createProductSpecificationWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), ProductSpecification.TYPE_PRODUCT_SPECIFICATION).toString();

        HttpResponse<ProductSpecificationVO> firstResponse = callAndCatch(
                () -> testClient.createProductSpecificationWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<ProductSpecificationVO> secondResponse = callAndCatch(
                () -> testClient.createProductSpecificationWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createProductSpecificationWithId500() throws Exception {
    }
}
