package org.fiware.tmforum.productordering;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.productordering.api.ext.CancelProductOrderExtensionApiTestClient;
import org.fiware.productordering.model.CancelProductOrderCreateVO;
import org.fiware.productordering.model.CancelProductOrderCreateVOTestExample;
import org.fiware.productordering.model.CancelProductOrderVO;
import org.fiware.productordering.model.ProductOrderRefVOTestExample;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.productordering.domain.CancelProductOrder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(packages = {"org.fiware.tmforum.productordering"})
public class DisabledCancelProductOrderExtensionApiIT extends AbstractApiIT {

    private final CancelProductOrderExtensionApiTestClient extensionTestClient;

    protected DisabledCancelProductOrderExtensionApiIT(EntitiesApiClient entitiesApiClient,
                                                       ObjectMapper objectMapper,
                                                       GeneralProperties generalProperties,
                                                       CancelProductOrderExtensionApiTestClient extensionTestClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.extensionTestClient = extensionTestClient;
    }

    @Override
    protected String getEntityType() {
        return CancelProductOrder.TYPE_CANCEL_PRODUCT_ORDER;
    }

    @Test
    public void createCancelProductOrderWithId405() throws Exception {
        CancelProductOrderCreateVO createVO = CancelProductOrderCreateVOTestExample.build()
                .atSchemaLocation(null)
                .productOrder(ProductOrderRefVOTestExample.build().atSchemaLocation(null)
                        .id("urn:ngsi-ld:product-order:test"));
        HttpResponse<CancelProductOrderVO> response = callAndCatch(
                () -> extensionTestClient.createCancelProductOrderWithId(null,
                        "urn:ngsi-ld:cancel-product-order:test", createVO));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }
}
