package org.fiware.tmforum.agreement.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.agreement.api.ext.AgreementExtensionApi;
import org.fiware.agreement.model.AgreementCreateVO;
import org.fiware.agreement.model.AgreementVO;
import org.fiware.tmforum.agreement.TMForumMapper;
import org.fiware.tmforum.agreement.domain.Agreement;
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
@Controller("${api.agreement.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedAgreementApiController extends AbstractApiController<Agreement>
        implements AgreementExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final AgreementController agreementController;

    @Value("${apiExtension.putEnabled:false}")
    private boolean putEnabled;

    public ExtendedAgreementApiController(QueryParser queryParser, ReferenceValidationService validationService,
                                          TmForumRepository repository, TMForumEventHandler eventHandler,
                                          TMForumMapper tmForumMapper,
                                          AgreementController agreementController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.agreementController = agreementController;
    }

    @Override
    public Mono<HttpResponse<AgreementVO>> createAgreementWithId(String id, AgreementCreateVO createVO) {
        if (!putEnabled) {
            return Mono.just(HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED));
        }
        if (!IdHelper.isNgsiLdId(id, Agreement.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, Agreement.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        Agreement agreement = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(agreementController.getCheckingMono(agreement), Agreement.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
