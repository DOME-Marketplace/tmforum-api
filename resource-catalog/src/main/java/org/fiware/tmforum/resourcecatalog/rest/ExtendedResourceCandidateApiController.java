package org.fiware.tmforum.resourcecatalog.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.resourcecatalog.api.ext.ResourceCandidateExtensionApi;
import org.fiware.resourcecatalog.model.ResourceCandidateCreateVO;
import org.fiware.resourcecatalog.model.ResourceCandidateVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.resource.ResourceCandidate;
import org.fiware.tmforum.resourcecatalog.TMForumMapper;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Clock;

@Slf4j
@Controller("${api.resource-catalog.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedResourceCandidateApiController extends AbstractApiController<ResourceCandidate>
        implements ResourceCandidateExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final Clock clock;
    private final ResourceCandidateApiController resourceCandidateApiController;

    @Value("${apiExtension.putEnabled:false}")
    private boolean putEnabled;

    public ExtendedResourceCandidateApiController(QueryParser queryParser, ReferenceValidationService validationService,
                                                  TmForumRepository repository, TMForumEventHandler eventHandler,
                                                  TMForumMapper tmForumMapper, Clock clock,
                                                  ResourceCandidateApiController resourceCandidateApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.clock = clock;
        this.resourceCandidateApiController = resourceCandidateApiController;
    }

    @Override
    public Mono<HttpResponse<ResourceCandidateVO>> createResourceCandidateWithId(String id, ResourceCandidateCreateVO createVO) {
        if (!putEnabled) {
            return Mono.just(HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED));
        }
        if (!IdHelper.isNgsiLdId(id, ResourceCandidate.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, ResourceCandidate.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        ResourceCandidate resourceCandidate = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        resourceCandidate.setLastUpdate(clock.instant());
        return create(resourceCandidateApiController.getCheckingMono(resourceCandidate), ResourceCandidate.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
