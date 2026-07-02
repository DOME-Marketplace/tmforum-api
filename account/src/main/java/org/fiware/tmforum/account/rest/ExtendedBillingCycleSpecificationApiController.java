package org.fiware.tmforum.account.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.account.api.ext.BillingCycleSpecificationExtensionApi;
import org.fiware.account.model.BillingCycleSpecificationCreateVO;
import org.fiware.account.model.BillingCycleSpecificationVO;
import org.fiware.tmforum.account.TMForumMapper;
import org.fiware.tmforum.account.domain.BillingCycleSpecification;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Controller("${api.account.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedBillingCycleSpecificationApiController extends AbstractApiController<BillingCycleSpecification>
        implements BillingCycleSpecificationExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final BillingCycleSpecificationApiController billingCycleSpecificationApiController;

    public ExtendedBillingCycleSpecificationApiController(QueryParser queryParser, ReferenceValidationService validationService,
                                                          TmForumRepository repository, TMForumEventHandler eventHandler,
                                                          TMForumMapper tmForumMapper,
                                                          BillingCycleSpecificationApiController billingCycleSpecificationApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.billingCycleSpecificationApiController = billingCycleSpecificationApiController;
    }

    @Override
    public Mono<HttpResponse<BillingCycleSpecificationVO>> createBillingCycleSpecificationWithId(String id, BillingCycleSpecificationCreateVO createVO) {
        if (!IdHelper.isNgsiLdId(id, BillingCycleSpecification.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, BillingCycleSpecification.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        BillingCycleSpecification billingCycleSpecification = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(billingCycleSpecificationApiController.getCheckingMono(billingCycleSpecification), BillingCycleSpecification.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
