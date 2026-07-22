package org.fiware.tmforum.party.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.party.model.OrganizationCreateVO;
import org.fiware.party.model.OrganizationVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.party.TMForumMapper;
import org.fiware.tmforum.party.domain.organization.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

class ExtendedOrganizationApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:organization:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private OrganizationApiController organizationApiController;

    private ExtendedOrganizationApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedOrganizationApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, organizationApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedOrganizationApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createOrganizationWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<OrganizationVO> response = controller.createOrganizationWithId(VALID_ID, new OrganizationCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createOrganizationWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        Organization mappedOrganization = new Organization(Organization.TYPE_ORGANIZATION);
        mappedOrganization.setId(URI.create(VALID_ID));
        OrganizationVO intermediateVO = new OrganizationVO();
        OrganizationVO responseVO = new OrganizationVO();

        OrganizationCreateVO createVO = new OrganizationCreateVO();
        when(tmForumMapper.map(any(OrganizationCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(OrganizationVO.class))).thenReturn(mappedOrganization);
        when(tmForumMapper.map(any(Organization.class))).thenReturn(responseVO);
        when(organizationApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedOrganization));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<OrganizationVO> response = controller.createOrganizationWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
