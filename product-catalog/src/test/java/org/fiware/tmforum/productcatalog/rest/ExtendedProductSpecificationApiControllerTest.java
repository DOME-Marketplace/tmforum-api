package org.fiware.tmforum.productcatalog.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.productcatalog.model.ProductSpecificationCreateVO;
import org.fiware.productcatalog.model.ProductSpecificationVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.product.ProductSpecification;
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

class ExtendedProductSpecificationApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:product-specification:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private ProductSpecificationApiController productSpecificationApiController;

    private ExtendedProductSpecificationApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedProductSpecificationApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, productSpecificationApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedProductSpecificationApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createProductSpecificationWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<ProductSpecificationVO> response = controller.createProductSpecificationWithId(VALID_ID, new ProductSpecificationCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createProductSpecificationWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        ProductSpecification mappedProductSpecification = new ProductSpecification(VALID_ID);
        ProductSpecificationVO intermediateVO = new ProductSpecificationVO();
        ProductSpecificationVO responseVO = new ProductSpecificationVO();

        ProductSpecificationCreateVO createVO = new ProductSpecificationCreateVO();
        when(tmForumMapper.map(any(ProductSpecificationCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(ProductSpecificationVO.class))).thenReturn(mappedProductSpecification);
        when(tmForumMapper.map(any(ProductSpecification.class))).thenReturn(responseVO);
        when(productSpecificationApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedProductSpecification));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<ProductSpecificationVO> response = controller.createProductSpecificationWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
