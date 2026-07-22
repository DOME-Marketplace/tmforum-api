package org.fiware.tmforum.productcatalog.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.productcatalog.model.ProductOfferingPriceCreateVO;
import org.fiware.productcatalog.model.ProductOfferingPriceVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.product.ProductOfferingPrice;
import org.fiware.tmforum.productcatalog.TMForumMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

class ExtendedProductOfferingPriceApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:product-offering-price:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private ProductOfferingPriceApiController productOfferingPriceApiController;

    private ExtendedProductOfferingPriceApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedProductOfferingPriceApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, productOfferingPriceApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedProductOfferingPriceApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createProductOfferingPriceWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<ProductOfferingPriceVO> response = controller.createProductOfferingPriceWithId(VALID_ID, new ProductOfferingPriceCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createProductOfferingPriceWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        ProductOfferingPrice mappedProductOfferingPrice = new ProductOfferingPrice(VALID_ID);
        ProductOfferingPriceVO intermediateVO = new ProductOfferingPriceVO();
        ProductOfferingPriceVO responseVO = new ProductOfferingPriceVO();

        ProductOfferingPriceCreateVO createVO = new ProductOfferingPriceCreateVO();
        when(tmForumMapper.map(any(ProductOfferingPriceCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(ProductOfferingPriceVO.class))).thenReturn(mappedProductOfferingPrice);
        when(tmForumMapper.map(any(ProductOfferingPrice.class))).thenReturn(responseVO);
        when(productOfferingPriceApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedProductOfferingPrice));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<ProductOfferingPriceVO> response = controller.createProductOfferingPriceWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
