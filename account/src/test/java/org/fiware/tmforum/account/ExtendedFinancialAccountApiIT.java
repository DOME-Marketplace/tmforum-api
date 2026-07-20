package org.fiware.tmforum.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.account.api.ext.FinancialAccountExtensionApiTestClient;
import org.fiware.account.api.ext.FinancialAccountExtensionApiTestSpec;
import org.fiware.account.model.FinancialAccountCreateVO;
import org.fiware.account.model.FinancialAccountCreateVOTestExample;
import org.fiware.account.model.FinancialAccountVO;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.tmforum.account.domain.FinancialAccount;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest(packages = {"org.fiware.tmforum.account"})
@Property(name = "apiExtension.enabled", value = "true")
@Property(name = "apiExtension.putEnabled", value = "true")
public class ExtendedFinancialAccountApiIT extends AbstractApiIT implements FinancialAccountExtensionApiTestSpec {

    private final FinancialAccountExtensionApiTestClient testClient;

    public ExtendedFinancialAccountApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                         GeneralProperties generalProperties,
                                         FinancialAccountExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @MockBean(TMForumEventHandler.class)
    public TMForumEventHandler eventHandler() {
        TMForumEventHandler eventHandler = mock(TMForumEventHandler.class);
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());
        when(eventHandler.handleUpdateEvent(any(), any())).thenReturn(Mono.empty());
        return eventHandler;
    }

    @Override
    protected String getEntityType() {
        return FinancialAccount.TYPE_FINANCIALAC;
    }

    private static FinancialAccountCreateVO buildCreateVO() {
        return FinancialAccountCreateVOTestExample.build().atSchemaLocation(null);
    }

    @Test
    @Override
    public void createFinancialAccountWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), FinancialAccount.TYPE_FINANCIALAC).toString();

        HttpResponse<FinancialAccountVO> response = callAndCatch(
                () -> testClient.createFinancialAccountWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "FinancialAccount should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createFinancialAccountWithId400() throws Exception {
        HttpResponse<FinancialAccountVO> response = callAndCatch(
                () -> testClient.createFinancialAccountWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createFinancialAccountWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createFinancialAccountWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createFinancialAccountWithId405() throws Exception {
    }

    @Test
    @Override
    public void createFinancialAccountWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), FinancialAccount.TYPE_FINANCIALAC).toString();

        HttpResponse<FinancialAccountVO> firstResponse = callAndCatch(
                () -> testClient.createFinancialAccountWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<FinancialAccountVO> secondResponse = callAndCatch(
                () -> testClient.createFinancialAccountWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createFinancialAccountWithId500() throws Exception {
    }
}
