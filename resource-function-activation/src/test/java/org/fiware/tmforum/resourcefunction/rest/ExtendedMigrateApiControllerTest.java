package org.fiware.tmforum.resourcefunction.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.resourcefunction.model.MigrateCreateVO;
import org.fiware.resourcefunction.model.MigrateVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.resourcefunction.TMForumMapper;
import org.fiware.tmforum.resourcefunction.domain.Migrate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

class ExtendedMigrateApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:migrate:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private MigrateApiController migrateApiController;

    private ExtendedMigrateApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedMigrateApiController(queryParser, validationService, repository, eventHandler, tmForumMapper, migrateApiController);
    }

    private void setDeleteEnabled(boolean value) throws Exception {
        Field field = ExtendedMigrateApiController.class.getDeclaredField("deleteEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedMigrateApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createMigrateWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<MigrateVO> response = controller.createMigrateWithId(VALID_ID, new MigrateCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createMigrateWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        Migrate mappedMigrate = new Migrate(VALID_ID);
        MigrateVO intermediateVO = new MigrateVO();
        MigrateVO responseVO = new MigrateVO();

        MigrateCreateVO createVO = new MigrateCreateVO();
        when(tmForumMapper.map(any(MigrateCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(MigrateVO.class))).thenReturn(mappedMigrate);
        when(tmForumMapper.map(any(Migrate.class))).thenReturn(responseVO);
        when(migrateApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedMigrate));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<MigrateVO> response = controller.createMigrateWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }

    @Test
    void deleteMigrate_whenDeleteDisabled_returns405() throws Exception {
        setDeleteEnabled(false);

        HttpResponse<Object> response = controller.deleteMigrate(VALID_ID).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void deleteMigrate_whenDeleteEnabled_returns204() throws Exception {
        setDeleteEnabled(true);
        when(repository.deleteDomainEntity(any())).thenReturn(Mono.empty());

        HttpResponse<Object> response = controller.deleteMigrate(VALID_ID).block();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
    }
}
