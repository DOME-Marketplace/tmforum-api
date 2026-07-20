package org.fiware.tmforum.party.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.party.api.ext.IndividualExtensionApi;
import org.fiware.party.model.IndividualCreateVO;
import org.fiware.party.model.IndividualVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.party.TMForumMapper;
import org.fiware.tmforum.party.domain.individual.Individual;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Controller("${api.party-catalog.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedIndividualApiController extends AbstractApiController<Individual>
        implements IndividualExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final IndividualApiController individualApiController;

    @Value("${apiExtension.putEnabled:false}")
    private boolean putEnabled;

    public ExtendedIndividualApiController(QueryParser queryParser, ReferenceValidationService validationService,
                                           TmForumRepository repository, TMForumEventHandler eventHandler,
                                           TMForumMapper tmForumMapper,
                                           IndividualApiController individualApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.individualApiController = individualApiController;
    }

    @Override
    public Mono<HttpResponse<IndividualVO>> createIndividualWithId(String id, IndividualCreateVO createVO) {
        if (!putEnabled) {
            return Mono.just(HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED));
        }
        if (!IdHelper.isNgsiLdId(id, Individual.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, Individual.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        Individual individual = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(individualApiController.getCheckingMono(individual), Individual.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
