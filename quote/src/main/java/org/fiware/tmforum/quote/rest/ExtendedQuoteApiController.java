package org.fiware.tmforum.quote.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.quote.api.ext.QuoteExtensionApi;
import org.fiware.quote.model.QuoteCreateVO;
import org.fiware.quote.model.QuoteVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.product.Quote;
import org.fiware.tmforum.product.QuoteState;
import org.fiware.tmforum.quote.TMForumMapper;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Clock;

@Slf4j
@Controller("${api.quote.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedQuoteApiController extends AbstractApiController<Quote>
        implements QuoteExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final Clock clock;
    private final QuoteApiController quoteApiController;

    @Value("${apiExtension.putEnabled:false}")
    private boolean putEnabled;

    public ExtendedQuoteApiController(QueryParser queryParser, ReferenceValidationService validationService,
                                      TmForumRepository repository, TMForumEventHandler eventHandler,
                                      TMForumMapper tmForumMapper, Clock clock,
                                      QuoteApiController quoteApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.clock = clock;
        this.quoteApiController = quoteApiController;
    }

    @Override
    public Mono<HttpResponse<QuoteVO>> createQuoteWithId(String id, QuoteCreateVO createVO) {
        if (!putEnabled) {
            return Mono.just(HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED));
        }
        if (!IdHelper.isNgsiLdId(id, Quote.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, Quote.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        Quote quote = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        quote.setQuoteDate(clock.instant());
        quote.setState(QuoteState.IN_PROGRESS);

        if (quote.getQuoteItem() == null || quote.getQuoteItem().isEmpty()) {
            throw new TmForumException("Quotes need at least one QuoteItem.", TmForumExceptionReason.INVALID_DATA);
        }

        return create(quoteApiController.getCheckingMono(quote), Quote.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
