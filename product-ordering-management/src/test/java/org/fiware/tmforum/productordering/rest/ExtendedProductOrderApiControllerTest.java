package org.fiware.tmforum.productordering.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.productordering.model.ProductOrderCreateVO;
import org.fiware.productordering.model.ProductOrderVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.productordering.TMForumMapper;
import org.fiware.tmforum.productordering.domain.ProductOrder;
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

class ExtendedProductOrderApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:product-order:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private Clock clock;
    @Mock private ProductOrderingApiController productOrderingApiController;

    private ExtendedProductOrderApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedProductOrderApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, clock, productOrderingApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedProductOrderApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createProductOrderWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<ProductOrderVO> response =
                controller.createProductOrderWithId(VALID_ID, new ProductOrderCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createProductOrderWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        ProductOrder mappedProductOrder = new ProductOrder(VALID_ID);
        ProductOrderVO intermediateVO = new ProductOrderVO();
        ProductOrderVO responseVO = new ProductOrderVO();

        ProductOrderCreateVO createVO = new ProductOrderCreateVO();
        when(clock.instant()).thenReturn(Instant.now());
        when(tmForumMapper.map(any(ProductOrderCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(ProductOrderVO.class))).thenReturn(mappedProductOrder);
        when(tmForumMapper.map(any(ProductOrder.class))).thenReturn(responseVO);
        when(productOrderingApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedProductOrder));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<ProductOrderVO> response = controller.createProductOrderWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
