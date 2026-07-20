package org.fiware.tmforum.resourcefunction.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.resourcefunction.model.ScaleCreateVO;
import org.fiware.resourcefunction.model.ScaleVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.resourcefunction.TMForumMapper;
import org.fiware.tmforum.resourcefunction.domain.Scale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

class ExtendedScaleApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:scale:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private ScaleApiController scaleApiController;

    private ExtendedScaleApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedScaleApiController(queryParser, validationService, repository, eventHandler, tmForumMapper, scaleApiController);
    }

    private void setDeleteEnabled(boolean value) throws Exception {
        Field field = ExtendedScaleApiController.class.getDeclaredField("deleteEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedScaleApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createScaleWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<ScaleVO> response = controller.createScaleWithId(VALID_ID, new ScaleCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createScaleWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        Scale mappedScale = new Scale(VALID_ID);
        ScaleVO intermediateVO = new ScaleVO();
        ScaleVO responseVO = new ScaleVO();

        ScaleCreateVO createVO = new ScaleCreateVO();
        when(tmForumMapper.map(any(ScaleCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(ScaleVO.class))).thenReturn(mappedScale);
        when(tmForumMapper.map(any(Scale.class))).thenReturn(responseVO);
        when(scaleApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedScale));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<ScaleVO> response = controller.createScaleWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
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
        when(repository.deleteDomainEntity(any())).thenReturn(Mono.empty());

        HttpResponse<Object> response = controller.deleteScale(VALID_ID).block();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
    }
}
