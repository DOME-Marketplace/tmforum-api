package org.fiware.tmforum.servicecatalog.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.servicecatalog.model.ServiceCandidateCreateVO;
import org.fiware.servicecatalog.model.ServiceCandidateVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.service.ServiceCandidate;
import org.fiware.tmforum.servicecatalog.TMForumMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

class ExtendedServiceCandidateApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:service-candidate:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private Clock clock;
    @Mock private ServiceCandidateApiController serviceCandidateApiController;

    private ExtendedServiceCandidateApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedServiceCandidateApiController(queryParser, validationService, repository,
                eventHandler, tmForumMapper, clock, serviceCandidateApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedServiceCandidateApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createServiceCandidateWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<ServiceCandidateVO> response = controller
                .createServiceCandidateWithId(VALID_ID, new ServiceCandidateCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createServiceCandidateWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        when(clock.instant()).thenReturn(Instant.ofEpochSecond(10000));

        ServiceCandidate mappedServiceCandidate = new ServiceCandidate(VALID_ID);
        ServiceCandidateVO intermediateVO = new ServiceCandidateVO();
        ServiceCandidateVO responseVO = new ServiceCandidateVO();

        ServiceCandidateCreateVO createVO = new ServiceCandidateCreateVO();
        when(tmForumMapper.map(any(ServiceCandidateCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(ServiceCandidateVO.class))).thenReturn(mappedServiceCandidate);
        when(tmForumMapper.map(any(ServiceCandidate.class))).thenReturn(responseVO);
        when(serviceCandidateApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedServiceCandidate));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<ServiceCandidateVO> response = controller.createServiceCandidateWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
