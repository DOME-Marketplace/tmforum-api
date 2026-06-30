package org.fiware.tmforum.productordering;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.productordering.api.ext.ProductOrderExtensionApiTestClient;
import org.fiware.productordering.model.ProductOrderCreateVO;
import org.fiware.productordering.model.ProductOrderCreateVOTestExample;
import org.fiware.productordering.model.ProductOrderVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.productordering.domain.ProductOrder;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@MicronautTest(packages = {"org.fiware.tmforum.productordering"})
public class DisabledProductOrderExtensionApiIT extends AbstractApiIT {

    private final ProductOrderExtensionApiTestClient productOrderExtensionApiTestClient;

    @MockBean(Clock.class)
    public Clock clock() {
        return mock(Clock.class);
    }

    protected DisabledProductOrderExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                                 GeneralProperties generalProperties,
                                                 ProductOrderExtensionApiTestClient productOrderExtensionApiTestClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.productOrderExtensionApiTestClient = productOrderExtensionApiTestClient;
    }

    @Override
    protected String getEntityType() {
        return ProductOrder.TYPE_PRODUCT_ORDER;
    }

    @Test
    public void createProductOrderWithId405() throws Exception {
        ProductOrderCreateVO createVO = ProductOrderCreateVOTestExample.build().atSchemaLocation(null).billingAccount(null);
        HttpResponse<ProductOrderVO> response = callAndCatch(
                () -> productOrderExtensionApiTestClient.createProductOrderWithId(null,
                        "urn:ngsi-ld:product-order:test", createVO));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }
}
