package org.fiware.tmforum.productinventory.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.productinventory.api.ext.ProductExtensionApi;
import org.fiware.productinventory.model.ProductCreateVO;
import org.fiware.productinventory.model.ProductVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.product.Product;
import org.fiware.tmforum.productinventory.TMForumMapper;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Controller("${api.product-inventory.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedProductApiController extends AbstractApiController<Product>
        implements ProductExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final ProductApiController productApiController;

    public ExtendedProductApiController(QueryParser queryParser, ReferenceValidationService validationService,
                                        TmForumRepository repository, TMForumEventHandler eventHandler,
                                        TMForumMapper tmForumMapper,
                                        ProductApiController productApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.productApiController = productApiController;
    }

    @Override
    public Mono<HttpResponse<ProductVO>> createProductWithId(String id, ProductCreateVO createVO) {
        if (!IdHelper.isNgsiLdId(id, Product.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, Product.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        Product product = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(productApiController.getCheckingMono(product), Product.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
