package org.fiware.tmforum.productcatalog.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.productcatalog.model.ProductOfferingCreateVO;
import org.fiware.productcatalog.model.ProductOfferingVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.product.ProductOffering;
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

class ExtendedProductOfferingApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:product-offering:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private ProductOfferingApiController productOfferingApiController;

    private ExtendedProductOfferingApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedProductOfferingApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, productOfferingApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedProductOfferingApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createProductOfferingWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<ProductOfferingVO> response = controller.createProductOfferingWithId(VALID_ID, new ProductOfferingCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createProductOfferingWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        ProductOffering mappedProductOffering = new ProductOffering(VALID_ID);
        ProductOfferingVO intermediateVO = new ProductOfferingVO();
        ProductOfferingVO responseVO = new ProductOfferingVO();

        ProductOfferingCreateVO createVO = new ProductOfferingCreateVO();
        when(tmForumMapper.map(any(ProductOfferingCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(ProductOfferingVO.class))).thenReturn(mappedProductOffering);
        when(tmForumMapper.map(any(ProductOffering.class))).thenReturn(responseVO);
        when(productOfferingApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedProductOffering));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<ProductOfferingVO> response = controller.createProductOfferingWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
