package org.fiware.tmforum.customerbillmanagement.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import org.fiware.customerbillmanagement.api.ext.CustomerBillOnDemandExtensionApi;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.querying.QueryParser;
import org.fiware.tmforum.common.repository.TmForumRepository;
import org.fiware.tmforum.common.rest.AbstractApiController;
import org.fiware.tmforum.common.validation.ReferenceValidationService;
import org.fiware.tmforum.customerbillmanagement.domain.CustomerBillOnDemand;
import reactor.core.publisher.Mono;

@Slf4j
@Controller("${api.customer-bill-management.basepath:/}")
@Requires(property = "apiExtension.enabled", value = "true")
public class ExtendedCustomerBillOnDemandApiController extends AbstractApiController<CustomerBillOnDemand>
        implements CustomerBillOnDemandExtensionApi {

    @Value("${apiExtension.deleteEnabled:true}")
    private boolean deleteEnabled;

    public ExtendedCustomerBillOnDemandApiController(
            QueryParser queryParser,
            ReferenceValidationService validationService,
            TmForumRepository repository,
            TMForumEventHandler eventHandler) {
        super(queryParser, validationService, repository, eventHandler);
    }

    @Override
    public Mono<HttpResponse<Object>> deleteCustomerBillOnDemand(String id) {
        if (!deleteEnabled) {
            return Mono.just(HttpResponse.status(HttpStatus.METHOD_NOT_ALLOWED));
        }
        return delete(id);
    }
}
