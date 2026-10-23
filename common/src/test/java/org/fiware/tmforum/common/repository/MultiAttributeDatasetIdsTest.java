package org.fiware.tmforum.common.repository;

import org.fiware.ngsi.model.AdditionalPropertyVO;
import org.fiware.ngsi.model.EntityVO;
import org.fiware.ngsi.model.PropertyListVO;
import org.fiware.ngsi.model.PropertyVO;
import org.fiware.ngsi.model.RelationshipListVO;
import org.fiware.ngsi.model.RelationshipVO;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Scorpio 6 refuses to write an attribute with several instances if more than one has no datasetId, or if
 * datasetIds repeat. Entities written by older versions are in that state and have to be repaired before the
 * read-merge-write update writes them back - and nothing else may be touched.
 */
class MultiAttributeDatasetIdsTest {

	@Test
	public void severalInstancesWithoutDatasetIdGetAPositionalOne() {
		EntityVO entity = entityWith("relatedParty", properties(null, null));

		int repaired = MultiAttributeDatasetIds.repair(entity);

		assertEquals(2, repaired);
		assertEquals(List.of("urn:ngsi-ld:dataset:list-item:0", "urn:ngsi-ld:dataset:list-item:1"),
				datasetIds(entity, "relatedParty"));
	}

	@Test
	public void aValidMultiAttributeIsLeftExactlyAsItIs() {
		PropertyListVO valid = properties("urn:uuid:a", "urn:uuid:b", "urn:uuid:c");
		EntityVO entity = entityWith("productOfferingTerm", valid);

		int repaired = MultiAttributeDatasetIds.repair(entity);

		assertEquals(0, repaired);
		assertEquals(List.of("urn:uuid:a", "urn:uuid:b", "urn:uuid:c"), datasetIds(entity, "productOfferingTerm"));
		assertSame(valid, entity.getAdditionalProperties().get("productOfferingTerm"));
	}

	@Test
	public void oneInstanceWithoutDatasetIdIsTheDefaultInstanceAndIsAllowed() {
		EntityVO entity = entityWith("relatedParty", properties(null, "urn:uuid:b"));

		int repaired = MultiAttributeDatasetIds.repair(entity);

		assertEquals(0, repaired, "A single default instance is valid, the broker accepts it.");
		assertNull(propertyAt(entity, "relatedParty", 0).getDatasetId());
	}

	@Test
	public void aSingleInstanceNeverNeedsADatasetId() {
		EntityVO entity = entityWith("name", properties((String) null));

		assertEquals(0, MultiAttributeDatasetIds.repair(entity));
		assertNull(propertyAt(entity, "name", 0).getDatasetId());
	}

	@Test
	public void existingDatasetIdsAreKeptAndOnlyTheMissingOnesAreFilled() {
		EntityVO entity = entityWith("relatedParty", properties(null, "urn:uuid:keep", null));

		int repaired = MultiAttributeDatasetIds.repair(entity);

		assertEquals(2, repaired);
		assertEquals(List.of("urn:ngsi-ld:dataset:list-item:0", "urn:uuid:keep", "urn:ngsi-ld:dataset:list-item:2"),
				datasetIds(entity, "relatedParty"));
	}

	@Test
	public void aGeneratedIdNeverCollidesWithAnExistingOne() {
		EntityVO entity = entityWith("relatedParty",
				properties(null, "urn:ngsi-ld:dataset:list-item:0", null));

		MultiAttributeDatasetIds.repair(entity);

		List<String> ids = datasetIds(entity, "relatedParty");
		assertEquals(3, ids.stream().distinct().count(), "All ids have to be unique, was " + ids);
		assertEquals("urn:ngsi-ld:dataset:list-item:0", ids.get(1), "The existing id stays where it was.");
	}

	@Test
	public void repeatedDatasetIdsAreMadeUnique() {
		EntityVO entity = entityWith("category", properties("urn:x:same", "urn:x:same", "urn:x:other"));

		int repaired = MultiAttributeDatasetIds.repair(entity);

		assertEquals(1, repaired);
		List<String> ids = datasetIds(entity, "category");
		assertEquals("urn:x:same", ids.get(0));
		assertEquals("urn:x:other", ids.get(2));
		assertEquals(3, ids.stream().distinct().count());
	}

	@Test
	public void relationshipListsAreRepairedToo() {
		RelationshipListVO relationships = new RelationshipListVO();
		relationships.add(new RelationshipVO());
		relationships.add(new RelationshipVO());
		EntityVO entity = entityWith("category", relationships);

		assertEquals(2, MultiAttributeDatasetIds.repair(entity));
		assertEquals(URI.create("urn:ngsi-ld:dataset:list-item:1"),
				((RelationshipVO) ((RelationshipListVO) entity.getAdditionalProperties().get("category")).get(1))
						.getDatasetId());
	}

	@Test
	public void everyAttributeIsJudgedOnItsOwn() {
		EntityVO entity = new EntityVO();
		entity.setId(URI.create("urn:ngsi-ld:product-offering:1"));
		entity.setAdditionalProperties("relatedParty", properties(null, null));
		entity.setAdditionalProperties("productOfferingTerm", properties("urn:uuid:a", "urn:uuid:b"));

		assertEquals(2, MultiAttributeDatasetIds.repair(entity));
		assertEquals(List.of("urn:uuid:a", "urn:uuid:b"), datasetIds(entity, "productOfferingTerm"));
	}

	@Test
	public void anEntityWithoutAdditionalPropertiesIsFine() {
		assertEquals(0, MultiAttributeDatasetIds.repair(new EntityVO()));
	}

	private EntityVO entityWith(String attribute, AdditionalPropertyVO value) {
		EntityVO entity = new EntityVO();
		entity.setId(URI.create("urn:ngsi-ld:product-offering:1"));
		entity.setAdditionalProperties(attribute, value);
		return entity;
	}

	/** A list of properties; a null entry is an instance without datasetId. */
	private PropertyListVO properties(String... datasetIds) {
		PropertyListVO list = new PropertyListVO();
		for (String datasetId : datasetIds) {
			PropertyVO property = new PropertyVO();
			property.setValue("value");
			if (datasetId != null) {
				property.setDatasetId(URI.create(datasetId));
			}
			list.add(property);
		}
		return list;
	}

	private PropertyVO propertyAt(EntityVO entity, String attribute, int index) {
		Object value = entity.getAdditionalProperties().get(attribute);
		return ((PropertyListVO) value).get(index);
	}

	private List<String> datasetIds(EntityVO entity, String attribute) {
		return ((List<?>) entity.getAdditionalProperties().get(attribute)).stream()
				.map(instance -> instance instanceof PropertyVO p ? p.getDatasetId()
						: ((RelationshipVO) instance).getDatasetId())
				.map(id -> id == null ? null : id.toString())
				.collect(Collectors.toList());
	}
}
