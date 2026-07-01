package org.fiware.tmforum.productcatalog.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.productcatalog.api.ext.ProductOfferingPriceExtensionApi;
import org.fiware.productcatalog.model.ProductOfferingPriceCreateVO;
import org.fiware.productcatalog.model.ProductOfferingPriceVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.product.ProductOfferingPrice;
import org.fiware.tmforum.productcatalog.TMForumMapper;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Controller("${api.product-catalog.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedProductOfferingPriceApiController extends AbstractApiController<ProductOfferingPrice>
        implements ProductOfferingPriceExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final ProductOfferingPriceApiController productOfferingPriceApiController;

    public ExtendedProductOfferingPriceApiController(QueryParser queryParser, ReferenceValidationService validationService,
                                                     TmForumRepository repository, TMForumEventHandler eventHandler,
                                                     TMForumMapper tmForumMapper,
                                                     ProductOfferingPriceApiController productOfferingPriceApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.productOfferingPriceApiController = productOfferingPriceApiController;
    }

    @Override
    public Mono<HttpResponse<ProductOfferingPriceVO>> createProductOfferingPriceWithId(String id, ProductOfferingPriceCreateVO createVO) {
        if (!IdHelper.isNgsiLdId(id)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI.", id),
                    TmForumExceptionReason.INVALID_DATA);
        }
        ProductOfferingPrice productOfferingPrice = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(productOfferingPriceApiController.getCheckingMono(productOfferingPrice), ProductOfferingPrice.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
