package org.fiware.tmforum.productcatalog.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.productcatalog.model.CatalogCreateVO;
import org.fiware.productcatalog.model.CatalogVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.productcatalog.TMForumMapper;
import org.fiware.tmforum.productcatalog.domain.Catalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

class ExtendedCatalogApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:catalog:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private CatalogApiController catalogApiController;

    private ExtendedCatalogApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedCatalogApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, catalogApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedCatalogApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createCatalogWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<CatalogVO> response = controller.createCatalogWithId(VALID_ID, new CatalogCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createCatalogWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        Catalog mappedCatalog = new Catalog(VALID_ID);
        CatalogVO intermediateVO = new CatalogVO();
        CatalogVO responseVO = new CatalogVO();

        CatalogCreateVO createVO = new CatalogCreateVO();
        when(tmForumMapper.map(any(CatalogCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(CatalogVO.class))).thenReturn(mappedCatalog);
        when(tmForumMapper.map(any(Catalog.class))).thenReturn(responseVO);
        when(catalogApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedCatalog));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<CatalogVO> response = controller.createCatalogWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
