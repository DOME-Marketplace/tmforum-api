package org.fiware.tmforum.customermanagement.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.customermanagement.api.ext.CustomerExtensionApi;
import org.fiware.customermanagement.model.CustomerCreateVO;
import org.fiware.customermanagement.model.CustomerVO;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.customermanagement.TMForumMapper;
import org.fiware.tmforum.customermanagement.domain.Customer;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Controller("${api.customer-management.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedCustomerApiController extends AbstractApiController<Customer>
        implements CustomerExtensionApi {

    private final TMForumMapper tmForumMapper;
    private final CustomerApiController customerApiController;

    public ExtendedCustomerApiController(QueryParser queryParser, ReferenceValidationService validationService,
                                         TmForumRepository repository, TMForumEventHandler eventHandler,
                                         TMForumMapper tmForumMapper,
                                         CustomerApiController customerApiController) {
        super(queryParser, validationService, repository, eventHandler);
        this.tmForumMapper = tmForumMapper;
        this.customerApiController = customerApiController;
    }

    @Override
    public Mono<HttpResponse<CustomerVO>> createCustomerWithId(String id, CustomerCreateVO createVO) {
        if (!IdHelper.isNgsiLdId(id, Customer.class)) {
            throw new TmForumException(
                    String.format("Did not receive a valid id %s, the id has to be a valid NGSI-LD URI of type %s.", id, Customer.class.getSimpleName()),
                    TmForumExceptionReason.INVALID_DATA);
        }
        Customer customer = tmForumMapper.map(tmForumMapper.map(createVO, URI.create(id)));
        return create(customerApiController.getCheckingMono(customer), Customer.class)
                .map(tmForumMapper::map)
                .map(HttpResponse::created);
    }
}
