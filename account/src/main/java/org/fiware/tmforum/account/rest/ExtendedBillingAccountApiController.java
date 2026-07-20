package org.fiware.tmforum.account.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.account.api.ext.BillingAccountExtensionApi;
import org.fiware.account.model.BillingAccountCreateVO;
import org.fiware.account.model.BillingAccountVO;
import org.fiware.tmforum.account.TMForumMapper;
import org.fiware.tmforum.account.domain.BillingAccount;
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
public class ExtendedBillingAccountApiController extends AbstractApiController<BillingAccount>
        implements BillingAccountExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final BillingAccountApiController billingAccountApiController;

    @Value("${apiExtension.putEnabled:false}")
    private boolean putEnabled;

    public ExtendedBillingAccountApiController(QueryParser queryParser, ReferenceValidationService validationService,
                                               TmForumRepository repository, TMForumEventHandler eventHandler,
                                               TMForumMapper tmForumMapper,
                                               BillingAccountApiController billingAccountApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.billingAccountApiController = billingAccountApiController;
    }

    @Override
    public Mono<HttpResponse<BillingAccountVO>> createBillingAccountWithId(String id, BillingAccountCreateVO createVO) {
        if (!putEnabled) {
            return Mono.just(HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED));
        }
        if (!IdHelper.isNgsiLdId(id, BillingAccount.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, BillingAccount.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        BillingAccount billingAccount = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(billingAccountApiController.getCheckingMono(billingAccount), BillingAccount.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
