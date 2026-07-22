package org.fiware.tmforum.resourcefunction.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.resourcefunction.model.MonitorVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.resourcefunction.TMForumMapper;
import org.fiware.tmforum.resourcefunction.domain.Monitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

class ExtendedMonitorApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:monitor:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;

    private ExtendedMonitorApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedMonitorApiController(queryParser, validationService, repository, eventHandler, tmForumMapper);
    }

    private void setDeleteEnabled(boolean value) throws Exception {
        Field field = ExtendedMonitorApiController.class.getDeclaredField("deleteEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedMonitorApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createMonitorWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<MonitorVO> response = controller.createMonitorWithId(VALID_ID, new MonitorVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createMonitorWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        Monitor mappedMonitor = new Monitor(VALID_ID);
        MonitorVO intermediateVO = new MonitorVO();
        MonitorVO responseVO = new MonitorVO();

        MonitorVO createVO = new MonitorVO();
        when(tmForumMapper.map(any(MonitorVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(MonitorVO.class))).thenReturn(mappedMonitor);
        when(tmForumMapper.map(any(Monitor.class))).thenReturn(responseVO);
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<MonitorVO> response = controller.createMonitorWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }

    @Test
    void deleteMonitor_whenDeleteDisabled_returns405() throws Exception {
        setDeleteEnabled(false);

        HttpResponse<Object> response = controller.deleteMonitor(VALID_ID).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void deleteMonitor_whenDeleteEnabled_returns204() throws Exception {
        setDeleteEnabled(true);
        when(repository.deleteDomainEntity(any())).thenReturn(Mono.empty());

        HttpResponse<Object> response = controller.deleteMonitor(VALID_ID).block();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
    }
}
