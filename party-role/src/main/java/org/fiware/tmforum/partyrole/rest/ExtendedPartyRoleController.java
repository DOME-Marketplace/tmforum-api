package org.fiware.tmforum.partyrole.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.partyRole.api.ext.PartyRoleExtensionApi;
import org.fiware.partyRole.model.PartyRoleCreateVO;
import org.fiware.partyRole.model.PartyRoleVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.partyrole.TMForumMapper;
import org.fiware.tmforum.partyrole.domain.PartyRole;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Controller("${api.party-role.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedPartyRoleController extends AbstractApiController<PartyRole>
        implements PartyRoleExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final PartyRoleController partyRoleController;

    public ExtendedPartyRoleController(QueryParser queryParser, ReferenceValidationService validationService,
                                       TmForumRepository repository, TMForumEventHandler eventHandler,
                                       TMForumMapper tmForumMapper,
                                       PartyRoleController partyRoleController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.partyRoleController = partyRoleController;
    }

    @Override
    public Mono<HttpResponse<PartyRoleVO>> createPartyRoleWithId(String id, PartyRoleCreateVO createVO) {
        if (!IdHelper.isNgsiLdId(id, PartyRole.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, PartyRole.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        PartyRole partyRole = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(partyRoleController.getCheckingMono(partyRole), PartyRole.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
