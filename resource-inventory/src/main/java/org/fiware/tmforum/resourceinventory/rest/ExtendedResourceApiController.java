package org.fiware.tmforum.resourceinventory.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.resourceinventory.api.ext.ResourceExtensionApi;
import org.fiware.resourceinventory.model.ResourceCreateVO;
import org.fiware.resourceinventory.model.ResourceVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.resource.Resource;
import org.fiware.tmforum.resourceinventory.TMForumMapper;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Controller("${api.resource-inventory.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedResourceApiController extends AbstractApiController<Resource>
        implements ResourceExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final ResourceApiController resourceApiController;

    public ExtendedResourceApiController(
            QueryParser queryParser,
            ReferenceValidationService validationService,
            TmForumRepository repository,
            TMForumEventHandler eventHandler,
            TMForumMapper tmForumMapper,
            ResourceApiController resourceApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.resourceApiController = resourceApiController;
    }

    @Override
    public Mono<HttpResponse<ResourceVO>> createResourceWithId(String id, ResourceCreateVO createVO) {
        if (!IdHelper.isNgsiLdId(id, Resource.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, Resource.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        Resource resource = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        resourceApiController.validateInternalRefs(resource);
        return create(resourceApiController.getCheckingMono(resource), Resource.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
