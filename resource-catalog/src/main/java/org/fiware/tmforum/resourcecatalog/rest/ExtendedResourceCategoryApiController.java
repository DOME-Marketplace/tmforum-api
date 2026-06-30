package org.fiware.tmforum.resourcecatalog.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.resourcecatalog.api.ext.ResourceCategoryExtensionApi;
import org.fiware.resourcecatalog.model.ResourceCategoryCreateVO;
import org.fiware.resourcecatalog.model.ResourceCategoryVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.resource.ResourceCategory;
import org.fiware.tmforum.resourcecatalog.TMForumMapper;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Clock;

@Slf4j
@Controller("${api.resource-catalog.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedResourceCategoryApiController extends AbstractApiController<ResourceCategory>
        implements ResourceCategoryExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final Clock clock;
    private final ResourceCategoryApiController resourceCategoryApiController;

    public ExtendedResourceCategoryApiController(QueryParser queryParser, ReferenceValidationService validationService,
                                                 TmForumRepository repository, TMForumEventHandler eventHandler,
                                                 TMForumMapper tmForumMapper, Clock clock,
                                                 ResourceCategoryApiController resourceCategoryApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.clock = clock;
        this.resourceCategoryApiController = resourceCategoryApiController;
    }

    @Override
    public Mono<HttpResponse<ResourceCategoryVO>> createResourceCategoryWithId(String id, ResourceCategoryCreateVO createVO) {
        if (!IdHelper.isNgsiLdId(id)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI.", id),
                    TmForumExceptionReason.INVALID_DATA);
        }
        ResourceCategory resourceCategory = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        resourceCategory.setLastUpdate(clock.instant());
        return create(resourceCategoryApiController.getCheckingMono(resourceCategory), ResourceCategory.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
