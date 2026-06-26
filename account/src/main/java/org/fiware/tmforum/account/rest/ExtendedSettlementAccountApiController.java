package org.fiware.tmforum.account.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.account.api.ext.SettlementAccountExtensionApi;
import org.fiware.account.model.SettlementAccountCreateVO;
import org.fiware.account.model.SettlementAccountVO;
import org.fiware.tmforum.account.TMForumMapper;
import org.fiware.tmforum.account.domain.SettlementAccount;
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
public class ExtendedSettlementAccountApiController extends AbstractApiController<SettlementAccount>
        implements SettlementAccountExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final SettlementAccountApiController settlementAccountApiController;

    public ExtendedSettlementAccountApiController(QueryParser queryParser, ReferenceValidationService validationService,
                                                  TmForumRepository repository, TMForumEventHandler eventHandler,
                                                  TMForumMapper tmForumMapper,
                                                  SettlementAccountApiController settlementAccountApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.settlementAccountApiController = settlementAccountApiController;
    }

    @Override
    public Mono<HttpResponse<SettlementAccountVO>> createSettlementAccountWithId(String id, SettlementAccountCreateVO createVO) {
        if (!IdHelper.isNgsiLdId(id)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI.", id),
                    TmForumExceptionReason.INVALID_DATA);
        }
        SettlementAccount settlementAccount = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(settlementAccountApiController.getCheckingMono(settlementAccount), SettlementAccount.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
