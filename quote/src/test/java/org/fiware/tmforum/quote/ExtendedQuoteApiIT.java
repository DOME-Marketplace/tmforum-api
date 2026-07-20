package org.fiware.tmforum.quote;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.quote.api.ext.QuoteExtensionApiTestClient;
import org.fiware.quote.api.ext.QuoteExtensionApiTestSpec;
import org.fiware.quote.model.QuoteCreateVO;
import org.fiware.quote.model.QuoteCreateVOTestExample;
import org.fiware.quote.model.QuoteItemVOTestExample;
import org.fiware.quote.model.QuoteVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.product.Quote;
import org.fiware.tmforum.product.QuoteItemState;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest(packages = {"org.fiware.tmforum.quote"})
@Property(name = "apiExtension.enabled", value = "true")
@Property(name = "apiExtension.putEnabled", value = "true")
public class ExtendedQuoteApiIT extends AbstractApiIT implements QuoteExtensionApiTestSpec {

    private final QuoteExtensionApiTestClient testClient;

    private Clock clock = mock(Clock.class);

    @MockBean(Clock.class)
    public Clock clock() {
        return clock;
    }

    @MockBean(TMForumEventHandler.class)
    public TMForumEventHandler eventHandler() {
        TMForumEventHandler eventHandler = mock(TMForumEventHandler.class);
        when(eventHandler.handleCreateEvent(any())).thenReturn(Mono.empty());
        when(eventHandler.handleUpdateEvent(any(), any())).thenReturn(Mono.empty());
        return eventHandler;
    }

    public ExtendedQuoteApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                               GeneralProperties generalProperties,
                               QuoteExtensionApiTestClient testClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.testClient = testClient;
    }

    @Override
    protected String getEntityType() {
        return Quote.TYPE_QUOTE;
    }

    private static QuoteCreateVO buildCreateVO() {
        return QuoteCreateVOTestExample.build()
                .atSchemaLocation(null)
                .quoteItem(List.of(QuoteItemVOTestExample.build()
                        .state(QuoteItemState.IN_PROGRESS.getValue())
                        .atSchemaLocation(null)
                        .product(null)
                        .productOffering(null)
                        .productOfferingQualificationItem(null)));
    }

    @Test
    @Override
    public void createQuoteWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), Quote.TYPE_QUOTE).toString();

        HttpResponse<QuoteVO> response = callAndCatch(
                () -> testClient.createQuoteWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "Quote should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createQuoteWithId400() throws Exception {
        HttpResponse<QuoteVO> response = callAndCatch(
                () -> testClient.createQuoteWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createQuoteWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createQuoteWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createQuoteWithId405() throws Exception {
    }

    @Test
    @Override
    public void createQuoteWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), Quote.TYPE_QUOTE).toString();

        HttpResponse<QuoteVO> firstResponse = callAndCatch(
                () -> testClient.createQuoteWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<QuoteVO> secondResponse = callAndCatch(
                () -> testClient.createQuoteWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createQuoteWithId500() throws Exception {
    }
}
