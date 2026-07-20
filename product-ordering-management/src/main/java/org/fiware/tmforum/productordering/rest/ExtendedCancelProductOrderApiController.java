package org.fiware.tmforum.productordering.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.productordering.api.ext.CancelProductOrderExtensionApi;
import org.fiware.productordering.model.CancelProductOrderCreateVO;
import org.fiware.productordering.model.CancelProductOrderVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.productordering.TMForumMapper;
import org.fiware.tmforum.productordering.domain.CancelProductOrder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Slf4j
@Controller("${api.product-ordering-management.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedCancelProductOrderApiController extends AbstractApiController<CancelProductOrder>
        implements CancelProductOrderExtensionApi {

    @Value("${apiExtension.putEnabled:false}")
    private boolean putEnabled;

    @Value("${apiExtension.deleteEnabled:false}")
    private boolean deleteEnabled;

    private final TMForumMapper tmForumMapper;

    public ExtendedCancelProductOrderApiController(
            QueryParser queryParser,
            ReferenceValidationService validationService,
            TmForumRepository repository,
            TMForumEventHandler eventHandler,
            TMForumMapper tmForumMapper) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
    }

    @Override
    public Mono<HttpResponse<CancelProductOrderVO>> createCancelProductOrderWithId(String id, CancelProductOrderCreateVO createVO) {
        if (!putEnabled) {
            return Mono.just(HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED));
        }
        if (!IdHelper.isNgsiLdId(id, CancelProductOrder.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, CancelProductOrder.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        if (createVO.getProductOrder() == null) {
            throw new TmForumException("Received a cancellation without a product.",
                    TmForumExceptionReason.INVALID_DATA);
        }
        CancelProductOrder cancelProductOrder = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        Mono<CancelProductOrder> checkingMono = getCheckingMono(cancelProductOrder,
                List.of(List.of(cancelProductOrder.getProductOrder())))
                .onErrorMap(throwable -> new TmForumException(
                        String.format("Was not able to cancel product order %s", cancelProductOrder.getId()),
                        throwable,
                        TmForumExceptionReason.INVALID_RELATIONSHIP));
        return create(checkingMono, CancelProductOrder.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }

    @Override
    public Mono<HttpResponse<Object>> deleteCancelProductOrder(String id) {
        if (!deleteEnabled) {
            return Mono.just(HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED));
        }
        return delete(id);
    }
}
