package org.fiware.tmforum.productcatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.productcatalog.api.ext.ProductOfferingPriceExtensionApiTestClient;
import org.fiware.productcatalog.model.ProductOfferingPriceCreateVO;
import org.fiware.productcatalog.model.ProductOfferingPriceCreateVOTestExample;
import org.fiware.productcatalog.model.ProductOfferingPriceVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.product.ProductOfferingPrice;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(packages = {"org.fiware.tmforum.productcatalog"})
public class DisabledProductOfferingPriceExtensionApiIT extends AbstractApiIT {

    private final ProductOfferingPriceExtensionApiTestClient testClient;

    protected DisabledProductOfferingPriceExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                                         GeneralProperties generalProperties,
                                                         ProductOfferingPriceExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @Override
    protected String getEntityType() {
        return ProductOfferingPrice.TYPE_PRODUCT_OFFERING_PRICE;
    }

    @Test
    public void createProductOfferingPriceWithId405() throws Exception {
        ProductOfferingPriceCreateVO createVO = ProductOfferingPriceCreateVOTestExample.build().atSchemaLocation(null);
        HttpResponse<ProductOfferingPriceVO> response = callAndCatch(
                () -> testClient.createProductOfferingPriceWithId(null, "urn:ngsi-ld:product-offering-price:test", createVO));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }
}
