package org.fiware.tmforum.usagemanagement.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
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
import org.fiware.tmforum.usagemanagement.domain.UsageSpecification;
import org.fiware.usagemanagement.api.ext.UsageSpecificationExtensionApi;
import org.fiware.usagemanagement.model.UsageSpecificationCreateVO;
import org.fiware.usagemanagement.model.UsageSpecificationVO;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Controller("${api.usage-management.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedUsageSpecificationController extends AbstractApiController<UsageSpecification>
        implements UsageSpecificationExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final UsageSpecificationController usageSpecificationController;

    public ExtendedUsageSpecificationController(
            QueryParser queryParser,
            ReferenceValidationService validationService,
            TmForumRepository repository,
            TMForumEventHandler eventHandler,
            TMForumMapper tmForumMapper,
            UsageSpecificationController usageSpecificationController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.usageSpecificationController = usageSpecificationController;
    }

    @Override
    public Mono<HttpResponse<UsageSpecificationVO>> createUsageSpecificationWithId(String id, UsageSpecificationCreateVO createVO) {
        if (!IdHelper.isNgsiLdId(id)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI.", id),
                    TmForumExceptionReason.INVALID_DATA);
        }
        UsageSpecification usageSpecification = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(usageSpecificationController.getCheckingMono(usageSpecification), UsageSpecification.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
