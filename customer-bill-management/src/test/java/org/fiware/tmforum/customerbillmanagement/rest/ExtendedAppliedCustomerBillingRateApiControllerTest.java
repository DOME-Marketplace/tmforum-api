package org.fiware.tmforum.customerbillmanagement.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.customerbillmanagement.model.AppliedCustomerBillingRateCreateVO;
import org.fiware.customerbillmanagement.model.AppliedCustomerBillingRateVO;
import org.fiware.customerbillmanagement.model.BillingAccountRefVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.customerbillmanagement.TMForumMapper;
import org.fiware.tmforum.customerbillmanagement.domain.AppliedCustomerBillingRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

class ExtendedAppliedCustomerBillingRateApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:applied-customer-billing-rate:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private Clock clock;

    private ExtendedAppliedCustomerBillingRateApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedAppliedCustomerBillingRateApiController(queryParser, validationService, repository,
                tmForumMapper, eventHandler, clock);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedAppliedCustomerBillingRateApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createAppliedCustomerBillingRateWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<AppliedCustomerBillingRateVO> response = controller.createAppliedCustomerBillingRateWithId(
                VALID_ID, new AppliedCustomerBillingRateCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createAppliedCustomerBillingRateWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(10000));

        AppliedCustomerBillingRate mappedEntity = new AppliedCustomerBillingRate(VALID_ID);
        AppliedCustomerBillingRateVO intermediateVO = new AppliedCustomerBillingRateVO();
        AppliedCustomerBillingRateVO responseVO = new AppliedCustomerBillingRateVO();

        AppliedCustomerBillingRateCreateVO createVO = new AppliedCustomerBillingRateCreateVO()
                .isBilled(false)
                .bill(null)
                .billingAccount(new BillingAccountRefVO().id("urn:ngsi-ld:billing-account:test-id"))
                .product(null);

        when(tmForumMapper.map(any(AppliedCustomerBillingRateCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(AppliedCustomerBillingRateVO.class))).thenReturn(mappedEntity);
        when(tmForumMapper.map(any(AppliedCustomerBillingRate.class))).thenReturn(responseVO);
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<AppliedCustomerBillingRateVO> response = controller.createAppliedCustomerBillingRateWithId(
                VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
