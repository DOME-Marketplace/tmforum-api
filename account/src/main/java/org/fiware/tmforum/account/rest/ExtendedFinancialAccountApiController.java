package org.fiware.tmforum.account.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.account.api.ext.FinancialAccountExtensionApi;
import org.fiware.account.model.FinancialAccountCreateVO;
import org.fiware.account.model.FinancialAccountVO;
import org.fiware.tmforum.account.TMForumMapper;
import org.fiware.tmforum.account.domain.FinancialAccount;
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
public class ExtendedFinancialAccountApiController extends AbstractApiController<FinancialAccount>
        implements FinancialAccountExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final FinancialAccountApiController financialAccountApiController;

    @Value("${apiExtension.putEnabled:false}")
    private boolean putEnabled;

    public ExtendedFinancialAccountApiController(QueryParser queryParser, ReferenceValidationService validationService,
                                                 TmForumRepository repository, TMForumEventHandler eventHandler,
                                                 TMForumMapper tmForumMapper,
                                                 FinancialAccountApiController financialAccountApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.financialAccountApiController = financialAccountApiController;
    }

    @Override
    public Mono<HttpResponse<FinancialAccountVO>> createFinancialAccountWithId(String id, FinancialAccountCreateVO createVO) {
        if (!putEnabled) {
            return Mono.just(HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED));
        }
        if (!IdHelper.isNgsiLdId(id, FinancialAccount.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, FinancialAccount.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        FinancialAccount financialAccount = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(financialAccountApiController.getCheckingMono(financialAccount), FinancialAccount.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
