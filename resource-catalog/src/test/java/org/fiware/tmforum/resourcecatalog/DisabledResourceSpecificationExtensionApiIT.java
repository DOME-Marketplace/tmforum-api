package org.fiware.tmforum.resourcecatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.resourcecatalog.api.ext.ResourceSpecificationExtensionApiTestClient;
import org.fiware.resourcecatalog.model.ResourceSpecificationCreateVOTestExample;
import org.fiware.resourcecatalog.model.ResourceSpecificationVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.resource.ResourceSpecification;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@MicronautTest(packages = {"org.fiware.tmforum.resourcecatalog"})
public class DisabledResourceSpecificationExtensionApiIT extends AbstractApiIT {

    private final ResourceSpecificationExtensionApiTestClient testClient;

    @MockBean(Clock.class)
    public Clock clock() {
        return mock(Clock.class);
    }

    protected DisabledResourceSpecificationExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                                          GeneralProperties generalProperties,
                                                          ResourceSpecificationExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @Override
    protected String getEntityType() {
        return ResourceSpecification.TYPE_RESOURCE_SPECIFICATION;
    }

    @Test
    public void createResourceSpecificationWithId405() throws Exception {
        HttpResponse<ResourceSpecificationVO> response = callAndCatch(
                () -> testClient.createResourceSpecificationWithId(null, "urn:ngsi-ld:resource-specification:test",
                        ResourceSpecificationCreateVOTestExample.build().atSchemaLocation(null).relatedParty(null).targetResourceSchema(null)));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }
}
