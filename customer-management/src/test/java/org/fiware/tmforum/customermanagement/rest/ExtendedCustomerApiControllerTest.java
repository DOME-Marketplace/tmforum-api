package org.fiware.tmforum.customermanagement.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.customermanagement.model.CustomerCreateVO;
import org.fiware.customermanagement.model.CustomerVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.customermanagement.TMForumMapper;
import org.fiware.tmforum.customermanagement.domain.Customer;
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

class ExtendedCustomerApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:customer:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private CustomerApiController customerApiController;

    private ExtendedCustomerApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedCustomerApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, customerApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedCustomerApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createCustomerWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<CustomerVO> response = controller.createCustomerWithId(VALID_ID, new CustomerCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createCustomerWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        Customer mappedCustomer = new Customer(VALID_ID);
        mappedCustomer.setId(URI.create(VALID_ID));
        CustomerVO intermediateVO = new CustomerVO();
        CustomerVO responseVO = new CustomerVO();

        CustomerCreateVO createVO = new CustomerCreateVO();
        when(tmForumMapper.map(any(CustomerCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(CustomerVO.class))).thenReturn(mappedCustomer);
        when(tmForumMapper.map(any(Customer.class))).thenReturn(responseVO);
        when(customerApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedCustomer));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<CustomerVO> response = controller.createCustomerWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
