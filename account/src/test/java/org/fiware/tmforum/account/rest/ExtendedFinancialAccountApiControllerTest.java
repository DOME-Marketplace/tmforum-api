package org.fiware.tmforum.account.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.account.model.FinancialAccountCreateVO;
import org.fiware.account.model.FinancialAccountVO;
import org.fiware.tmforum.account.TMForumMapper;
import org.fiware.tmforum.account.domain.FinancialAccount;
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

class ExtendedFinancialAccountApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:financialAccount:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private FinancialAccountApiController financialAccountApiController;

    private ExtendedFinancialAccountApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedFinancialAccountApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, financialAccountApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedFinancialAccountApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createFinancialAccountWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<FinancialAccountVO> response = controller.createFinancialAccountWithId(VALID_ID, new FinancialAccountCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createFinancialAccountWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        FinancialAccount mappedFinancialAccount = new FinancialAccount(VALID_ID);
        FinancialAccountVO intermediateVO = new FinancialAccountVO();
        FinancialAccountVO responseVO = new FinancialAccountVO();

        FinancialAccountCreateVO createVO = new FinancialAccountCreateVO();
        when(tmForumMapper.map(any(FinancialAccountCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(FinancialAccountVO.class))).thenReturn(mappedFinancialAccount);
        when(tmForumMapper.map(any(FinancialAccount.class))).thenReturn(responseVO);
        when(financialAccountApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedFinancialAccount));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<FinancialAccountVO> response = controller.createFinancialAccountWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
