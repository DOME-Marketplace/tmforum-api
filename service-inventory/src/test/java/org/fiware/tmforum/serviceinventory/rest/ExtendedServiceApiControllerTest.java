package org.fiware.tmforum.serviceinventory.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.serviceinventory.model.ServiceCreateVO;
import org.fiware.serviceinventory.model.ServiceVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.serviceinventory.TMForumMapper;
import org.fiware.tmforum.serviceinventory.domain.Service;
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

class ExtendedServiceApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:service:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private ServiceApiController serviceApiController;

    private ExtendedServiceApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedServiceApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, serviceApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedServiceApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createServiceWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<ServiceVO> response = controller.createServiceWithId(VALID_ID, new ServiceCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createServiceWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        Service mappedService = new Service(Service.TYPE_SERVICE);
        mappedService.setId(URI.create(VALID_ID));
        ServiceVO intermediateVO = new ServiceVO();
        ServiceVO responseVO = new ServiceVO();

        ServiceCreateVO createVO = new ServiceCreateVO();
        when(tmForumMapper.map(any(ServiceCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(ServiceVO.class))).thenReturn(mappedService);
        when(tmForumMapper.map(any(Service.class))).thenReturn(responseVO);
        when(serviceApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedService));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<ServiceVO> response = controller.createServiceWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
