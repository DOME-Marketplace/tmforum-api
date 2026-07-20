package org.fiware.tmforum.usagemanagement.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.usagemanagement.TMForumMapper;
import org.fiware.tmforum.usagemanagement.domain.Usage;
import org.fiware.usagemanagement.model.UsageCreateVO;
import org.fiware.usagemanagement.model.UsageVO;
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

class ExtendedUsageControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:usage:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private UsageController usageController;

    private ExtendedUsageController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedUsageController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, usageController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedUsageController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createUsageWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<UsageVO> response = controller.createUsageWithId(VALID_ID, new UsageCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createUsageWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        Usage mappedUsage = new Usage(Usage.TYPE_U);
        mappedUsage.setId(URI.create(VALID_ID));
        UsageVO intermediateVO = new UsageVO();
        UsageVO responseVO = new UsageVO();

        UsageCreateVO createVO = new UsageCreateVO();
        when(tmForumMapper.map(any(UsageCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(UsageVO.class))).thenReturn(mappedUsage);
        when(tmForumMapper.map(any(Usage.class))).thenReturn(responseVO);
        when(usageController.getCheckingMono(any())).thenReturn(Mono.just(mappedUsage));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<UsageVO> response = controller.createUsageWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
