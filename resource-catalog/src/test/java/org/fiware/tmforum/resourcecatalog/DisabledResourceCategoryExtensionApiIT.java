package org.fiware.tmforum.resourcecatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.resourcecatalog.api.ext.ResourceCategoryExtensionApiTestClient;
import org.fiware.resourcecatalog.model.ResourceCategoryCreateVOTestExample;
import org.fiware.resourcecatalog.model.ResourceCategoryVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.resource.ResourceCategory;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@MicronautTest(packages = {"org.fiware.tmforum.resourcecatalog"})
public class DisabledResourceCategoryExtensionApiIT extends AbstractApiIT {

    private final ResourceCategoryExtensionApiTestClient testClient;

    @MockBean(Clock.class)
    public Clock clock() {
        return mock(Clock.class);
    }

    protected DisabledResourceCategoryExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                                     GeneralProperties generalProperties,
                                                     ResourceCategoryExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @Override
    protected String getEntityType() {
        return ResourceCategory.TYPE_RESOURCE_CATEGORY;
    }

    @Test
    public void createResourceCategoryWithId405() throws Exception {
        HttpResponse<ResourceCategoryVO> response = callAndCatch(
                () -> testClient.createResourceCategoryWithId(null, "urn:ngsi-ld:resource-category:test",
                        ResourceCategoryCreateVOTestExample.build().atSchemaLocation(null)
                                .parentId(null).category(null).relatedParty(null).resourceCandidate(null)));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }
}
