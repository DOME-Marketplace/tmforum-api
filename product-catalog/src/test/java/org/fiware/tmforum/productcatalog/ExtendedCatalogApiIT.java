package org.fiware.tmforum.productcatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.productcatalog.api.ext.CatalogExtensionApiTestClient;
import org.fiware.productcatalog.api.ext.CatalogExtensionApiTestSpec;
import org.fiware.productcatalog.model.CatalogCreateVO;
import org.fiware.productcatalog.model.CatalogCreateVOTestExample;
import org.fiware.productcatalog.model.CatalogVO;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.ErrorDetails;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.notification.TMForumEventHandler;
import org.fiware.tmforum.common.test.AbstractApiIT;
import org.fiware.tmforum.productcatalog.domain.Catalog;
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

@MicronautTest(packages = {"org.fiware.tmforum.productcatalog"})
@Property(name = "apiExtension.enabled", value = "true")
@Property(name = "apiExtension.putEnabled", value = "true")
public class ExtendedCatalogApiIT extends AbstractApiIT implements CatalogExtensionApiTestSpec {

    private final CatalogExtensionApiTestClient testClient;

    public ExtendedCatalogApiIT(EntitiesApiClient entitiesApiClient, ObjectMapper objectMapper,
                                GeneralProperties generalProperties,
                                CatalogExtensionApiTestClient testClient) {
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
        return Catalog.TYPE_CATALOG;
    }

    private static CatalogCreateVO buildCreateVO() {
        return CatalogCreateVOTestExample.build().atSchemaLocation(null);
    }

    @Test
    @Override
    public void createCatalogWithId201() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), Catalog.TYPE_CATALOG).toString();

        HttpResponse<CatalogVO> response = callAndCatch(
                () -> testClient.createCatalogWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, response.getStatus(), "Catalog should have been created with the provided id.");
        assertEquals(id, response.body().getId(), "The returned id should match the provided id.");
    }

    @Test
    @Override
    public void createCatalogWithId400() throws Exception {
        HttpResponse<CatalogVO> response = callAndCatch(
                () -> testClient.createCatalogWithId(null, "invalid-id", buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A non NGSI-LD id should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Test
    public void createCatalogWithIdWrongType() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), "s").toString();

        HttpResponse<CatalogVO> response = callAndCatch(
                () -> testClient.createCatalogWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(),
                "A well-formed NGSI-LD id whose type segment does not match the entity type should be rejected.");
        Optional<ErrorDetails> optionalErrorDetails = response.getBody(ErrorDetails.class);
        assertTrue(optionalErrorDetails.isPresent(), "Error details should be provided.");
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createCatalogWithId401() throws Exception {
    }

    @Disabled("Security is handled externally.")
    @Test
    @Override
    public void createCatalogWithId403() throws Exception {
    }

    @Disabled("Prohibited by the framework.")
    @Test
    @Override
    public void createCatalogWithId405() throws Exception {
    }

    @Test
    @Override
    public void createCatalogWithId409() throws Exception {
        String id = IdHelper.toNgsiLd(UUID.randomUUID().toString(), Catalog.TYPE_CATALOG).toString();

        HttpResponse<CatalogVO> firstResponse = callAndCatch(
                () -> testClient.createCatalogWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CREATED, firstResponse.getStatus(), "First creation should succeed.");

        HttpResponse<CatalogVO> secondResponse = callAndCatch(
                () -> testClient.createCatalogWithId(null, id, buildCreateVO()));
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatus(), "Second creation with the same id should fail.");
    }

    @Override
    public void createCatalogWithId500() throws Exception {
    }
}
