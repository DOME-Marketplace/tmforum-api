package org.fiware.tmforum.productcatalog.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.productcatalog.api.ext.CategoryExtensionApi;
import org.fiware.productcatalog.model.CategoryCreateVO;
import org.fiware.productcatalog.model.CategoryVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.product.Category;
import org.fiware.tmforum.productcatalog.TMForumMapper;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Controller("${api.product-catalog.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedCategoryApiController extends AbstractApiController<Category>
        implements CategoryExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final CategoryApiController categoryApiController;

    public ExtendedCategoryApiController(QueryParser queryParser, ReferenceValidationService validationService,
                                         TmForumRepository repository, TMForumEventHandler eventHandler,
                                         TMForumMapper tmForumMapper,
                                         CategoryApiController categoryApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.categoryApiController = categoryApiController;
    }

    @Override
    public Mono<HttpResponse<CategoryVO>> createCategoryWithId(String id, CategoryCreateVO createVO) {
        if (!IdHelper.isNgsiLdId(id)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI.", id),
                    TmForumExceptionReason.INVALID_DATA);
        }
        Category category = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(categoryApiController.getCheckingMono(category), Category.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
