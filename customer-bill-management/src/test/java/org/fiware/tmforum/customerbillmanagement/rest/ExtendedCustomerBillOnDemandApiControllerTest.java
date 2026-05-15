package org.fiware.tmforum.customerbillmanagement.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
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
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ExtendedCustomerBillOnDemandApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:customerbillondemand:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;

    private ExtendedCustomerBillOnDemandApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedCustomerBillOnDemandApiController(queryParser, validationService, repository, eventHandler);
    }

    private void setDeleteEnabled(boolean value) throws Exception {
        Field field = ExtendedCustomerBillOnDemandApiController.class.getDeclaredField("deleteEnabled");
        field.setAccessible(true);
        field.set(controller, value);
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
        when(repository.deleteDomainEntity(any(URI.class))).thenReturn(Mono.empty());

        HttpResponse<Object> response = controller.deleteCustomerBillOnDemand(VALID_ID).block();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
    }
}
