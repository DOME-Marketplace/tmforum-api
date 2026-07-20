package org.fiware.tmforum.productcatalog.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.productcatalog.model.CategoryCreateVO;
import org.fiware.productcatalog.model.CategoryVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.product.Category;
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

class ExtendedCategoryApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:category:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private CategoryApiController categoryApiController;

    private ExtendedCategoryApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedCategoryApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, categoryApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedCategoryApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createCategoryWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<CategoryVO> response = controller.createCategoryWithId(VALID_ID, new CategoryCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createCategoryWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        Category mappedCategory = new Category(VALID_ID);
        CategoryVO intermediateVO = new CategoryVO();
        CategoryVO responseVO = new CategoryVO();

        CategoryCreateVO createVO = new CategoryCreateVO();
        when(tmForumMapper.map(any(CategoryCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(CategoryVO.class))).thenReturn(mappedCategory);
        when(tmForumMapper.map(any(Category.class))).thenReturn(responseVO);
        when(categoryApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedCategory));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<CategoryVO> response = controller.createCategoryWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
