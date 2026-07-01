package org.fiware.tmforum.productcatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.productcatalog.api.ext.ProductOfferingExtensionApiTestClient;
import org.fiware.productcatalog.model.ProductOfferingCreateVO;
import org.fiware.productcatalog.model.ProductOfferingCreateVOTestExample;
import org.fiware.productcatalog.model.ProductOfferingVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.product.ProductOffering;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(packages = {"org.fiware.tmforum.productcatalog"})
public class DisabledProductOfferingExtensionApiIT extends AbstractApiIT {

    private final ProductOfferingExtensionApiTestClient testClient;

    protected DisabledProductOfferingExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                                    GeneralProperties generalProperties,
                                                    ProductOfferingExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @Override
    protected String getEntityType() {
        return ProductOffering.TYPE_PRODUCT_OFFERING;
    }

    @Test
    public void createProductOfferingWithId405() throws Exception {
        ProductOfferingCreateVO createVO = ProductOfferingCreateVOTestExample.build().atSchemaLocation(null);
        createVO.setProductSpecification(null);
        createVO.setResourceCandidate(null);
        createVO.setServiceCandidate(null);
        createVO.setServiceLevelAgreement(null);
        HttpResponse<ProductOfferingVO> response = callAndCatch(
                () -> testClient.createProductOfferingWithId(null, "urn:ngsi-ld:product-offering:test", createVO));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }
}
