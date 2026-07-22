package org.fiware.tmforum.resourcefunction.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.resourcefunction.model.ResourceFunctionCreateVO;
import org.fiware.resourcefunction.model.ResourceFunctionVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.resourcefunction.TMForumMapper;
import org.fiware.tmforum.resourcefunction.domain.ResourceFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

class ExtendedResourceFunctionApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:resource-function:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private ResourceFunctionApiController resourceFunctionApiController;

    private ExtendedResourceFunctionApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedResourceFunctionApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, resourceFunctionApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedResourceFunctionApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createResourceFunctionWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<ResourceFunctionVO> response = controller.createResourceFunctionWithId(VALID_ID, new ResourceFunctionCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createResourceFunctionWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        ResourceFunction mappedResourceFunction = new ResourceFunction(VALID_ID);
        ResourceFunctionVO intermediateVO = new ResourceFunctionVO();
        ResourceFunctionVO responseVO = new ResourceFunctionVO();

        ResourceFunctionCreateVO createVO = new ResourceFunctionCreateVO();
        when(tmForumMapper.map(any(ResourceFunctionCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(ResourceFunctionVO.class))).thenReturn(mappedResourceFunction);
        when(tmForumMapper.map(any(ResourceFunction.class))).thenReturn(responseVO);
        when(resourceFunctionApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedResourceFunction));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<ResourceFunctionVO> response = controller.createResourceFunctionWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
