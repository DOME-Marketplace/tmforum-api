package org.fiware.tmforum.party.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.party.model.IndividualCreateVO;
import org.fiware.party.model.IndividualVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.party.TMForumMapper;
import org.fiware.tmforum.party.domain.individual.Individual;
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

class ExtendedIndividualApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:individual:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private IndividualApiController individualApiController;

    private ExtendedIndividualApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedIndividualApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, individualApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedIndividualApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void createIndividualWithId_whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<IndividualVO> response = controller.createIndividualWithId(VALID_ID, new IndividualCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void createIndividualWithId_whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        Individual mappedIndividual = new Individual(Individual.TYPE_INDIVIDUAL);
        mappedIndividual.setId(URI.create(VALID_ID));
        IndividualVO intermediateVO = new IndividualVO();
        IndividualVO responseVO = new IndividualVO();

        IndividualCreateVO createVO = new IndividualCreateVO();
        when(tmForumMapper.map(any(IndividualCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(IndividualVO.class))).thenReturn(mappedIndividual);
        when(tmForumMapper.map(any(Individual.class))).thenReturn(responseVO);
        when(individualApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedIndividual));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<IndividualVO> response = controller.createIndividualWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
