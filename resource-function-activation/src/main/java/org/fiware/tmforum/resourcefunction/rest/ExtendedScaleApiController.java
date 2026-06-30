package org.fiware.tmforum.resourcefunction.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.resourcefunction.api.ext.ScaleExtensionApi;
import org.fiware.resourcefunction.model.ScaleCreateVO;
import org.fiware.resourcefunction.model.ScaleVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.resourcefunction.TMForumMapper;
import org.fiware.tmforum.resourcefunction.domain.Scale;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Controller("${api.resource-function-activation.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedScaleApiController extends AbstractApiController<Scale>
        implements ScaleExtensionApi {

    @Value("${apiExtension.deleteEnabled:false}")
    private boolean deleteEnabled;

    private final TMForumMapper tmForumMapper;
    private final ScaleApiController scaleApiController;

    public ExtendedScaleApiController(
            QueryParser queryParser,
            ReferenceValidationService validationService,
            TmForumRepository repository,
            TMForumEventHandler eventHandler,
            TMForumMapper tmForumMapper,
            ScaleApiController scaleApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.scaleApiController = scaleApiController;
    }

    @Override
    public Mono<HttpResponse<ScaleVO>> createScaleWithId(String id, ScaleCreateVO createVO) {
        if (!IdHelper.isNgsiLdId(id)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI.", id),
                    TmForumExceptionReason.INVALID_DATA);
        }
        Scale scale = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(scaleApiController.getCheckingMono(scale), Scale.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }

    @Override
    public Mono<HttpResponse<Object>> deleteScale(String id) {
        if (!deleteEnabled) {
            return Mono.just(HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED));
        }
        return delete(id);
    }
}
