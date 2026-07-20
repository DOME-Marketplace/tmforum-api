package org.fiware.tmforum.resourcecatalog.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.resourcecatalog.model.ResourceSpecificationCreateVO;
import org.fiware.resourcecatalog.model.ResourceSpecificationVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.resource.ResourceSpecification;
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

class ExtendedResourceSpecificationApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:resource-specification:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private Clock clock;
    @Mock private ResourceSpecifcationApiController resourceSpecifcationApiController;

    private ExtendedResourceSpecificationApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedResourceSpecificationApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, clock, resourceSpecifcationApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedResourceSpecificationApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createResourceSpecificationWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<ResourceSpecificationVO> response = controller
                .createResourceSpecificationWithId(VALID_ID, new ResourceSpecificationCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createResourceSpecificationWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        when(clock.instant()).thenReturn(Instant.now());

        ResourceSpecification mappedResourceSpecification = new ResourceSpecification(VALID_ID);
        ResourceSpecificationVO intermediateVO = new ResourceSpecificationVO();
        ResourceSpecificationVO responseVO = new ResourceSpecificationVO();

        ResourceSpecificationCreateVO createVO = new ResourceSpecificationCreateVO();
        createVO.name("test-spec");
        when(tmForumMapper.map(any(ResourceSpecificationCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(ResourceSpecificationVO.class))).thenReturn(mappedResourceSpecification);
        when(tmForumMapper.map(any(ResourceSpecification.class))).thenReturn(responseVO);
        when(resourceSpecifcationApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedResourceSpecification));
        when(resourceSpecifcationApiController.validateSpec(any())).thenReturn(Mono.just(mappedResourceSpecification));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<ResourceSpecificationVO> response = controller.createResourceSpecificationWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
