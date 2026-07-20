package org.fiware.tmforum.productcatalog.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.productcatalog.api.ext.CatalogExtensionApi;
import org.fiware.productcatalog.model.CatalogCreateVO;
import org.fiware.productcatalog.model.CatalogVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.productcatalog.TMForumMapper;
import org.fiware.tmforum.productcatalog.domain.Catalog;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Controller("${api.product-catalog.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedCatalogApiController extends AbstractApiController<Catalog>
        implements CatalogExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final CatalogApiController catalogApiController;

    @Value("${apiExtension.putEnabled:false}")
    private boolean putEnabled;

    public ExtendedCatalogApiController(QueryParser queryParser, ReferenceValidationService validationService,
                                        TmForumRepository repository, TMForumEventHandler eventHandler,
                                        TMForumMapper tmForumMapper,
                                        CatalogApiController catalogApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.catalogApiController = catalogApiController;
    }

    @Override
    public Mono<HttpResponse<CatalogVO>> createCatalogWithId(String id, CatalogCreateVO createVO) {
        if (!putEnabled) {
            return Mono.just(HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED));
        }
        if (!IdHelper.isNgsiLdId(id, Catalog.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, Catalog.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        Catalog catalog = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(catalogApiController.getCheckingMono(catalog), Catalog.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
