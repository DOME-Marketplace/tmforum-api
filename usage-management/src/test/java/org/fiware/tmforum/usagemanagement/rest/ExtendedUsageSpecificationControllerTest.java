package org.fiware.tmforum.usagemanagement.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.usagemanagement.TMForumMapper;
import org.fiware.tmforum.usagemanagement.domain.UsageSpecification;
import org.fiware.usagemanagement.model.UsageSpecificationCreateVO;
import org.fiware.usagemanagement.model.UsageSpecificationVO;
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

class ExtendedUsageSpecificationControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:usageSpecification:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private UsageSpecificationController usageSpecificationController;

    private ExtendedUsageSpecificationController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedUsageSpecificationController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, usageSpecificationController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedUsageSpecificationController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createUsageSpecificationWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<UsageSpecificationVO> response =
                controller.createUsageSpecificationWithId(VALID_ID, new UsageSpecificationCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createUsageSpecificationWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        UsageSpecification mappedUsageSpecification = new UsageSpecification(UsageSpecification.TYPE_USP);
        mappedUsageSpecification.setId(URI.create(VALID_ID));
        UsageSpecificationVO intermediateVO = new UsageSpecificationVO();
        UsageSpecificationVO responseVO = new UsageSpecificationVO();

        UsageSpecificationCreateVO createVO = new UsageSpecificationCreateVO();
        when(tmForumMapper.map(any(UsageSpecificationCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(UsageSpecificationVO.class))).thenReturn(mappedUsageSpecification);
        when(tmForumMapper.map(any(UsageSpecification.class))).thenReturn(responseVO);
        when(usageSpecificationController.getCheckingMono(any())).thenReturn(Mono.just(mappedUsageSpecification));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<UsageSpecificationVO> response =
                controller.createUsageSpecificationWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
