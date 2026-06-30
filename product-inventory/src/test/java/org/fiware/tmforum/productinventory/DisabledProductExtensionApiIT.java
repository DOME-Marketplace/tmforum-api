package org.fiware.tmforum.productinventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.productinventory.api.ext.ProductExtensionApiTestClient;
import org.fiware.productinventory.model.ProductCreateVO;
import org.fiware.productinventory.model.ProductCreateVOTestExample;
import org.fiware.productinventory.model.ProductVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.product.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(packages = {"org.fiware.tmforum.productinventory"})
public class DisabledProductExtensionApiIT extends AbstractApiIT {

    private final ProductExtensionApiTestClient productExtensionApiTestClient;

    protected DisabledProductExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                            GeneralProperties generalProperties,
                                            ProductExtensionApiTestClient productExtensionApiTestClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.productExtensionApiTestClient = productExtensionApiTestClient;
    }

    @Override
    protected String getEntityType() {
        return Product.TYPE_PRODUCT;
    }

    @Test
    public void createProductWithId405() throws Exception {
        ProductCreateVO createVO = ProductCreateVOTestExample.build().atSchemaLocation(null)
                .productSpecification(null)
                .billingAccount(null)
                .productOffering(null);
        HttpResponse<ProductVO> response = callAndCatch(
                () -> productExtensionApiTestClient.createProductWithId(null, "urn:ngsi-ld:product:test", createVO));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }
}
