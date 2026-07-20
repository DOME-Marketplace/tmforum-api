package org.fiware.tmforum.resourcecatalog.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.resourcecatalog.model.ResourceCategoryCreateVO;
import org.fiware.resourcecatalog.model.ResourceCategoryVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.resource.ResourceCategory;
import org.fiware.tmforum.resourcecatalog.TMForumMapper;
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

class ExtendedResourceCategoryApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:resource-category:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private Clock clock;
    @Mock private ResourceCategoryApiController resourceCategoryApiController;

    private ExtendedResourceCategoryApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedResourceCategoryApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, clock, resourceCategoryApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedResourceCategoryApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createResourceCategoryWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<ResourceCategoryVO> response = controller
                .createResourceCategoryWithId(VALID_ID, new ResourceCategoryCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createResourceCategoryWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        when(clock.instant()).thenReturn(Instant.now());

        ResourceCategory mappedResourceCategory = new ResourceCategory(VALID_ID);
        ResourceCategoryVO intermediateVO = new ResourceCategoryVO();
        ResourceCategoryVO responseVO = new ResourceCategoryVO();

        ResourceCategoryCreateVO createVO = new ResourceCategoryCreateVO();
        when(tmForumMapper.map(any(ResourceCategoryCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(ResourceCategoryVO.class))).thenReturn(mappedResourceCategory);
        when(tmForumMapper.map(any(ResourceCategory.class))).thenReturn(responseVO);
        when(resourceCategoryApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedResourceCategory));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<ResourceCategoryVO> response = controller.createResourceCategoryWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
