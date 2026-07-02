package org.fiware.tmforum.documentmanagement.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.document.api.ext.DocumentSpecificationExtensionApi;
import org.fiware.document.model.DocumentSpecificationCreateVO;
import org.fiware.document.model.DocumentSpecificationVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.documentmanagement.TMForumMapper;
import org.fiware.tmforum.documentmanagement.domain.DocumentSpecification;
import org.fiware.tmforum.documentmanagement.s3.S3AttachmentService;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Clock;

@Slf4j
@Controller("${general.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedDocumentSpecificationApiController extends AbstractApiController<DocumentSpecification>
        implements DocumentSpecificationExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final Clock clock;
    private final S3AttachmentService s3AttachmentService;
    private final DocumentSpecificationApiController documentSpecificationApiController;

    public ExtendedDocumentSpecificationApiController(QueryParser queryParser,
                                                      ReferenceValidationService validationService,
                                                      TmForumRepository repository,
                                                      TMForumEventHandler eventHandler,
                                                      TMForumMapper tmForumMapper,
                                                      Clock clock,
                                                      S3AttachmentService s3AttachmentService,
                                                      DocumentSpecificationApiController documentSpecificationApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.clock = clock;
        this.s3AttachmentService = s3AttachmentService;
        this.documentSpecificationApiController = documentSpecificationApiController;
    }

    @Override
    public Mono<HttpResponse<DocumentSpecificationVO>> createDocumentSpecificationWithId(
            String id, DocumentSpecificationCreateVO createVO) {
        if (!IdHelper.isNgsiLdId(id, DocumentSpecification.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, DocumentSpecification.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        if (createVO.getName() == null || createVO.getName().trim().isEmpty()) {
            throw new TmForumException(
                    "Name field is required and must not be blank to create a document specification.",
                    TmForumExceptionReason.INVALID_DATA);
        }
        DocumentSpecification docSpec = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        docSpec.setLastUpdate(clock.instant());
        docSpec.setAttachment(
                s3AttachmentService.offloadAttachments(docSpec.getAttachment(), docSpec.getId().toString()));
        return create(documentSpecificationApiController.getCheckingMono(docSpec), DocumentSpecification.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
