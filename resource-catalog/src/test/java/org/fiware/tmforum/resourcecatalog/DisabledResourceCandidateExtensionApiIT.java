package org.fiware.tmforum.resourcecatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.resourcecatalog.api.ext.ResourceCandidateExtensionApiTestClient;
import org.fiware.resourcecatalog.model.ResourceCandidateCreateVOTestExample;
import org.fiware.resourcecatalog.model.ResourceCandidateVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.resource.ResourceCandidate;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@MicronautTest(packages = {"org.fiware.tmforum.resourcecatalog"})
public class DisabledResourceCandidateExtensionApiIT extends AbstractApiIT {

    private final ResourceCandidateExtensionApiTestClient testClient;

    @MockBean(Clock.class)
    public Clock clock() {
        return mock(Clock.class);
    }

    protected DisabledResourceCandidateExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                                      GeneralProperties generalProperties,
                                                      ResourceCandidateExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @Override
    protected String getEntityType() {
        return ResourceCandidate.TYPE_RESOURCE_CANDIDATE;
    }

    @Test
    public void createResourceCandidateWithId405() throws Exception {
        HttpResponse<ResourceCandidateVO> response = callAndCatch(
                () -> testClient.createResourceCandidateWithId(null, "urn:ngsi-ld:resource-candidate:test",
                        ResourceCandidateCreateVOTestExample.build().atSchemaLocation(null).resourceSpecification(null)));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }
}
