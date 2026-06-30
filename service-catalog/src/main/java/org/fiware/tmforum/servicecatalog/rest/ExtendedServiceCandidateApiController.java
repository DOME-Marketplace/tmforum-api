package org.fiware.tmforum.servicecatalog.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.servicecatalog.api.ext.ServiceCandidateExtensionApi;
import org.fiware.servicecatalog.model.ServiceCandidateCreateVO;
import org.fiware.servicecatalog.model.ServiceCandidateVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.service.ServiceCandidate;
import org.fiware.tmforum.servicecatalog.TMForumMapper;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Clock;

@Slf4j
@Controller("${api.service-catalog.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedServiceCandidateApiController extends AbstractApiController<ServiceCandidate>
        implements ServiceCandidateExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final Clock clock;
    private final ServiceCandidateApiController serviceCandidateApiController;

    public ExtendedServiceCandidateApiController(
            QueryParser queryParser,
            ReferenceValidationService validationService,
            TmForumRepository repository,
            TMForumEventHandler eventHandler,
            TMForumMapper tmForumMapper,
            Clock clock,
            ServiceCandidateApiController serviceCandidateApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.clock = clock;
        this.serviceCandidateApiController = serviceCandidateApiController;
    }

    @Override
    public Mono<HttpResponse<ServiceCandidateVO>> createServiceCandidateWithId(String id, ServiceCandidateCreateVO createVO) {
        if (!IdHelper.isNgsiLdId(id)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI.", id),
                    TmForumExceptionReason.INVALID_DATA);
        }
        ServiceCandidate serviceCandidate = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        serviceCandidate.setLastUpdate(clock.instant());
        return create(serviceCandidateApiController.getCheckingMono(serviceCandidate), ServiceCandidate.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
