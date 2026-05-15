package org.fiware.tmforum.resourcefunction.rest;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

class ExtendedHealApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:heal:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;

    private ExtendedHealApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedHealApiController(queryParser, validationService, repository, eventHandler);
    }

    private void setDeleteEnabled(boolean value) throws Exception {
        Field field = ExtendedHealApiController.class.getDeclaredField("deleteEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void deleteHeal_whenDeleteDisabled_returns405() throws Exception {
        setDeleteEnabled(false);

        HttpResponse<Object> response = controller.deleteHeal(VALID_ID).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void deleteHeal_whenDeleteEnabled_returns204() throws Exception {
        setDeleteEnabled(true);
        when(repository.deleteDomainEntity(any())).thenReturn(Mono.empty());

        HttpResponse<Object> response = controller.deleteHeal(VALID_ID).block();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
    }
}
