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
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ExtendedScaleApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:scale:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;

    private ExtendedScaleApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedScaleApiController(queryParser, validationService, repository, eventHandler);
    }

    private void setDeleteEnabled(boolean value) throws Exception {
        Field field = ExtendedScaleApiController.class.getDeclaredField("deleteEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void deleteScale_whenDeleteDisabled_returns405() throws Exception {
        setDeleteEnabled(false);

        HttpResponse<Object> response = controller.deleteScale(VALID_ID).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void deleteScale_whenDeleteEnabled_returns204() throws Exception {
        setDeleteEnabled(true);
        when(repository.deleteDomainEntity(any(URI.class))).thenReturn(Mono.empty());

        HttpResponse<Object> response = controller.deleteScale(VALID_ID).block();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
    }
}
