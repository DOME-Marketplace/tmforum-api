package org.fiware.tmforum.servicecatalog.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.servicecatalog.model.ServiceCategoryCreateVO;
import org.fiware.servicecatalog.model.ServiceCategoryVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.service.ServiceCategory;
import org.fiware.tmforum.servicecatalog.TMForumMapper;
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

class ExtendedServiceCategoryApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:service-category:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private Clock clock;
    @Mock private ServiceCategoryApiController serviceCategoryApiController;

    private ExtendedServiceCategoryApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedServiceCategoryApiController(queryParser, validationService, repository,
                eventHandler, tmForumMapper, clock, serviceCategoryApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedServiceCategoryApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createServiceCategoryWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<ServiceCategoryVO> response = controller
                .createServiceCategoryWithId(VALID_ID, new ServiceCategoryCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createServiceCategoryWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        when(clock.instant()).thenReturn(Instant.ofEpochSecond(10000));

        ServiceCategory mappedServiceCategory = new ServiceCategory(VALID_ID);
        ServiceCategoryVO intermediateVO = new ServiceCategoryVO();
        ServiceCategoryVO responseVO = new ServiceCategoryVO();

        ServiceCategoryCreateVO createVO = new ServiceCategoryCreateVO();
        when(tmForumMapper.map(any(ServiceCategoryCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(ServiceCategoryVO.class))).thenReturn(mappedServiceCategory);
        when(tmForumMapper.map(any(ServiceCategory.class))).thenReturn(responseVO);
        when(serviceCategoryApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedServiceCategory));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<ServiceCategoryVO> response = controller.createServiceCategoryWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
