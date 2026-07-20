package org.fiware.tmforum.productcatalog.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.productcatalog.api.ext.ProductOfferingExtensionApi;
import org.fiware.productcatalog.model.ProductOfferingCreateVO;
import org.fiware.productcatalog.model.ProductOfferingVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.product.ProductOffering;
import org.fiware.tmforum.productcatalog.TMForumMapper;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Controller("${api.product-catalog.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedProductOfferingApiController extends AbstractApiController<ProductOffering>
        implements ProductOfferingExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final ProductOfferingApiController productOfferingApiController;

    @Value("${apiExtension.putEnabled:false}")
    private boolean putEnabled;

    public ExtendedProductOfferingApiController(QueryParser queryParser, ReferenceValidationService validationService,
                                                TmForumRepository repository, TMForumEventHandler eventHandler,
                                                TMForumMapper tmForumMapper,
                                                ProductOfferingApiController productOfferingApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.productOfferingApiController = productOfferingApiController;
    }

    @Override
    public Mono<HttpResponse<ProductOfferingVO>> createProductOfferingWithId(String id, ProductOfferingCreateVO createVO) {
        if (!putEnabled) {
            return Mono.just(HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED));
        }
        if (!IdHelper.isNgsiLdId(id, ProductOffering.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, ProductOffering.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        ProductOffering productOffering = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(productOfferingApiController.getCheckingMono(productOffering), ProductOffering.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
