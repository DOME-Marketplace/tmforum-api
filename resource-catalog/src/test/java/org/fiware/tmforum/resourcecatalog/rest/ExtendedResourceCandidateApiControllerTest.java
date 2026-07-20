package org.fiware.tmforum.resourcecatalog.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.resourcecatalog.model.ResourceCandidateCreateVO;
import org.fiware.resourcecatalog.model.ResourceCandidateVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.resource.ResourceCandidate;
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

class ExtendedResourceCandidateApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:resource-candidate:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private Clock clock;
    @Mock private ResourceCandidateApiController resourceCandidateApiController;

    private ExtendedResourceCandidateApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedResourceCandidateApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, clock, resourceCandidateApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedResourceCandidateApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createResourceCandidateWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<ResourceCandidateVO> response = controller
                .createResourceCandidateWithId(VALID_ID, new ResourceCandidateCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createResourceCandidateWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        when(clock.instant()).thenReturn(Instant.now());

        ResourceCandidate mappedResourceCandidate = new ResourceCandidate(VALID_ID);
        ResourceCandidateVO intermediateVO = new ResourceCandidateVO();
        ResourceCandidateVO responseVO = new ResourceCandidateVO();

        ResourceCandidateCreateVO createVO = new ResourceCandidateCreateVO();
        when(tmForumMapper.map(any(ResourceCandidateCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(ResourceCandidateVO.class))).thenReturn(mappedResourceCandidate);
        when(tmForumMapper.map(any(ResourceCandidate.class))).thenReturn(responseVO);
        when(resourceCandidateApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedResourceCandidate));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<ResourceCandidateVO> response = controller.createResourceCandidateWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
