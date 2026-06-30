package org.fiware.tmforum.servicecatalog.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.servicecatalog.api.ext.ServiceCategoryExtensionApi;
import org.fiware.servicecatalog.model.ServiceCategoryCreateVO;
import org.fiware.servicecatalog.model.ServiceCategoryVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.service.ServiceCategory;
import org.fiware.tmforum.servicecatalog.TMForumMapper;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Clock;

@Slf4j
@Controller("${api.service-catalog.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedServiceCategoryApiController extends AbstractApiController<ServiceCategory>
        implements ServiceCategoryExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final Clock clock;
    private final ServiceCategoryApiController serviceCategoryApiController;

    public ExtendedServiceCategoryApiController(
            QueryParser queryParser,
            ReferenceValidationService validationService,
            TmForumRepository repository,
            TMForumEventHandler eventHandler,
            TMForumMapper tmForumMapper,
            Clock clock,
            ServiceCategoryApiController serviceCategoryApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.clock = clock;
        this.serviceCategoryApiController = serviceCategoryApiController;
    }

    @Override
    public Mono<HttpResponse<ServiceCategoryVO>> createServiceCategoryWithId(String id, ServiceCategoryCreateVO createVO) {
        if (!IdHelper.isNgsiLdId(id)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI.", id),
                    TmForumExceptionReason.INVALID_DATA);
        }
        ServiceCategory serviceCategory = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        serviceCategory.setLastUpdate(clock.instant());
        return create(serviceCategoryApiController.getCheckingMono(serviceCategory), ServiceCategory.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
