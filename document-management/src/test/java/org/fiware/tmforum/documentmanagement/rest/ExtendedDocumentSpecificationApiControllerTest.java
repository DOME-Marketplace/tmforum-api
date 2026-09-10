package org.fiware.tmforum.documentmanagement.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.document.model.DocumentSpecificationCreateVO;
import org.fiware.document.model.DocumentSpecificationVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.documentmanagement.TMForumMapper;
import org.fiware.tmforum.documentmanagement.domain.DocumentSpecification;
import org.fiware.tmforum.documentmanagement.s3.S3AttachmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

class ExtendedDocumentSpecificationApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:document-specification:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private Clock clock;
    @Mock private S3AttachmentService s3AttachmentService;
    @Mock private DocumentSpecificationApiController documentSpecificationApiController;

    private ExtendedDocumentSpecificationApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedDocumentSpecificationApiController(queryParser, validationService, repository,
                eventHandler, tmForumMapper, clock, s3AttachmentService, documentSpecificationApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedDocumentSpecificationApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<DocumentSpecificationVO> response = controller
                .createDocumentSpecificationWithId(VALID_ID, new DocumentSpecificationCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        DocumentSpecificationCreateVO createVO = new DocumentSpecificationCreateVO();
        createVO.setName("Test Document Specification");

        DocumentSpecification mappedDocSpec = new DocumentSpecification(VALID_ID);
        DocumentSpecificationVO intermediateVO = new DocumentSpecificationVO();
        DocumentSpecificationVO responseVO = new DocumentSpecificationVO();

        when(clock.instant()).thenReturn(Instant.now());
        when(tmForumMapper.map(any(DocumentSpecificationCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(DocumentSpecificationVO.class))).thenReturn(mappedDocSpec);
        when(tmForumMapper.map(any(DocumentSpecification.class))).thenReturn(responseVO);
        when(s3AttachmentService.offloadAttachments(any(), any())).thenReturn(Mono.empty());
        when(documentSpecificationApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedDocSpec));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<DocumentSpecificationVO> response = controller
                .createDocumentSpecificationWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
