package org.fiware.tmforum.customerbillmanagement.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.customerbillmanagement.model.CustomerBillOnDemandCreateVO;
import org.fiware.customerbillmanagement.model.CustomerBillOnDemandVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.customerbillmanagement.TMForumMapper;
import org.fiware.tmforum.customerbillmanagement.domain.CustomerBillOnDemand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

class ExtendedCustomerBillOnDemandApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:customerbillondemand:test-id";
    private static final String VALID_PUT_ID = "urn:ngsi-ld:customer-bill-on-demand:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private CustomerBillOnDemandApiController customerBillOnDemandApiController;

    private ExtendedCustomerBillOnDemandApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedCustomerBillOnDemandApiController(queryParser, validationService, repository, eventHandler, tmForumMapper, customerBillOnDemandApiController);
    }

    private void setDeleteEnabled(boolean value) throws Exception {
        Field field = ExtendedCustomerBillOnDemandApiController.class.getDeclaredField("deleteEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedCustomerBillOnDemandApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createCustomerBillOnDemandWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<CustomerBillOnDemandVO> response = controller.createCustomerBillOnDemandWithId(VALID_ID, new CustomerBillOnDemandCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createCustomerBillOnDemandWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        CustomerBillOnDemand mappedEntity = new CustomerBillOnDemand(VALID_PUT_ID);
        CustomerBillOnDemandVO intermediateVO = new CustomerBillOnDemandVO();
        CustomerBillOnDemandVO responseVO = new CustomerBillOnDemandVO();
        CustomerBillOnDemandCreateVO createVO = new CustomerBillOnDemandCreateVO();

        when(tmForumMapper.map(any(CustomerBillOnDemandCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(CustomerBillOnDemandVO.class))).thenReturn(mappedEntity);
        when(tmForumMapper.map(any(CustomerBillOnDemand.class))).thenReturn(responseVO);
        when(customerBillOnDemandApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedEntity));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<CustomerBillOnDemandVO> response = controller.createCustomerBillOnDemandWithId(VALID_PUT_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }

    @Test
    void deleteCustomerBillOnDemand_whenDeleteDisabled_returns405() throws Exception {
        setDeleteEnabled(false);

        HttpResponse<Object> response = controller.deleteCustomerBillOnDemand(VALID_ID).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void deleteCustomerBillOnDemand_whenDeleteEnabled_returns204() throws Exception {
        setDeleteEnabled(true);
        when(repository.deleteDomainEntity(any())).thenReturn(Mono.empty());

        HttpResponse<Object> response = controller.deleteCustomerBillOnDemand(VALID_ID).block();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
    }
}
