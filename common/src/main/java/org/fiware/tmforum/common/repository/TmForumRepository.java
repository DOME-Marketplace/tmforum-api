package org.fiware.tmforum.common.repository;

import io.github.wistefan.mapping.EntityVOMapper;
import io.github.wistefan.mapping.JavaObjectMapper;
import io.micronaut.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.ngsi.api.SubscriptionsApiClient;
import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.fiware.tmforum.common.exception.TmForumException;
import org.fiware.tmforum.common.exception.TmForumExceptionReason;
import org.fiware.tmforum.common.mapping.IdHelper;
import org.fiware.tmforum.common.mapping.NGSIMapper;
import reactor.core.publisher.Mono;

import javax.inject.Singleton;
import java.net.URI;
import java.util.function.Function;

@Slf4j
@Singleton
public class TmForumRepository extends NgsiLdBaseRepository {

    public TmForumRepository(GeneralProperties generalProperties, EntitiesApiClient entitiesApi,
                             SubscriptionsApiClient subscriptionsApi, EntityVOMapper entityVOMapper,
                             NGSIMapper ngsiMapper, JavaObjectMapper javaObjectMapper) {
        super(generalProperties, entitiesApi, subscriptionsApi, javaObjectMapper, ngsiMapper, entityVOMapper);
    }

    public <T> Mono<T> get(URI id, Class<T> entityClass) {

        if (!IdHelper.isNgsiLdId(id.toString(), entityClass)) {
            log.warn("Entity {} does not have the type expected for {}", id, entityClass.getName());
            return Mono.empty();
        }

        return retrieveEntityById(id)
                .flatMap(entityVO -> entityVOMapper.fromEntityVO(entityVO, entityClass));
    }

    public <T> Mono<PagedResult<T>> findEntities(Integer offset, Integer limit, Class<T> entityClass,
                                          String query, String ids, String types, String orderBy) {
        return entitiesApi.queryEntities(generalProperties.getTenant(),
                        ids,
                        null,
                        types,
                        null,
                        query,
                        orderBy,
                        null,
                        null,
                        null,
                        null,
                        null,
                        limit,
                        offset,
                        true,
                        null,
                        getLinkHeader())
                .flatMap(response -> zipToList(response.body().stream(), entityClass)
                        .map(entities -> new PagedResult<>(entities, offset, limit, extractTotalCount(response))))
                .onErrorResume(t -> {
                    log.warn("Was not able to list entities.", t);
                    throw new TmForumException("Was not able to list entities.", t, TmForumExceptionReason.UNKNOWN);
                });
    }


    public <T> Mono<PagedResult<T>> findEntities(Integer offset, Integer limit, String entityType, Class<T> entityClass,
                                          String query) {
        return findEntities(offset, limit, entityClass, query, null, entityType, null);
    }

    /**
     * Query several NGSI-LD entity types in a single broker call (NGSI-LD's {@code type} parameter
     * accepts a comma-separated list with OR semantics), instead of one call per type. This lets the
     * broker apply {@code offset}/{@code limit} and report the count against the combined result set,
     * rather than callers fanning out per-type queries and merging paginated slices themselves.
     * <p>
     * Since the result is a mix of entity types, each entity is mapped to its own domain class via
     * {@code typeToClass} instead of a single fixed class.
     */
    public <T> Mono<PagedResult<T>> findEntitiesPolymorphic(Integer offset, Integer limit, String types,
                                          String query, String ids, String orderBy, Function<String, Class<? extends T>> typeToClass) {
        return entitiesApi.queryEntities(generalProperties.getTenant(),
                        ids,
                        null,
                        types,
                        null,
                        query,
                        orderBy,
                        null,
                        null,
                        null,
                        null,
                        null,
                        limit,
                        offset,
                        true,
                        null,
                        getLinkHeader())
                .flatMap(response -> zipToPolymorphicList(response.body().stream(), typeToClass)
                        .map(entities -> new PagedResult<>(entities, offset, limit, extractTotalCount(response))))
                .onErrorResume(t -> {
                    log.warn("Was not able to list entities.", t);
                    throw new TmForumException("Was not able to list entities.", t, TmForumExceptionReason.UNKNOWN);
                });
    }

    Integer extractTotalCount(HttpResponse<?> response) {
        String headerName = generalProperties.getCountHeader();
        if (headerName == null) {
            return null;
        }
        String headerValue = response.header(headerName);
        if (headerValue == null) {
            return null;
        }
        try {
            return Integer.parseInt(headerValue);
        } catch (NumberFormatException e) {
            log.warn("Total count header {} did not contain a valid integer: {}", headerName, headerValue);
            return null;
        }
    }

}
