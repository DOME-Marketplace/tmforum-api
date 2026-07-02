package org.fiware.tmforum.party.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.party.api.ext.OrganizationExtensionApi;
import org.fiware.party.model.OrganizationCreateVO;
import org.fiware.party.model.OrganizationVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.party.TMForumMapper;
import org.fiware.tmforum.party.domain.organization.Organization;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Controller("${api.party-catalog.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedOrganizationApiController extends AbstractApiController<Organization>
        implements OrganizationExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final OrganizationApiController organizationApiController;

    public ExtendedOrganizationApiController(QueryParser queryParser, ReferenceValidationService validationService,
                                             TmForumRepository repository, TMForumEventHandler eventHandler,
                                             TMForumMapper tmForumMapper,
                                             OrganizationApiController organizationApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.organizationApiController = organizationApiController;
    }

    @Override
    public Mono<HttpResponse<OrganizationVO>> createOrganizationWithId(String id, OrganizationCreateVO createVO) {
        if (!IdHelper.isNgsiLdId(id, Organization.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, Organization.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        Organization organization = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(organizationApiController.getCheckingMono(organization), Organization.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
