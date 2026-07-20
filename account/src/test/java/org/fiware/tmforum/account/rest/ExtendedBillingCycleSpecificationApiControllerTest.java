package org.fiware.tmforum.account.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.account.model.BillingCycleSpecificationCreateVO;
import org.fiware.account.model.BillingCycleSpecificationVO;
import org.fiware.tmforum.account.TMForumMapper;
import org.fiware.tmforum.account.domain.BillingCycleSpecification;
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

class ExtendedBillingCycleSpecificationApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:billingCycleSpecification:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private BillingCycleSpecificationApiController billingCycleSpecificationApiController;

    private ExtendedBillingCycleSpecificationApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedBillingCycleSpecificationApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, billingCycleSpecificationApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedBillingCycleSpecificationApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createBillingCycleSpecificationWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<BillingCycleSpecificationVO> response = controller.createBillingCycleSpecificationWithId(VALID_ID, new BillingCycleSpecificationCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createBillingCycleSpecificationWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        BillingCycleSpecification mappedBillingCycleSpecification = new BillingCycleSpecification(VALID_ID);
        BillingCycleSpecificationVO intermediateVO = new BillingCycleSpecificationVO();
        BillingCycleSpecificationVO responseVO = new BillingCycleSpecificationVO();

        BillingCycleSpecificationCreateVO createVO = new BillingCycleSpecificationCreateVO();
        when(tmForumMapper.map(any(BillingCycleSpecificationCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(BillingCycleSpecificationVO.class))).thenReturn(mappedBillingCycleSpecification);
        when(tmForumMapper.map(any(BillingCycleSpecification.class))).thenReturn(responseVO);
        when(billingCycleSpecificationApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedBillingCycleSpecification));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<BillingCycleSpecificationVO> response = controller.createBillingCycleSpecificationWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
