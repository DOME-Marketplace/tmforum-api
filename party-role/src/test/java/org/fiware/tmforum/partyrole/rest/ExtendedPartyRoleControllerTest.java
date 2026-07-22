package org.fiware.tmforum.partyrole.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.partyRole.model.PartyRoleCreateVO;
import org.fiware.partyRole.model.PartyRoleVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.partyrole.TMForumMapper;
import org.fiware.tmforum.partyrole.domain.PartyRole;
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

class ExtendedPartyRoleControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:party-role:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private PartyRoleController partyRoleController;

    private ExtendedPartyRoleController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedPartyRoleController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, partyRoleController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedPartyRoleController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<PartyRoleVO> response = controller.createPartyRoleWithId(VALID_ID, new PartyRoleCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        PartyRole mappedPartyRole = new PartyRole(PartyRole.TYPE_PR);
        mappedPartyRole.setId(URI.create(VALID_ID));
        PartyRoleVO intermediateVO = new PartyRoleVO();
        PartyRoleVO responseVO = new PartyRoleVO();

        PartyRoleCreateVO createVO = new PartyRoleCreateVO();
        when(tmForumMapper.map(any(PartyRoleCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(PartyRoleVO.class))).thenReturn(mappedPartyRole);
        when(tmForumMapper.map(any(PartyRole.class))).thenReturn(responseVO);
        when(partyRoleController.getCheckingMono(any())).thenReturn(Mono.just(mappedPartyRole));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<PartyRoleVO> response = controller.createPartyRoleWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
