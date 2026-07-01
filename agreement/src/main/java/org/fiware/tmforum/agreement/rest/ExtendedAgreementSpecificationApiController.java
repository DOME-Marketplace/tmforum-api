package org.fiware.tmforum.agreement.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.agreement.api.ext.AgreementSpecificationExtensionApi;
import org.fiware.agreement.model.AgreementSpecificationCreateVO;
import org.fiware.agreement.model.AgreementSpecificationVO;
import org.fiware.tmforum.agreement.TMForumMapper;
import org.fiware.tmforum.agreement.domain.AgreementSpecification;
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
public class ExtendedAgreementSpecificationApiController extends AbstractApiController<AgreementSpecification>
        implements AgreementSpecificationExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final AgreementSpecController agreementSpecController;

    public ExtendedAgreementSpecificationApiController(QueryParser queryParser, ReferenceValidationService validationService,
                                                       TmForumRepository repository, TMForumEventHandler eventHandler,
                                                       TMForumMapper tmForumMapper,
                                                       AgreementSpecController agreementSpecController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.agreementSpecController = agreementSpecController;
    }

    @Override
    public Mono<HttpResponse<AgreementSpecificationVO>> createAgreementSpecificationWithId(String id, AgreementSpecificationCreateVO createVO) {
        if (!IdHelper.isNgsiLdId(id)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI.", id),
                    TmForumExceptionReason.INVALID_DATA);
        }
        AgreementSpecification agreementSpecification = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(agreementSpecController.getCheckingMono(agreementSpecification), AgreementSpecification.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
