package org.fiware.tmforum.documentmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.document.api.ext.DocumentSpecificationExtensionApiTestClient;
import org.fiware.document.model.DocumentSpecificationCreateVO;
import org.fiware.document.model.DocumentSpecificationVO;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.documentmanagement.domain.DocumentSpecification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(packages = {"org.fiware.tmforum.documentmanagement"})
public class DisabledDocumentSpecificationExtensionApiIT extends AbstractApiIT {

    private final DocumentSpecificationExtensionApiTestClient documentSpecificationExtensionApiTestClient;

    protected DisabledDocumentSpecificationExtensionApiIT(EntitiesApiClient entitiesApiClient,
                                                          ObjectMapper objectMapper,
                                                          GeneralProperties generalProperties,
                                                          DocumentSpecificationExtensionApiTestClient documentSpecificationExtensionApiTestClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.documentSpecificationExtensionApiTestClient = documentSpecificationExtensionApiTestClient;
    }

    @Override
    protected String getEntityType() {
        return DocumentSpecification.TYPE_DOCUMENT_SPECIFICATION;
    }

    @Test
    public void createDocumentSpecificationWithId405() throws Exception {
        DocumentSpecificationCreateVO createVO = new DocumentSpecificationCreateVO();
        createVO.setName("Test Document Specification");
        HttpResponse<DocumentSpecificationVO> response = callAndCatch(
                () -> documentSpecificationExtensionApiTestClient.createDocumentSpecificationWithId(
                        null, "urn:ngsi-ld:document-specification:test", createVO));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }
}
