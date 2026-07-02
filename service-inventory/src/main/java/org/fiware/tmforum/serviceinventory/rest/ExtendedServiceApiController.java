package org.fiware.tmforum.serviceinventory.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.serviceinventory.api.ext.ServiceExtensionApi;
import org.fiware.serviceinventory.model.ServiceCreateVO;
import org.fiware.serviceinventory.model.ServiceVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.serviceinventory.TMForumMapper;
import org.fiware.tmforum.serviceinventory.domain.Service;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Controller("${api.service-inventory.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedServiceApiController extends AbstractApiController<Service>
        implements ServiceExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final ServiceApiController serviceApiController;

    public ExtendedServiceApiController(
            QueryParser queryParser,
            ReferenceValidationService validationService,
            TmForumRepository repository,
            TMForumEventHandler eventHandler,
            TMForumMapper tmForumMapper,
            ServiceApiController serviceApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.serviceApiController = serviceApiController;
    }

    @Override
    public Mono<HttpResponse<ServiceVO>> createServiceWithId(String id, ServiceCreateVO createVO) {
        if (!IdHelper.isNgsiLdId(id, Service.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, Service.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        Service service = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        serviceApiController.validateInternalRefs(service);
        return create(serviceApiController.getCheckingMono(service), Service.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
