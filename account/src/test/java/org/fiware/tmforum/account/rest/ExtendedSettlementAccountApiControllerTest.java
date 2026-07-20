package org.fiware.tmforum.account.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.account.model.SettlementAccountCreateVO;
import org.fiware.account.model.SettlementAccountVO;
import org.fiware.tmforum.account.TMForumMapper;
import org.fiware.tmforum.account.domain.SettlementAccount;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

class ExtendedSettlementAccountApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:settlement-account:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private SettlementAccountApiController settlementAccountApiController;

    private ExtendedSettlementAccountApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedSettlementAccountApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, settlementAccountApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedSettlementAccountApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createSettlementAccountWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<SettlementAccountVO> response = controller.createSettlementAccountWithId(VALID_ID, new SettlementAccountCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createSettlementAccountWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        SettlementAccount mappedSettlementAccount = new SettlementAccount(VALID_ID);
        SettlementAccountVO intermediateVO = new SettlementAccountVO();
        SettlementAccountVO responseVO = new SettlementAccountVO();

        SettlementAccountCreateVO createVO = new SettlementAccountCreateVO();
        when(tmForumMapper.map(any(SettlementAccountCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(SettlementAccountVO.class))).thenReturn(mappedSettlementAccount);
        when(tmForumMapper.map(any(SettlementAccount.class))).thenReturn(responseVO);
        when(settlementAccountApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedSettlementAccount));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<SettlementAccountVO> response = controller.createSettlementAccountWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
