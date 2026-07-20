package org.fiware.tmforum.agreement.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.agreement.model.AgreementCreateVO;
import org.fiware.agreement.model.AgreementVO;
import org.fiware.tmforum.agreement.TMForumMapper;
import org.fiware.tmforum.agreement.domain.Agreement;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
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

class ExtendedAgreementApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:agreement:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private AgreementController agreementController;

    private ExtendedAgreementApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedAgreementApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, agreementController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedAgreementApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createAgreementWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<AgreementVO> response = controller.createAgreementWithId(VALID_ID, new AgreementCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createAgreementWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        Agreement mappedAgreement = new Agreement(Agreement.TYPE_AGREEMENT);
        mappedAgreement.setId(URI.create(VALID_ID));
        AgreementVO intermediateVO = new AgreementVO();
        AgreementVO responseVO = new AgreementVO();

        AgreementCreateVO createVO = new AgreementCreateVO();
        when(tmForumMapper.map(any(AgreementCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(AgreementVO.class))).thenReturn(mappedAgreement);
        when(tmForumMapper.map(any(Agreement.class))).thenReturn(responseVO);
        when(agreementController.getCheckingMono(any())).thenReturn(Mono.just(mappedAgreement));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<AgreementVO> response = controller.createAgreementWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
