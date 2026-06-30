package org.fiware.tmforum.servicecatalog.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.servicecatalog.api.ext.ServiceSpecificationExtensionApi;
import org.fiware.servicecatalog.model.ServiceSpecificationCreateVO;
import org.fiware.servicecatalog.model.ServiceSpecificationVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.servicecatalog.TMForumMapper;
import org.fiware.tmforum.servicecatalog.domain.ServiceSpecification;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Clock;

@Slf4j
@Controller("${api.service-catalog.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedServiceSpecificationApiController extends AbstractApiController<ServiceSpecification>
        implements ServiceSpecificationExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final Clock clock;
    private final ServiceSpecificationApiController serviceSpecificationApiController;

    public ExtendedServiceSpecificationApiController(
            QueryParser queryParser,
            ReferenceValidationService validationService,
            TmForumRepository repository,
            TMForumEventHandler eventHandler,
            TMForumMapper tmForumMapper,
            Clock clock,
            ServiceSpecificationApiController serviceSpecificationApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.clock = clock;
        this.serviceSpecificationApiController = serviceSpecificationApiController;
    }

    @Override
    public Mono<HttpResponse<ServiceSpecificationVO>> createServiceSpecificationWithId(String id, ServiceSpecificationCreateVO createVO) {
        if (!IdHelper.isNgsiLdId(id)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI.", id),
                    TmForumExceptionReason.INVALID_DATA);
        }
        if (createVO.getIsBundle() == null) {
            createVO.isBundle(false);
        }
        if (createVO.getLifecycleStatus() == null) {
            createVO.lifecycleStatus("created");
        }
        ServiceSpecification serviceSpecification = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        serviceSpecification.setLastUpdate(clock.instant());
        return create(serviceSpecificationApiController.validateSpec(serviceSpecification), ServiceSpecification.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
