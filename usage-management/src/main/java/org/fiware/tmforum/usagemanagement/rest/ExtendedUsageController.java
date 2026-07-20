package org.fiware.tmforum.usagemanagement.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.usagemanagement.TMForumMapper;
import org.fiware.tmforum.usagemanagement.domain.Usage;
import org.fiware.usagemanagement.api.ext.UsageExtensionApi;
import org.fiware.usagemanagement.model.UsageCreateVO;
import org.fiware.usagemanagement.model.UsageVO;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Controller("${api.usage-management.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedUsageController extends AbstractApiController<Usage>
        implements UsageExtensionApi {

    @Value("${apiExtension.putEnabled:false}")
    private boolean putEnabled;

    private final TMForumMapper tmForumMapper;
    private final UsageController usageController;

    public ExtendedUsageController(
            QueryParser queryParser,
            ReferenceValidationService validationService,
            TmForumRepository repository,
            TMForumEventHandler eventHandler,
            TMForumMapper tmForumMapper,
            UsageController usageController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.usageController = usageController;
    }

    @Override
    public Mono<HttpResponse<UsageVO>> createUsageWithId(String id, UsageCreateVO createVO) {
        if (!putEnabled) {
            return Mono.just(HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED));
        }
        if (!IdHelper.isNgsiLdId(id, Usage.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, Usage.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        Usage usage = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(usageController.getCheckingMono(usage), Usage.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
