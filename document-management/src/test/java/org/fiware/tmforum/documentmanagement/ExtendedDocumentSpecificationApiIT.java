package org.fiware.tmforum.documentmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.document.api.ext.DocumentSpecificationExtensionApiTestClient;
import org.fiware.document.api.ext.DocumentSpecificationExtensionApiTestSpec;
import org.fiware.document.model.DocumentSpecificationCreateVO;
import org.fiware.document.model.DocumentSpecificationVO;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.documentmanagement.domain.DocumentSpecification;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest(packages = {"org.fiware.tmforum.documentmanagement"})
@Property(name = "apiExtension.enabled", value = "true")
@Property(name = "apiExtension.putEnabled", value = "true")
public class ExtendedDocumentSpecificationApiIT extends AbstractApiIT
        implements DocumentSpecificationExtensionApiTestSpec {

    private final DocumentSpecificationExtensionApiTestClient testClient;

    public ExtendedDocumentSpecificationApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                              GeneralProperties generalProperties,
                                              DocumentSpecificationExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @MockBean(TMForumEventHandler.class)
    public TMForumEventHandler eventHandler() {
        TMForumEventHandler eventHandler = mock(TMForumEventHandler.class);
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());
        when(eventHandler.handleUpdateEvent(any(), any())).thenReturn(Mono.empty());
        return eventHandler;
    }

    @Override
    protected String getEntityType() {
        return DocumentSpecification.TYPE_DOCUMENT_SPECIFICATION;
    }

    private static DocumentSpecificationCreateVO buildCreateVO() {
        DocumentSpecificationCreateVO createVO = new DocumentSpecificationCreateVO();
        createVO.setName("Test Document Specification");
        return createVO;
    }

    @Test
    @Override
    public void createDocumentSpecificationWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(),
                DocumentSpecification.TYPE_DOCUMENT_SPECIFICATION).toString();

        HttpResponse<DocumentSpecificationVO> response = callAndCatch(
                () -> testClient.createDocumentSpecificationWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(),
                "DocumentSpecification should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createDocumentSpecificationWithId400() throws Exception {
        HttpResponse<DocumentSpecificationVO> response = callAndCatch(
                () -> testClient.createDocumentSpecificationWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createDocumentSpecificationWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createDocumentSpecificationWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createDocumentSpecificationWithId405() throws Exception {
    }

    @Test
    @Override
    public void createDocumentSpecificationWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(),
                DocumentSpecification.TYPE_DOCUMENT_SPECIFICATION).toString();

        HttpResponse<DocumentSpecificationVO> firstResponse = callAndCatch(
                () -> testClient.createDocumentSpecificationWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<DocumentSpecificationVO> secondResponse = callAndCatch(
                () -> testClient.createDocumentSpecificationWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(),
                "Second creation with the same id should fail.");
    }

    @Override
    public void createDocumentSpecificationWithId500() throws Exception {
    }
}
