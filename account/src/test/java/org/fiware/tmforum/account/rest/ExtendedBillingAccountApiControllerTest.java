package org.fiware.tmforum.account.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.account.model.BillingAccountCreateVO;
import org.fiware.account.model.BillingAccountVO;
import org.fiware.tmforum.account.TMForumMapper;
import org.fiware.tmforum.account.domain.BillingAccount;
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

class ExtendedBillingAccountApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:billing-account:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private BillingAccountApiController billingAccountApiController;

    private ExtendedBillingAccountApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedBillingAccountApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, billingAccountApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedBillingAccountApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createBillingAccountWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<BillingAccountVO> response = controller.createBillingAccountWithId(VALID_ID, new BillingAccountCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createBillingAccountWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        BillingAccount mappedBillingAccount = new BillingAccount(VALID_ID);
        BillingAccountVO intermediateVO = new BillingAccountVO();
        BillingAccountVO responseVO = new BillingAccountVO();

        BillingAccountCreateVO createVO = new BillingAccountCreateVO();
        when(tmForumMapper.map(any(BillingAccountCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(BillingAccountVO.class))).thenReturn(mappedBillingAccount);
        when(tmForumMapper.map(any(BillingAccount.class))).thenReturn(responseVO);
        when(billingAccountApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedBillingAccount));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<BillingAccountVO> response = controller.createBillingAccountWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
