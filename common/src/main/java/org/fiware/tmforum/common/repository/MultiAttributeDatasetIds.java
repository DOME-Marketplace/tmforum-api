package org.fiware.tmforum.common.repository;

import lombok.extern.slf4j.Slf4j;
import org.fiware.ngsi.model.AdditionalPropertyVO;
import org.fiware.ngsi.model.EntityVO;
import org.fiware.ngsi.model.GeoPropertyVO;
import org.fiware.ngsi.model.PropertyVO;
import org.fiware.ngsi.model.RelationshipVO;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Brings the multi-attributes (lists) of an entity into a state the broker accepts on write.
 * <p>
 * NGSI-LD identifies the instances of an attribute with a {@code datasetId}. At most one instance may go
 * without it, and the ids must be unique. Scorpio before 6.0 did not check this, and older versions of the
 * mapping did not set a {@code datasetId} on every list - so entities written back then (for instance the
 * {@code relatedParty} of offerings, or {@code productSpecCharacteristic_ext}) carry several instances without
 * one. Scorpio 6 still returns them on read, but refuses to write them back
 * ("Duplicated datasetId or multiple entries with no datasetId found"), which makes every update of such an
 * entity fail on the read-merge-write path.
 * <p>
 * Only attributes that would be refused are touched: a valid multi-attribute is left exactly as it is.
 */
@Slf4j
final class MultiAttributeDatasetIds {

	/**
	 * Same scheme the mapping library uses for list items without an identity of their own.
	 */
	static final String LIST_ITEM_PREFIX = "urn:ngsi-ld:dataset:list-item:";

	private MultiAttributeDatasetIds() {
	}

	/**
	 * Assign a datasetId to the instances of every invalid multi-attribute of the entity.
	 *
	 * @param entityVO the entity that is about to be written, modified in place
	 * @return the number of instances that got a datasetId
	 */
	static int repair(EntityVO entityVO) {
		Map<String, AdditionalPropertyVO> attributes = entityVO.getAdditionalProperties();
		if (attributes == null || attributes.isEmpty()) {
			return 0;
		}
		int repaired = 0;
		for (Map.Entry<String, AdditionalPropertyVO> attribute : attributes.entrySet()) {
			if (attribute.getValue() instanceof List<?> instances && instances.size() > 1) {
				int assigned = repairAttribute(instances);
				if (assigned > 0) {
					log.info("Assigned a datasetId to {} instance(s) of attribute {} of entity {}.",
							assigned, attribute.getKey(), entityVO.getId());
				}
				repaired += assigned;
			}
		}
		return repaired;
	}

	private static int repairAttribute(List<?> instances) {
		Set<URI> taken = new HashSet<>();
		int withoutDatasetId = 0;
		boolean duplicated = false;
		for (Object instance : instances) {
			URI datasetId = datasetIdOf(instance);
			if (datasetId == null) {
				withoutDatasetId++;
			} else if (!taken.add(datasetId)) {
				duplicated = true;
			}
		}
		if (withoutDatasetId <= 1 && !duplicated) {
			// valid as it is - at most one default instance and no repeated ids
			return 0;
		}
		int assigned = 0;
		Set<URI> seen = new HashSet<>();
		for (int i = 0; i < instances.size(); i++) {
			Object instance = instances.get(i);
			URI datasetId = datasetIdOf(instance);
			if (datasetId != null && seen.add(datasetId)) {
				continue;
			}
			URI generated = nextFree(i, taken);
			if (setDatasetIdOf(instance, generated)) {
				taken.add(generated);
				seen.add(generated);
				assigned++;
			}
		}
		return assigned;
	}

	/**
	 * Positional id for the instance, moving on if it is already used by another instance.
	 */
	private static URI nextFree(int index, Set<URI> taken) {
		int candidate = index;
		URI datasetId = URI.create(LIST_ITEM_PREFIX + candidate);
		while (taken.contains(datasetId)) {
			candidate++;
			datasetId = URI.create(LIST_ITEM_PREFIX + candidate);
		}
		return datasetId;
	}

	private static URI datasetIdOf(Object instance) {
		if (instance instanceof PropertyVO propertyVO) {
			return propertyVO.getDatasetId();
		}
		if (instance instanceof RelationshipVO relationshipVO) {
			return relationshipVO.getDatasetId();
		}
		if (instance instanceof GeoPropertyVO geoPropertyVO) {
			return geoPropertyVO.getDatasetId();
		}
		return null;
	}

	private static boolean setDatasetIdOf(Object instance, URI datasetId) {
		if (instance instanceof PropertyVO propertyVO) {
			propertyVO.setDatasetId(datasetId);
			return true;
		}
		if (instance instanceof RelationshipVO relationshipVO) {
			relationshipVO.setDatasetId(datasetId);
			return true;
		}
		if (instance instanceof GeoPropertyVO geoPropertyVO) {
			geoPropertyVO.setDatasetId(datasetId);
			return true;
		}
		return false;
	}
}
