package org.fiware.tmforum.documentmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.document.api.DocumentSpecificationApiTestClient;
import org.fiware.document.api.DocumentSpecificationApiTestSpec;
import org.fiware.document.model.DocumentSpecificationCreateVO;
import org.fiware.document.model.DocumentSpecificationVO;
import org.fiware.document.model.AttachmentRefOrValueVO;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.documentmanagement.s3.S3AttachmentService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest(packages = {"org.fiware.tmforum.documentmanagement"})
public class DocumentSpecificationApiIT extends AbstractApiIT implements DocumentSpecificationApiTestSpec {

    public final DocumentSpecificationApiTestClient documentSpecificationApiTestClient;

    private String message;
    private DocumentSpecificationCreateVO createVO;
    private DocumentSpecificationVO expectedDocSpec;

    public DocumentSpecificationApiIT(
            DocumentSpecificationApiTestClient documentSpecificationApiTestClient,
            EntitiesApiClient entitiesApiClient,
            ObjectMapper objectMapper,
            GeneralProperties generalProperties) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.documentSpecificationApiTestClient = documentSpecificationApiTestClient;
    }

    @Override
    protected String getEntityType() {
        return "document-specification";
    }

    @MockBean(TMForumEventHandler.class)
    public TMForumEventHandler eventHandler() {
        TMForumEventHandler eventHandler = mock(TMForumEventHandler.class);
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());
        when(eventHandler.handleUpdateEvent(any(), any())).thenReturn(Mono.empty());
        return eventHandler;
    }

    @Override
    public void createDocumentSpecification201() throws Exception {
        // Test basic creation
        DocumentSpecificationCreateVO createVO = new DocumentSpecificationCreateVO();
        createVO.setName("Test Document Specification");
        createVO.setDescription("A test document specification");
        createVO.setVersion("1.0.0");

        HttpResponse<DocumentSpecificationVO> response = callAndCatch(
                () -> documentSpecificationApiTestClient.createDocumentSpecification(null, createVO));

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertNotNull(response.body());
        assertNotNull(response.body().getId());
        assertEquals("Test Document Specification", response.body().getName());
    }

    @Test
    public void createDocumentSpecificationWithAttachment201() throws Exception {
        // Test creation with attachment
        DocumentSpecificationCreateVO createVO = new DocumentSpecificationCreateVO();
        createVO.setName("Document with Attachment");
        createVO.setVersion("1.0.0");

        AttachmentRefOrValueVO attachment = new AttachmentRefOrValueVO();
        attachment.setName("test-file.txt");
        attachment.setMimeType("text/plain");
        attachment.setContent(Base64.getEncoder().encodeToString("Hello World".getBytes()));
        createVO.setAttachment(List.of(attachment));

        HttpResponse<DocumentSpecificationVO> response = callAndCatch(
                () -> documentSpecificationApiTestClient.createDocumentSpecification(null, createVO));

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertNotNull(response.body());
        assertNotNull(response.body().getAttachment());
        assertFalse(response.body().getAttachment().isEmpty());
        // The attachment content should be S3 retrieval info, not the original content
        String content = response.body().getAttachment().get(0).getContent();
        assertTrue(content.startsWith("s3ref:"), "Content should be S3 retrieval info");
    }

    @Override
    public void createDocumentSpecification400() throws Exception {
        // Test creation with missing required field (name)
        DocumentSpecificationCreateVO createVO = new DocumentSpecificationCreateVO();
        createVO.setDescription("Missing name field");

        HttpResponse<DocumentSpecificationVO> response = callAndCatch(
                () -> documentSpecificationApiTestClient.createDocumentSpecification(null, createVO));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    }

    @Override
    @Disabled("Not implemented")
    public void createDocumentSpecification401() throws Exception {
        // Authentication not implemented
    }

    @Override
    @Disabled("Not implemented")
    public void createDocumentSpecification403() throws Exception {
        // Authorization not implemented
    }

    @Override
    @Disabled("Not implemented")
    public void createDocumentSpecification405() throws Exception {
        // Method not allowed - not applicable
    }

    @Override
    @Disabled("Not implemented")
    public void createDocumentSpecification409() throws Exception {
        // Conflict - not applicable for create
    }

    @Override
    @Disabled("Not implemented")
    public void createDocumentSpecification500() throws Exception {
        // Internal server error - hard to simulate
    }

    @Override
    public void deleteDocumentSpecification204() throws Exception {
        // First create a document specification
        DocumentSpecificationCreateVO createVO = new DocumentSpecificationCreateVO();
        createVO.setName("Document to Delete");
        createVO.setVersion("1.0.0");

        HttpResponse<DocumentSpecificationVO> createResponse = callAndCatch(
                () -> documentSpecificationApiTestClient.createDocumentSpecification(null, createVO));

        assertEquals(HttpStatus.CREATED, createResponse.getStatus());
        String id = createResponse.body().getId();

        // Then delete it
        HttpResponse<?> deleteResponse = callAndCatch(
                () -> documentSpecificationApiTestClient.deleteDocumentSpecification(null, id));

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatus());
    }

    @Override
    @Disabled("Not implemented")
    public void deleteDocumentSpecification400() throws Exception {
        // Bad request for delete - not applicable
    }

    @Override
    @Disabled("Not implemented")
    public void deleteDocumentSpecification401() throws Exception {
        // Authentication not implemented
    }

    @Override
    @Disabled("Not implemented")
    public void deleteDocumentSpecification403() throws Exception {
        // Authorization not implemented
    }

    @Override
    public void deleteDocumentSpecification404() throws Exception {
        HttpResponse<?> deleteResponse = callAndCatch(
                () -> documentSpecificationApiTestClient.deleteDocumentSpecification(null,
                        "urn:ngsi-ld:document-specification:nonexistent"));

        assertEquals(HttpStatus.NOT_FOUND, deleteResponse.getStatus());
    }

    @Override
    @Disabled("Not implemented")
    public void deleteDocumentSpecification405() throws Exception {
        // Method not allowed - not applicable
    }

    @Override
    @Disabled("Not implemented")
    public void deleteDocumentSpecification409() throws Exception {
        // Conflict - not applicable for delete
    }

    @Override
    @Disabled("Not implemented")
    public void deleteDocumentSpecification500() throws Exception {
        // Internal server error - hard to simulate
    }

    @Override
    public void listDocumentSpecification200() throws Exception {
        // Create a few document specifications first
        for (int i = 0; i < 3; i++) {
            DocumentSpecificationCreateVO createVO = new DocumentSpecificationCreateVO();
            createVO.setName("Document " + i);
            createVO.setVersion("1.0.0");

            callAndCatch(() -> documentSpecificationApiTestClient.createDocumentSpecification(null, createVO));
        }

        HttpResponse<List<DocumentSpecificationVO>> listResponse = callAndCatch(
                () -> documentSpecificationApiTestClient.listDocumentSpecification(null, null, null, null));

        assertEquals(HttpStatus.OK, listResponse.getStatus());
        assertNotNull(listResponse.body());
        assertTrue(listResponse.body().size() >= 3);
    }

    @Override
    @Disabled("Not implemented")
    public void listDocumentSpecification400() throws Exception {
        // Bad request for list - not applicable
    }

    @Override
    @Disabled("Not implemented")
    public void listDocumentSpecification401() throws Exception {
        // Authentication not implemented
    }

    @Override
    @Disabled("Not implemented")
    public void listDocumentSpecification403() throws Exception {
        // Authorization not implemented
    }

    @Override
    @Disabled("Not implemented")
    public void listDocumentSpecification404() throws Exception {
        // Not found for list - not applicable
    }

    @Override
    @Disabled("Not implemented")
    public void listDocumentSpecification405() throws Exception {
        // Method not allowed - not applicable
    }

    @Override
    @Disabled("Not implemented")
    public void listDocumentSpecification409() throws Exception {
        // Conflict - not applicable for list
    }

    @Override
    @Disabled("Not implemented")
    public void listDocumentSpecification500() throws Exception {
        // Internal server error - hard to simulate
    }

    @Override
    @Disabled("PATCH not implemented per requirements")
    public void patchDocumentSpecification200() throws Exception {
        // PATCH is not implemented per requirements
    }

    @Override
    @Disabled("PATCH not implemented per requirements")
    public void patchDocumentSpecification400() throws Exception {
        // PATCH is not implemented per requirements
    }

    @Override
    @Disabled("PATCH not implemented per requirements")
    public void patchDocumentSpecification401() throws Exception {
        // PATCH is not implemented per requirements
    }

    @Override
    @Disabled("PATCH not implemented per requirements")
    public void patchDocumentSpecification403() throws Exception {
        // PATCH is not implemented per requirements
    }

    @Override
    @Disabled("PATCH not implemented per requirements")
    public void patchDocumentSpecification404() throws Exception {
        // PATCH is not implemented per requirements
    }

    @Override
    @Disabled("PATCH not implemented per requirements")
    public void patchDocumentSpecification405() throws Exception {
        // PATCH is not implemented per requirements
    }

    @Override
    @Disabled("PATCH not implemented per requirements")
    public void patchDocumentSpecification409() throws Exception {
        // PATCH is not implemented per requirements
    }

    @Override
    @Disabled("PATCH not implemented per requirements")
    public void patchDocumentSpecification500() throws Exception {
        // PATCH is not implemented per requirements
    }

    @Override
    public void retrieveDocumentSpecification200() throws Exception {
        // Create a document specification
        DocumentSpecificationCreateVO createVO = new DocumentSpecificationCreateVO();
        createVO.setName("Document to Retrieve");
        createVO.setVersion("1.0.0");

        HttpResponse<DocumentSpecificationVO> createResponse = callAndCatch(
                () -> documentSpecificationApiTestClient.createDocumentSpecification(null, createVO));

        assertEquals(HttpStatus.CREATED, createResponse.getStatus());
        String id = createResponse.body().getId();

        // Retrieve it
        HttpResponse<DocumentSpecificationVO> retrieveResponse = callAndCatch(
                () -> documentSpecificationApiTestClient.retrieveDocumentSpecification(null, id, null));

        assertEquals(HttpStatus.OK, retrieveResponse.getStatus());
        assertNotNull(retrieveResponse.body());
        assertEquals(id, retrieveResponse.body().getId());
        assertEquals("Document to Retrieve", retrieveResponse.body().getName());
    }

    @Test
    public void retrieveDocumentSpecificationWithHydratedAttachment() throws Exception {
        // Create a document specification with attachment
        DocumentSpecificationCreateVO createVO = new DocumentSpecificationCreateVO();
        createVO.setName("Document with Hydrated Attachment");
        createVO.setVersion("1.0.0");

        String originalContent = "Hello World from S3!";
        AttachmentRefOrValueVO attachment = new AttachmentRefOrValueVO();
        attachment.setName("test-file.txt");
        attachment.setMimeType("text/plain");
        attachment.setContent(Base64.getEncoder().encodeToString(originalContent.getBytes()));
        createVO.setAttachment(List.of(attachment));

        HttpResponse<DocumentSpecificationVO> createResponse = callAndCatch(
                () -> documentSpecificationApiTestClient.createDocumentSpecification(null, createVO));

        assertEquals(HttpStatus.CREATED, createResponse.getStatus());
        String id = createResponse.body().getId();

        // Retrieve it - the attachment should be hydrated
        HttpResponse<DocumentSpecificationVO> retrieveResponse = callAndCatch(
                () -> documentSpecificationApiTestClient.retrieveDocumentSpecification(null, id, null));

        assertEquals(HttpStatus.OK, retrieveResponse.getStatus());
        assertNotNull(retrieveResponse.body().getAttachment());
        assertFalse(retrieveResponse.body().getAttachment().isEmpty());

        // The content should be the original content (hydrated from S3)
        String retrievedContent = retrieveResponse.body().getAttachment().get(0).getContent();
        String decodedContent = new String(Base64.getDecoder().decode(retrievedContent));
        assertEquals(originalContent, decodedContent);
    }

    @Override
    @Disabled("Not implemented")
    public void retrieveDocumentSpecification400() throws Exception {
        // Bad request for retrieve - not applicable
    }

    @Override
    @Disabled("Not implemented")
    public void retrieveDocumentSpecification401() throws Exception {
        // Authentication not implemented
    }

    @Override
    @Disabled("Not implemented")
    public void retrieveDocumentSpecification403() throws Exception {
        // Authorization not implemented
    }

    @Override
    public void retrieveDocumentSpecification404() throws Exception {
        HttpResponse<DocumentSpecificationVO> retrieveResponse = callAndCatch(
                () -> documentSpecificationApiTestClient.retrieveDocumentSpecification(null,
                        "urn:ngsi-ld:document-specification:nonexistent", null));

        assertEquals(HttpStatus.NOT_FOUND, retrieveResponse.getStatus());
    }

    @Override
    @Disabled("Not implemented")
    public void retrieveDocumentSpecification405() throws Exception {
        // Method not allowed - not applicable
    }

    @Override
    @Disabled("Not implemented")
    public void retrieveDocumentSpecification409() throws Exception {
        // Conflict - not applicable for retrieve
    }

    @Override
    @Disabled("Not implemented")
    public void retrieveDocumentSpecification500() throws Exception {
        // Internal server error - hard to simulate
    }
}
