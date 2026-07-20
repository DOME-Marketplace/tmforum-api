package org.fiware.tmforum.resourcefunction.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.resourcefunction.model.HealCreateVO;
import org.fiware.resourcefunction.model.HealVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.resourcefunction.TMForumMapper;
import org.fiware.tmforum.resourcefunction.domain.Heal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

class ExtendedHealApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:heal:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private HealApiController healApiController;

    private ExtendedHealApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedHealApiController(queryParser, validationService, repository, eventHandler, tmForumMapper, healApiController);
    }

    private void setDeleteEnabled(boolean value) throws Exception {
        Field field = ExtendedHealApiController.class.getDeclaredField("deleteEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedHealApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createHealWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<HealVO> response = controller.createHealWithId(VALID_ID, new HealCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createHealWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        Heal mappedHeal = new Heal(VALID_ID);
        HealVO intermediateVO = new HealVO();
        HealVO responseVO = new HealVO();

        HealCreateVO createVO = new HealCreateVO();
        when(tmForumMapper.map(any(HealCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(HealVO.class))).thenReturn(mappedHeal);
        when(tmForumMapper.map(any(Heal.class))).thenReturn(responseVO);
        when(healApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedHeal));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<HealVO> response = controller.createHealWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }

    @Test
    void deleteHeal_whenDeleteDisabled_returns405() throws Exception {
        setDeleteEnabled(false);

        HttpResponse<Object> response = controller.deleteHeal(VALID_ID).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void deleteHeal_whenDeleteEnabled_returns204() throws Exception {
        setDeleteEnabled(true);
        when(repository.deleteDomainEntity(any())).thenReturn(Mono.empty());

        HttpResponse<Object> response = controller.deleteHeal(VALID_ID).block();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
    }
}
