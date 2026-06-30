package org.fiware.tmforum.quote;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.quote.api.ext.QuoteExtensionApiTestClient;
import org.fiware.quote.model.QuoteCreateVO;
import org.fiware.quote.model.QuoteCreateVOTestExample;
import org.fiware.quote.model.QuoteItemVOTestExample;
import org.fiware.quote.model.QuoteVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.product.Quote;
import org.fiware.tmforum.product.QuoteItemState;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@MicronautTest(packages = {"org.fiware.tmforum.quote"})
public class DisabledQuoteExtensionApiIT extends AbstractApiIT {

    private final QuoteExtensionApiTestClient quoteExtensionApiTestClient;

    @MockBean(Clock.class)
    public Clock clock() {
        return mock(Clock.class);
    }

    protected DisabledQuoteExtensionApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                          GeneralProperties generalProperties,
                                          QuoteExtensionApiTestClient quoteExtensionApiTestClient) {
        super(entitiesApiClient, objectMapper, generalProperties);
        this.quoteExtensionApiTestClient = quoteExtensionApiTestClient;
    }

    @Override
    protected String getEntityType() {
        return Quote.TYPE_QUOTE;
    }

    @Test
    public void createQuoteWithId405() throws Exception {
        QuoteCreateVO createVO = QuoteCreateVOTestExample.build()
                .atSchemaLocation(null)
                .quoteItem(List.of(QuoteItemVOTestExample.build()
                        .state(QuoteItemState.IN_PROGRESS.getValue())
                        .atSchemaLocation(null)
                        .product(null)
                        .productOffering(null)
                        .productOfferingQualificationItem(null)));
        HttpResponse<QuoteVO> response = callAndCatch(
                () -> quoteExtensionApiTestClient.createQuoteWithId(null, "urn:ngsi-ld:quote:test", createVO));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus(),
                "When apiExtension is not enabled, PUT with custom id should not be supported.");
    }
}
