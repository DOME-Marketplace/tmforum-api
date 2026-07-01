package org.fiware.tmforum.productcatalog.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.productcatalog.api.ext.ProductSpecificationExtensionApi;
import org.fiware.productcatalog.model.ProductSpecificationCreateVO;
import org.fiware.productcatalog.model.ProductSpecificationVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.product.ProductSpecification;
import org.fiware.tmforum.productcatalog.TMForumMapper;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Controller("${api.product-catalog.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedProductSpecificationApiController extends AbstractApiController<ProductSpecification>
        implements ProductSpecificationExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final ProductSpecificationApiController productSpecificationApiController;

    public ExtendedProductSpecificationApiController(QueryParser queryParser, ReferenceValidationService validationService,
                                                     TmForumRepository repository, TMForumEventHandler eventHandler,
                                                     TMForumMapper tmForumMapper,
                                                     ProductSpecificationApiController productSpecificationApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.productSpecificationApiController = productSpecificationApiController;
    }

    @Override
    public Mono<HttpResponse<ProductSpecificationVO>> createProductSpecificationWithId(String id, ProductSpecificationCreateVO createVO) {
        if (!IdHelper.isNgsiLdId(id)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI.", id),
                    TmForumExceptionReason.INVALID_DATA);
        }
        ProductSpecification productSpecification = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(productSpecificationApiController.getCheckingMono(productSpecification), ProductSpecification.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
