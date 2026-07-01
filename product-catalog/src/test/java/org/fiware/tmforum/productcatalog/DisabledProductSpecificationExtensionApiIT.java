package org.fiware.tmforum.productcatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.productcatalog.api.ext.ProductSpecificationExtensionApiTestClient;
import org.fiware.productcatalog.model.ProductSpecificationCreateVO;
import org.fiware.productcatalog.model.ProductSpecificationCreateVOTestExample;
import org.fiware.productcatalog.model.ProductSpecificationVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.product.ProductSpecification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(packages = {"org.fiware.tmforum.productcatalog"})
public class DisabledProductSpecificationExtensionApiIT extends AbstractApiIT {

    private final ProductSpecificationExtensionApiTestClient testClient;

    protected DisabledProductSpecificationExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                                         GeneralProperties generalProperties,
                                                         ProductSpecificationExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @Override
    protected String getEntityType() {
        return ProductSpecification.TYPE_PRODUCT_SPECIFICATION;
    }

    @Test
    public void createProductSpecificationWithId405() throws Exception {
        ProductSpecificationCreateVO createVO = ProductSpecificationCreateVOTestExample.build().atSchemaLocation(null).targetProductSchema(null);
        HttpResponse<ProductSpecificationVO> response = callAndCatch(
                () -> testClient.createProductSpecificationWithId(null, "urn:ngsi-ld:product-specification:test", createVO));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }
}
