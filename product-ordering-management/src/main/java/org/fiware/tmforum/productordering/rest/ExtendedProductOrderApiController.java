package org.fiware.tmforum.productordering.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.productordering.api.ext.ProductOrderExtensionApi;
import org.fiware.productordering.model.ProductOrderCreateVO;
import org.fiware.productordering.model.ProductOrderVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.productordering.TMForumMapper;
import org.fiware.tmforum.productordering.domain.ProductOrder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Clock;

@Slf4j
@Controller("${api.product-ordering-management.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedProductOrderApiController extends AbstractApiController<ProductOrder>
        implements ProductOrderExtensionApi {

    @Value("${apiExtension.putEnabled:false}")
    private boolean putEnabled;

    private final TMForumMapper tmForumMapper;
    private final Clock clock;
    private final ProductOrderingApiController productOrderingApiController;

    public ExtendedProductOrderApiController(QueryParser queryParser, ReferenceValidationService validationService,
                                             TmForumRepository repository, TMForumEventHandler eventHandler,
                                             TMForumMapper tmForumMapper, Clock clock,
                                             ProductOrderingApiController productOrderingApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.clock = clock;
        this.productOrderingApiController = productOrderingApiController;
    }

    @Override
    public Mono<HttpResponse<ProductOrderVO>> createProductOrderWithId(String id, ProductOrderCreateVO createVO) {
        if (!putEnabled) {
            return Mono.just(HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED));
        }
        if (!IdHelper.isNgsiLdId(id, ProductOrder.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, ProductOrder.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        ProductOrder productOrder = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        productOrder.setOrderDate(clock.instant());
        return create(productOrderingApiController.getCheckingMono(productOrder), ProductOrder.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
