package org.fiware.tmforum.agreement.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.agreement.model.AgreementSpecificationCreateVO;
import org.fiware.agreement.model.AgreementSpecificationVO;
import org.fiware.tmforum.agreement.TMForumMapper;
import org.fiware.tmforum.agreement.domain.AgreementSpecification;
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

class ExtendedAgreementSpecificationApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:agreementSpecification:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private AgreementSpecController agreementSpecController;

    private ExtendedAgreementSpecificationApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedAgreementSpecificationApiController(queryParser, validationService, repository,
                eventHandler, tmForumMapper, agreementSpecController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedAgreementSpecificationApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createAgreementSpecificationWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<AgreementSpecificationVO> response = controller
                .createAgreementSpecificationWithId(VALID_ID, new AgreementSpecificationCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createAgreementSpecificationWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        AgreementSpecification mappedAgreementSpecification = new AgreementSpecification(AgreementSpecification.TYPE_AGREEMENT_SPECIFICATION);
        mappedAgreementSpecification.setId(URI.create(VALID_ID));
        AgreementSpecificationVO intermediateVO = new AgreementSpecificationVO();
        AgreementSpecificationVO responseVO = new AgreementSpecificationVO();

        AgreementSpecificationCreateVO createVO = new AgreementSpecificationCreateVO();
        when(tmForumMapper.map(any(AgreementSpecificationCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(AgreementSpecificationVO.class))).thenReturn(mappedAgreementSpecification);
        when(tmForumMapper.map(any(AgreementSpecification.class))).thenReturn(responseVO);
        when(agreementSpecController.getCheckingMono(any())).thenReturn(Mono.just(mappedAgreementSpecification));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<AgreementSpecificationVO> response = controller
                .createAgreementSpecificationWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
