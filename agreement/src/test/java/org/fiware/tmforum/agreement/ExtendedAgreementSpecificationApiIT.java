package org.fiware.tmforum.agreement;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.agreement.api.ext.AgreementSpecificationExtensionApiTestClient;
import org.fiware.agreement.api.ext.AgreementSpecificationExtensionApiTestSpec;
import org.fiware.agreement.model.AgreementSpecificationCreateVO;
import org.fiware.agreement.model.AgreementSpecificationCreateVOTestExample;
import org.fiware.agreement.model.AgreementSpecificationVO;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.tmforum.agreement.domain.AgreementSpecification;
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

@MicronautTest(packages = {"org.fiware.tmforum.agreement"})
@Property(name = "apiExtension.enabled", value = "true")
public class ExtendedAgreementSpecificationApiIT extends AbstractApiIT implements AgreementSpecificationExtensionApiTestSpec {

    private final AgreementSpecificationExtensionApiTestClient testClient;

    public ExtendedAgreementSpecificationApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                               GeneralProperties generalProperties,
                                               AgreementSpecificationExtensionApiTestClient testClient) {
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
        return AgreementSpecification.TYPE_AGREEMENT_SPECIFICATION;
    }

    private static AgreementSpecificationCreateVO buildCreateVO() {
        return AgreementSpecificationCreateVOTestExample.build().atSchemaLocation(null).serviceCategory(null);
    }

    @Test
    @Override
    public void createAgreementSpecificationWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), AgreementSpecification.TYPE_AGREEMENT_SPECIFICATION).toString();

        HttpResponse<AgreementSpecificationVO> response = callAndCatch(
                () -> testClient.createAgreementSpecificationWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "AgreementSpecification should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createAgreementSpecificationWithId400() throws Exception {
        HttpResponse<AgreementSpecificationVO> response = callAndCatch(
                () -> testClient.createAgreementSpecificationWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createAgreementSpecificationWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createAgreementSpecificationWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createAgreementSpecificationWithId405() throws Exception {
    }

    @Test
    @Override
    public void createAgreementSpecificationWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), AgreementSpecification.TYPE_AGREEMENT_SPECIFICATION).toString();

        HttpResponse<AgreementSpecificationVO> firstResponse = callAndCatch(
                () -> testClient.createAgreementSpecificationWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<AgreementSpecificationVO> secondResponse = callAndCatch(
                () -> testClient.createAgreementSpecificationWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createAgreementSpecificationWithId500() throws Exception {
    }
}
