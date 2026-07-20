package org.fiware.tmforum.resourceinventory.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.resourceinventory.model.ResourceCreateVO;
import org.fiware.resourceinventory.model.ResourceVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.resource.Resource;
import org.fiware.tmforum.resourceinventory.TMForumMapper;
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

class ExtendedResourceApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:resource:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private ResourceApiController resourceApiController;

    private ExtendedResourceApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedResourceApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, resourceApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedResourceApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<ResourceVO> response = controller.createResourceWithId(VALID_ID, new ResourceCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        Resource mappedResource = new Resource(VALID_ID);
        ResourceVO intermediateVO = new ResourceVO();
        ResourceVO responseVO = new ResourceVO();

        ResourceCreateVO createVO = new ResourceCreateVO();
        when(tmForumMapper.map(any(ResourceCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(ResourceVO.class))).thenReturn(mappedResource);
        when(tmForumMapper.map(any(Resource.class))).thenReturn(responseVO);
        when(resourceApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedResource));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<ResourceVO> response = controller.createResourceWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
