package org.fiware.tmforum.resourcefunction.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.resourcefunction.api.ext.ResourceFunctionExtensionApi;
import org.fiware.resourcefunction.model.ResourceFunctionCreateVO;
import org.fiware.resourcefunction.model.ResourceFunctionVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.resourcefunction.TMForumMapper;
import org.fiware.tmforum.resourcefunction.domain.ResourceFunction;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Controller("${api.resource-function-activation.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedResourceFunctionApiController extends AbstractApiController<ResourceFunction>
        implements ResourceFunctionExtensionApi {

    @Value("${apiExtension.putEnabled:false}")
    private boolean putEnabled;

    private final TMForumMapper tmForumMapper;
    private final ResourceFunctionApiController resourceFunctionApiController;

    public ExtendedResourceFunctionApiController(
            QueryParser queryParser,
            ReferenceValidationService validationService,
            TmForumRepository repository,
            TMForumEventHandler eventHandler,
            TMForumMapper tmForumMapper,
            ResourceFunctionApiController resourceFunctionApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.resourceFunctionApiController = resourceFunctionApiController;
    }

    @Override
    public Mono<HttpResponse<ResourceFunctionVO>> createResourceFunctionWithId(String id, ResourceFunctionCreateVO createVO) {
        if (!putEnabled) {
            return Mono.just(HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED));
        }
        if (!IdHelper.isNgsiLdId(id, ResourceFunction.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, ResourceFunction.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        ResourceFunction resourceFunction = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(resourceFunctionApiController.getCheckingMono(resourceFunction), ResourceFunction.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
