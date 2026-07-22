package org.fiware.tmforum.resourcefunction.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.resourcefunction.api.ext.MigrateExtensionApi;
import org.fiware.resourcefunction.model.MigrateCreateVO;
import org.fiware.resourcefunction.model.MigrateVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.resourcefunction.TMForumMapper;
import org.fiware.tmforum.resourcefunction.domain.Migrate;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Controller("${api.resource-function-activation.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedMigrateApiController extends AbstractApiController<Migrate>
        implements MigrateExtensionApi {

    @Value("${apiExtension.putEnabled:false}")
    private boolean putEnabled;

    @Value("${apiExtension.deleteEnabled:false}")
    private boolean deleteEnabled;

    private final TMForumMapper tmForumMapper;
    private final MigrateApiController migrateApiController;

    public ExtendedMigrateApiController(
            QueryParser queryParser,
            ReferenceValidationService validationService,
            TmForumRepository repository,
            TMForumEventHandler eventHandler,
            TMForumMapper tmForumMapper,
            MigrateApiController migrateApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.migrateApiController = migrateApiController;
    }

    @Override
    public Mono<HttpResponse<MigrateVO>> createMigrateWithId(String id, MigrateCreateVO createVO) {
        if (!putEnabled) {
            return Mono.just(HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED));
        }
        if (!IdHelper.isNgsiLdId(id, Migrate.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, Migrate.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        Migrate migrate = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(migrateApiController.getCheckingMono(migrate), Migrate.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }

    @Override
    public Mono<HttpResponse<Object>> deleteMigrate(String id) {
        if (!deleteEnabled) {
            return Mono.just(HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED));
        }
        return delete(id);
    }
}
