package org.fiware.tmforum.productcatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.productcatalog.api.ext.CategoryExtensionApiTestClient;
import org.fiware.productcatalog.model.CategoryCreateVO;
import org.fiware.productcatalog.model.CategoryCreateVOTestExample;
import org.fiware.productcatalog.model.CategoryVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.product.Category;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(packages = {"org.fiware.tmforum.productcatalog"})
public class DisabledCategoryExtensionApiIT extends AbstractApiIT {

    private final CategoryExtensionApiTestClient testClient;

    protected DisabledCategoryExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                             GeneralProperties generalProperties,
                                             CategoryExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @Override
    protected String getEntityType() {
        return Category.TYPE_CATEGORY;
    }

    @Test
    public void createCategoryWithId405() throws Exception {
        CategoryCreateVO createVO = CategoryCreateVOTestExample.build().atSchemaLocation(null).parentId(null);
        HttpResponse<CategoryVO> response = callAndCatch(
                () -> testClient.createCategoryWithId(null, "urn:ngsi-ld:category:test", createVO));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }
}
