package org.fiware.tmforum.quote.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.quote.model.QuoteCreateVO;
import org.fiware.quote.model.QuoteVO;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.product.Quote;
import org.fiware.tmforum.product.QuoteItem;
import org.fiware.tmforum.quote.TMForumMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

class ExtendedQuoteApiControllerTest {

    private static final String VALID_ID = "urn:ngsi-ld:quote:test-id";

    @Mock private QueryParser queryParser;
    @Mock private ReferenceValidationService validationService;
    @Mock private TmForumRepository repository;
    @Mock private TMForumEventHandler eventHandler;
    @Mock private TMForumMapper tmForumMapper;
    @Mock private Clock clock;
    @Mock private QuoteApiController quoteApiController;

    private ExtendedQuoteApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new ExtendedQuoteApiController(queryParser, validationService, repository, eventHandler,
                tmForumMapper, clock, quoteApiController);
    }

    private void setPutEnabled(boolean value) throws Exception {
        Field field = ExtendedQuoteApiController.class.getDeclaredField("putEnabled");
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void whenPutDisabled_returns405() throws Exception {
        setPutEnabled(false);

        HttpResponse<QuoteVO> response = controller.createQuoteWithId(VALID_ID, new QuoteCreateVO()).block();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    void whenPutEnabled_returns201() throws Exception {
        setPutEnabled(true);

        Quote mappedQuote = new Quote(VALID_ID);
        mappedQuote.setQuoteItem(List.of(new QuoteItem()));
        QuoteVO intermediateVO = new QuoteVO();
        QuoteVO responseVO = new QuoteVO();

        QuoteCreateVO createVO = new QuoteCreateVO();
        when(clock.instant()).thenReturn(Instant.now());
        when(tmForumMapper.map(any(QuoteCreateVO.class), any())).thenReturn(intermediateVO);
        when(tmForumMapper.map(any(QuoteVO.class))).thenReturn(mappedQuote);
        when(tmForumMapper.map(any(Quote.class))).thenReturn(responseVO);
        when(quoteApiController.getCheckingMono(any())).thenReturn(Mono.just(mappedQuote));
        when(repository.createDomainEntity(any())).thenReturn(Mono.empty());
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());

        HttpResponse<QuoteVO> response = controller.createQuoteWithId(VALID_ID, createVO).block();

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals(responseVO, response.body());
    }
}
