package org.fiware.tmforum.productinventory.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.productinventory.model.ProductCreateVO;
import org.fiware.productinventory.model.ProductVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.product.Product;
import org.fiware.tmforum.productinventory.TMForumMapper;
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

class ExtendedProductApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:product:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private ProductApiController productApiController;

    private ExtendedProductApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedProductApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, productApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedProductApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createProductWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<ProductVO> response = controller.createProductWithId(VALID_ID, new ProductCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createProductWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        Product mappedProduct = new Product(Product.TYPE_PRODUCT);
        mappedProduct.setId(URI.create(VALID_ID));
        ProductVO intermediateVO = new ProductVO();
        ProductVO responseVO = new ProductVO();

        ProductCreateVO createVO = new ProductCreateVO();
        when(tmForumMapper.map(any(ProductCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(ProductVO.class))).thenReturn(mappedProduct);
        when(tmForumMapper.map(any(Product.class))).thenReturn(responseVO);
        when(productApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedProduct));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<ProductVO> response = controller.createProductWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
