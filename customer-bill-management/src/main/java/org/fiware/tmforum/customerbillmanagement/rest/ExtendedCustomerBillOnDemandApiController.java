package org.fiware.tmforum.customerbillmanagement.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.customerbillmanagement.api.ext.CustomerBillOnDemandExtensionApi;
import org.fiware.customerbillmanagement.model.CustomerBillOnDemandCreateVO;
import org.fiware.customerbillmanagement.model.CustomerBillOnDemandVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.customerbillmanagement.TMForumMapper;
import org.fiware.tmforum.customerbillmanagement.domain.CustomerBillOnDemand;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Controller("${api.customer-bill-management.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedCustomerBillOnDemandApiController extends AbstractApiController<CustomerBillOnDemand>
        implements CustomerBillOnDemandExtensionApi {

    @Value("${apiExtension.putEnabled:false}")
    private boolean putEnabled;

    @Value("${apiExtension.deleteEnabled:false}")
    private boolean deleteEnabled;

    private final TMForumMapper tmForumMapper;
    private final CustomerBillOnDemandApiController customerBillOnDemandApiController;

    public ExtendedCustomerBillOnDemandApiController(
            QueryParser queryParser,
            ReferenceValidationService validationService,
            TmForumRepository repository,
            TMForumEventHandler eventHandler,
            TMForumMapper tmForumMapper,
            CustomerBillOnDemandApiController customerBillOnDemandApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.customerBillOnDemandApiController = customerBillOnDemandApiController;
    }

    @Override
    public Mono<HttpResponse<CustomerBillOnDemandVO>> createCustomerBillOnDemandWithId(String id, CustomerBillOnDemandCreateVO createVO) {
        if (!putEnabled) {
            return Mono.just(HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED));
        }
        if (!IdHelper.isNgsiLdId(id, CustomerBillOnDemand.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, CustomerBillOnDemand.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        CustomerBillOnDemand entity = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(customerBillOnDemandApiController.getCheckingMono(entity), CustomerBillOnDemand.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }

    @Override
    public Mono<HttpResponse<Object>> deleteCustomerBillOnDemand(String id) {
        if (!deleteEnabled) {
            return Mono.just(HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED));
        }
        return delete(id);
    }
}
