package org.fiware.tmforum.customerbillmanagement.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.customerbillmanagement.model.CustomerBillCreateVO;
import org.fiware.customerbillmanagement.model.CustomerBillVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.customerbillmanagement.TMForumMapper;
import org.fiware.tmforum.customerbillmanagement.domain.CustomerBill;
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

class ExtendedCustomerBillApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:customer-bill:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private Clock clock;
    @Mock private CustomerBillApiController customerBillApiController;

    private ExtendedCustomerBillApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedCustomerBillApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, clock, customerBillApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedCustomerBillApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createCustomerBillWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<CustomerBillVO> response = controller.createCustomerBillWithId(VALID_ID, new CustomerBillCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createCustomerBillWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(10000));

        CustomerBill mappedEntity = new CustomerBill(VALID_ID);
        CustomerBillVO intermediateVO = new CustomerBillVO();
        CustomerBillVO responseVO = new CustomerBillVO();
        CustomerBillCreateVO createVO = new CustomerBillCreateVO();

        when(tmForumMapper.map(any(CustomerBillCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(CustomerBillVO.class))).thenReturn(mappedEntity);
        when(tmForumMapper.map(any(CustomerBill.class))).thenReturn(responseVO);
        when(customerBillApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedEntity));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<CustomerBillVO> response = controller.createCustomerBillWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
