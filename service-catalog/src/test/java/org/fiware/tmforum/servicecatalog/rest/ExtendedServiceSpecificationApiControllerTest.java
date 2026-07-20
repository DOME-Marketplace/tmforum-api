package org.fiware.tmforum.servicecatalog.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.servicecatalog.model.ServiceSpecificationCreateVO;
import org.fiware.servicecatalog.model.ServiceSpecificationVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.servicecatalog.TMForumMapper;
import org.fiware.tmforum.servicecatalog.domain.ServiceSpecification;
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

class ExtendedServiceSpecificationApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:service-specification:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private Clock clock;
    @Mock private ServiceSpecificationApiController serviceSpecificationApiController;

    private ExtendedServiceSpecificationApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedServiceSpecificationApiController(queryParser, validationService, repository,
                eventHandler, tmForumMapper, clock, serviceSpecificationApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedServiceSpecificationApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createServiceSpecificationWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<ServiceSpecificationVO> response = controller
                .createServiceSpecificationWithId(VALID_ID, new ServiceSpecificationCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createServiceSpecificationWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        when(clock.instant()).thenReturn(Instant.ofEpochSecond(10000));

        ServiceSpecification mappedServiceSpecification = new ServiceSpecification(VALID_ID);
        ServiceSpecificationVO intermediateVO = new ServiceSpecificationVO();
        ServiceSpecificationVO responseVO = new ServiceSpecificationVO();

        ServiceSpecificationCreateVO createVO = new ServiceSpecificationCreateVO();
        when(tmForumMapper.map(any(ServiceSpecificationCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(ServiceSpecificationVO.class))).thenReturn(mappedServiceSpecification);
        when(tmForumMapper.map(any(ServiceSpecification.class))).thenReturn(responseVO);
        when(serviceSpecificationApiController.validateSpec(any())).thenReturn(Mono.just(mappedServiceSpecification));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<ServiceSpecificationVO> response =
                controller.createServiceSpecificationWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
