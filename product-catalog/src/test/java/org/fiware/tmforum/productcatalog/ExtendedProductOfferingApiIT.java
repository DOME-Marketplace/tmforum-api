package org.fiware.tmforum.productcatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.productcatalog.api.ext.ProductOfferingExtensionApiTestClient;
import org.fiware.productcatalog.api.ext.ProductOfferingExtensionApiTestSpec;
import org.fiware.productcatalog.model.ProductOfferingCreateVO;
import org.fiware.productcatalog.model.ProductOfferingCreateVOTestExample;
import org.fiware.productcatalog.model.ProductOfferingVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.product.ProductOffering;
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
public class ExtendedProductOfferingApiIT extends AbstractApiIT implements ProductOfferingExtensionApiTestSpec {

    private final ProductOfferingExtensionApiTestClient testClient;

    public ExtendedProductOfferingApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                        GeneralProperties generalProperties,
                                        ProductOfferingExtensionApiTestClient testClient) {
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
        return ProductOffering.TYPE_PRODUCT_OFFERING;
    }

    private static ProductOfferingCreateVO buildCreateVO() {
        ProductOfferingCreateVO createVO = ProductOfferingCreateVOTestExample.build().atSchemaLocation(null);
        createVO.setProductSpecification(null);
        createVO.setResourceCandidate(null);
        createVO.setServiceCandidate(null);
        createVO.setServiceLevelAgreement(null);
        return createVO;
    }

    @Test
    @Override
    public void createProductOfferingWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), ProductOffering.TYPE_PRODUCT_OFFERING).toString();

        HttpResponse<ProductOfferingVO> response = callAndCatch(
                () -> testClient.createProductOfferingWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "ProductOffering should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createProductOfferingWithId400() throws Exception {
        HttpResponse<ProductOfferingVO> response = callAndCatch(
                () -> testClient.createProductOfferingWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createProductOfferingWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createProductOfferingWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createProductOfferingWithId405() throws Exception {
    }

    @Test
    @Override
    public void createProductOfferingWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), ProductOffering.TYPE_PRODUCT_OFFERING).toString();

        HttpResponse<ProductOfferingVO> firstResponse = callAndCatch(
                () -> testClient.createProductOfferingWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<ProductOfferingVO> secondResponse = callAndCatch(
                () -> testClient.createProductOfferingWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createProductOfferingWithId500() throws Exception {
    }
}
