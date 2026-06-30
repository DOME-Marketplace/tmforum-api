package org.fiware.tmforum.resourcecatalog.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.resourcecatalog.api.ext.ResourceSpecificationExtensionApi;
import org.fiware.resourcecatalog.model.ResourceSpecificationCreateVO;
import org.fiware.resourcecatalog.model.ResourceSpecificationVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.resource.ResourceSpecification;
import org.fiware.tmforum.resourcecatalog.TMForumMapper;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Clock;

@Slf4j
@Controller("${api.resource-catalog.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedResourceSpecificationApiController extends AbstractApiController<ResourceSpecification>
        implements ResourceSpecificationExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final Clock clock;
    private final ResourceSpecifcationApiController resourceSpecifcationApiController;

    public ExtendedResourceSpecificationApiController(QueryParser queryParser, ReferenceValidationService validationService,
                                                      TmForumRepository repository, TMForumEventHandler eventHandler,
                                                      TMForumMapper tmForumMapper, Clock clock,
                                                      ResourceSpecifcationApiController resourceSpecifcationApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.clock = clock;
        this.resourceSpecifcationApiController = resourceSpecifcationApiController;
    }

    @Override
    public Mono<HttpResponse<ResourceSpecificationVO>> createResourceSpecificationWithId(String id, ResourceSpecificationCreateVO createVO) {
        if (!IdHelper.isNgsiLdId(id)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI.", id),
                    TmForumExceptionReason.INVALID_DATA);
        }
        if (createVO.getName() == null) {
            throw new TmForumException(
                    String.format("The specification create does not contain all mandatory values: %s.", createVO),
                    TmForumExceptionReason.INVALID_DATA);
        }
        if (createVO.getIsBundle() == null) {
            createVO.isBundle(false);
        }
        if (createVO.getLifecycleStatus() == null) {
            createVO.lifecycleStatus("created");
        }
        ResourceSpecification resourceSpecification = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        resourceSpecification.setLastUpdate(clock.instant());
        Mono<ResourceSpecification> checkingMono = resourceSpecifcationApiController.getCheckingMono(resourceSpecification);
        checkingMono = Mono.zip(checkingMono, resourceSpecifcationApiController.validateSpec(resourceSpecification),
                (p1, p2) -> resourceSpecification);
        return create(checkingMono, ResourceSpecification.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
