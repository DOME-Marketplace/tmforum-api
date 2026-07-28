package org.fiware.tmforum.common.querying;

import org.fiware.tmforum.common.configuration.GeneralProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class QueryParserTest {

	@ParameterizedTest
	@MethodSource("queriesAttributeIncluded")
	public void testQueryParsingAttributeIncluded(String tmForumQuery, QueryParams ngsiLdQuery, Class<?> targetClass) {
		GeneralProperties properties = new GeneralProperties();
		properties.setEncloseQuery(true);
		properties.setNgsildOrQueryKey("|");
		properties.setNgsildOrQueryValue("|");
		properties.setIncludeAttributeInList(true);
		properties.setUseDotSeperator(false);

		QueryParser qp = new QueryParser(properties);
		assertEquals(ngsiLdQuery, qp.toNgsiLdQuery(targetClass, tmForumQuery),
				"The query should have been properly translated.");
	}

	@ParameterizedTest
	@MethodSource("queriesAttributeNotIncluded")
	public void testQueryParsingAttributeNotIncluded(String tmForumQuery, QueryParams ngsiLdQuery, Class<?> targetClass) {
		GeneralProperties properties = new GeneralProperties();
		properties.setEncloseQuery(true);
		properties.setNgsildOrQueryKey("|");
		properties.setNgsildOrQueryValue("|");
		properties.setIncludeAttributeInList(false);
		properties.setUseDotSeperator(false);

		QueryParser qp = new QueryParser(properties);
		assertEquals(ngsiLdQuery, qp.toNgsiLdQuery(targetClass, tmForumQuery),
				"The query should have been properly translated.");
	}

	@ParameterizedTest
	@MethodSource("queriesAttributesWithDotPath")
	public void testQueryParsingWithDotPath(String tmForumQuery, QueryParams ngsiLdQuery, Class<?> targetClass) {
		GeneralProperties properties = new GeneralProperties();
		properties.setEncloseQuery(true);
		properties.setNgsildOrQueryKey("|");
		properties.setNgsildOrQueryValue("|");
		properties.setIncludeAttributeInList(false);
		properties.setUseDotSeperator(true);

		QueryParser qp = new QueryParser(properties);
		assertEquals(ngsiLdQuery, qp.toNgsiLdQuery(targetClass, tmForumQuery),
				"The query should have been properly translated.");
	}

	private static Stream<Arguments> queriesAttributeIncluded() {
		return Stream.of(
				// Property attributes queries
				Arguments.of("status=Active,Started&color=Red", new QueryParams(null, null, "color==\"Red\";status==\"Active\"|color==\"Red\";status==\"Started\"", Map.of()), MyPojo.class),
				Arguments.of("status=Active,Started;color=Red", new QueryParams(null, null, "color==\"Red\"|(status==\"Active\"|status==\"Started\")", Map.of()), MyPojo.class),
				Arguments.of("status=Active;status=Started", new QueryParams(null, null, "(status==\"Active\"|status==\"Started\")", Map.of()), MyPojo.class),
				Arguments.of("status=Active;status=Started;color=Red", new QueryParams(null, null, "color==\"Red\"|(status==\"Active\"|status==\"Started\")", Map.of()),
						MyPojo.class),
				Arguments.of("sub.status=Active;status=Started;color=Red",
						new QueryParams(null, null, "color==\"Red\"|sub[status]==\"Active\"|status==\"Started\"", Map.of()), MyPojo.class),
				Arguments.of("sub.status=Active;otherNamedSub.status=Started;color=Red",
						new QueryParams(null, null, "color==\"Red\"|otherSub[status]==\"Started\"|sub[status]==\"Active\"", Map.of()), MyPojo.class),
				Arguments.of("temperature<20&temperature>10", new QueryParams(null, null, "temperature<20;temperature>10", Map.of()), MyPojo.class),
				Arguments.of("temperature<=20;temperature=30", new QueryParams(null, null, "temperature==30|temperature<=20", Map.of()), MyPojo.class),
				Arguments.of("temperature>=20;temperature<3", new QueryParams(null, null, "temperature<3|temperature>=20", Map.of()), MyPojo.class),
				Arguments.of("status.eq=Active,Started&color.eq=Red", new QueryParams(null, null, "color==\"Red\";status==\"Active\"|color==\"Red\";status==\"Started\"", Map.of()),
						MyPojo.class),
				Arguments.of("status.eq=Active,Started;color.eq=Red", new QueryParams(null, null, "color==\"Red\"|(status==\"Active\"|status==\"Started\")", Map.of()),
						MyPojo.class),
				Arguments.of("status.eq=Active;status.eq=Started", new QueryParams(null, null, "(status==\"Active\"|status==\"Started\")", Map.of()), MyPojo.class),
				Arguments.of("status.eq=Active;status.eq=Started;color.eq=Red", new QueryParams(null, null, "color==\"Red\"|(status==\"Active\"|status==\"Started\")", Map.of()),
						MyPojo.class),
				Arguments.of("sub.status.eq=Active;status.eq=Started;color.eq=Red",
						new QueryParams(null, null, "color==\"Red\"|sub[status]==\"Active\"|status==\"Started\"", Map.of()), MyPojo.class),
				Arguments.of("sub.status.eq=Active;otherNamedSub.status.eq=Started;color.eq=Red",
						new QueryParams(null, null, "color==\"Red\"|otherSub[status]==\"Started\"|sub[status]==\"Active\"", Map.of()), MyPojo.class),
				Arguments.of("temperature.lt=20&temperature.gt=10", new QueryParams(null, null, "temperature<20;temperature>10", Map.of()), MyPojo.class),
				Arguments.of("temperature.lte=20;temperature.eq=30", new QueryParams(null, null, "temperature==30|temperature<=20", Map.of()), MyPojo.class),
				Arguments.of("temperature.gte=20;temperature.lt=3", new QueryParams(null, null, "temperature<3|temperature>=20", Map.of()), MyPojo.class),

				// Relationship attributes queries
				Arguments.of("rel.name=therel", new QueryParams(null, null, "rel.name==\"therel\"", Map.of()), MyPojo.class),
				Arguments.of("relList.name=therel", new QueryParams(null, null, "relList.name==\"therel\"", Map.of()), MyPojo.class),

				// Id queries
				Arguments.of("id=urn:ngsi-ld:service:c2016f17-997d-468a-be23-7657bc5b4c5b,urn:ngsi-ld:service:u2096f17-997d-468a-be23-7657bc5b4c67", new QueryParams("urn:ngsi-ld:service:c2016f17-997d-468a-be23-7657bc5b4c5b,urn:ngsi-ld:service:u2096f17-997d-468a-be23-7657bc5b4c67", null, null, Map.of()), MyPojo.class)
		);
	}

	private static Stream<Arguments> queriesAttributeNotIncluded() {
		return Stream.of(
				// Property attributes queries
				Arguments.of("status=Active,Started&color=Red", new QueryParams(null, null, "color==\"Red\";status==\"Active\"|color==\"Red\";status==\"Started\"", Map.of()), MyPojo.class),
				Arguments.of("status=Active,Started;color=Red", new QueryParams(null, null, "color==\"Red\"|status==(\"Active\"|\"Started\")", Map.of()), MyPojo.class),
				Arguments.of("status=Active;status=Started", new QueryParams(null, null, "status==(\"Active\"|\"Started\")", Map.of()), MyPojo.class),
				Arguments.of("status=Active;status=Started;color=Red", new QueryParams(null, null, "color==\"Red\"|status==(\"Active\"|\"Started\")", Map.of()),
						MyPojo.class),
				Arguments.of("sub.status=Active;status=Started;color=Red",
						new QueryParams(null, null, "color==\"Red\"|sub[status]==\"Active\"|status==\"Started\"", Map.of()), MyPojo.class),
				Arguments.of("sub.status=Active;otherNamedSub.status=Started;color=Red",
						new QueryParams(null, null, "color==\"Red\"|otherSub[status]==\"Started\"|sub[status]==\"Active\"", Map.of()), MyPojo.class),
				Arguments.of("temperature<20&temperature>10", new QueryParams(null, null, "temperature<20;temperature>10", Map.of()), MyPojo.class),
				Arguments.of("temperature<=20;temperature=30", new QueryParams(null, null, "temperature==30|temperature<=20", Map.of()), MyPojo.class),
				Arguments.of("temperature>=20;temperature<3", new QueryParams(null, null, "temperature<3|temperature>=20", Map.of()), MyPojo.class),
				Arguments.of("status.eq=Active,Started&color.eq=Red", new QueryParams(null, null, "color==\"Red\";status==\"Active\"|color==\"Red\";status==\"Started\"", Map.of()),
						MyPojo.class),
				Arguments.of("status.eq=Active,Started;color.eq=Red", new QueryParams(null, null, "color==\"Red\"|status==(\"Active\"|\"Started\")", Map.of()),
						MyPojo.class),
				Arguments.of("status.eq=Active;status.eq=Started", new QueryParams(null, null, "status==(\"Active\"|\"Started\")", Map.of()), MyPojo.class),
				Arguments.of("status.eq=Active;status.eq=Started;color.eq=Red", new QueryParams(null, null, "color==\"Red\"|status==(\"Active\"|\"Started\")", Map.of()),
						MyPojo.class)
		);
	}


	private static Stream<Arguments> queriesAttributesWithDotPath() {
		return Stream.of(
				// Property attributes queries
				Arguments.of("sub.status=Active;status=Started;color=Red",
						new QueryParams(null, null, "color==\"Red\"|sub.status==\"Active\"|status==\"Started\"", Map.of()), MyPojo.class),
				Arguments.of("sub.status=Active;otherNamedSub.status=Started;color=Red",
						new QueryParams(null, null, "color==\"Red\"|otherSub.status==\"Started\"|sub.status==\"Active\"", Map.of()), MyPojo.class),
				Arguments.of("sub.status=Active;relatedParty.role=Owner",
						new QueryParams(null, null, "relatedParty.role==\"Owner\"|sub.status==\"Active\"", Map.of()), MyPojo.class)
		);
	}

	@ParameterizedTest
	@MethodSource("scorpioQueries")
	public void testScorpioQueryParsing(String tmForumQuery, QueryParams ngsiLdQuery, Class<?> targetClass) {
		GeneralProperties properties = new GeneralProperties();
		properties.setNgsildOrQueryKey(",");
		properties.setNgsildOrQueryValue(",");
		properties.setEncloseQuery(true);
		properties.setIncludeAttributeInList(false);
		properties.setUseDotSeperator(false);

		QueryParser qp = new QueryParser(properties);
		assertEquals(ngsiLdQuery, qp.toNgsiLdQuery(targetClass, tmForumQuery),
				"The query should have been properly translated.");
	}

	private static Stream<Arguments> scorpioQueries() {
		return Stream.of(
				// AND query with OR values: distributed as (base;attr==v1)|(base;attr==v2)
				Arguments.of("status=Active,Started&color=Red", new QueryParams(null, null, "color==\"Red\";status==\"Active\"|color==\"Red\";status==\"Started\"", Map.of()), MyPojo.class),
				// OR query (TMForum ;): combined via combineParts, still uses valueList format
				Arguments.of("status=Active;status=Started", new QueryParams(null, null, "status==(\"Active\",\"Started\")", Map.of()), MyPojo.class),
				// AND query without OR values: unchanged
				Arguments.of("sub.status=Active&status=Started&color=Red", new QueryParams(null, null, "sub[status]==\"Active\";status==\"Started\";color==\"Red\"", Map.of()), MyPojo.class),
				Arguments.of("temperature<20&temperature>10", new QueryParams(null, null, "temperature<20;temperature>10", Map.of()), MyPojo.class),
				// Same as first two but using .eq= syntax
				Arguments.of("status.eq=Active,Started&color.eq=Red", new QueryParams(null, null, "color==\"Red\";status==\"Active\"|color==\"Red\";status==\"Started\"", Map.of()), MyPojo.class),
				Arguments.of("status.eq=Active;status.eq=Started", new QueryParams(null, null, "status==(\"Active\",\"Started\")", Map.of()), MyPojo.class)
		);
	}

	/**
	 * Verifies the translation of the TMForum {@code sort} query parameter (comma-separated
	 * properties, "-" prefix for descending) into NGSI-LD's {@code orderBy} syntax
	 * (comma-separated "property;direction" pairs, direction omitted meaning ascending).
	 */
	@ParameterizedTest
	@MethodSource("sortToOrderByQueries")
	public void testSortToOrderByTranslation(Map<String, List<String>> parameters, String expectedOrderBy,
			Class<?> targetClass) {
		GeneralProperties properties = new GeneralProperties();
		properties.setUseDotSeperator(true);

		QueryParser qp = new QueryParser(properties);
		assertEquals(expectedOrderBy, qp.toOrderBy(targetClass, parameters),
				"The sort parameter should have been properly translated to orderBy.");
	}

	private static Stream<Arguments> sortToOrderByQueries() {
		return Stream.of(
				// no sort requested at all
				Arguments.of(Map.of(), null, MyPojo.class),
				// single ascending field, no direction suffix needed
				Arguments.of(Map.of(QueryParser.SORT_KEY, List.of("color")), "color", MyPojo.class),
				// single descending field
				Arguments.of(Map.of(QueryParser.SORT_KEY, List.of("-color")), "color;desc", MyPojo.class),
				// mixed ascending/descending, comma-separated
				Arguments.of(Map.of(QueryParser.SORT_KEY, List.of("color,-temperature")), "color,temperature;desc", MyPojo.class),
				Arguments.of(Map.of(QueryParser.SORT_KEY, List.of("-color,-temperature")), "color;desc,temperature;desc", MyPojo.class),
				// nested attribute path
				Arguments.of(Map.of(QueryParser.SORT_KEY, List.of("-sub.status")), "sub.status;desc", MyPojo.class),
				// JSON-LD reserved token translation, same as filtering
				Arguments.of(Map.of(QueryParser.SORT_KEY, List.of("-@type")), "atType;desc", MyEntityPojo.class)
		);
	}

	@Test
	public void testSortToOrderByReturnsNullWhenSortValueIsBlank() {
		GeneralProperties properties = new GeneralProperties();
		QueryParser qp = new QueryParser(properties);
		assertNull(qp.toOrderBy(MyPojo.class, Map.of(QueryParser.SORT_KEY, List.of(""))),
				"A blank sort value should not produce an orderBy.");
	}
}