package org.fiware.tmforum.productcatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.productcatalog.api.ext.CatalogExtensionApiTestClient;
import org.fiware.productcatalog.model.CatalogCreateVO;
import org.fiware.productcatalog.model.CatalogCreateVOTestExample;
import org.fiware.productcatalog.model.CatalogVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.productcatalog.domain.Catalog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(packages = {"org.fiware.tmforum.productcatalog"})
public class DisabledCatalogExtensionApiIT extends AbstractApiIT {

    private final CatalogExtensionApiTestClient testClient;

    protected DisabledCatalogExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                            GeneralProperties generalProperties,
                                            CatalogExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @Override
    protected String getEntityType() {
        return Catalog.TYPE_CATALOG;
    }

    @Test
    public void createCatalogWithId405() throws Exception {
        CatalogCreateVO createVO = CatalogCreateVOTestExample.build().atSchemaLocation(null);
        HttpResponse<CatalogVO> response = callAndCatch(
                () -> testClient.createCatalogWithId(null, "urn:ngsi-ld:catalog:test", createVO));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }
}
