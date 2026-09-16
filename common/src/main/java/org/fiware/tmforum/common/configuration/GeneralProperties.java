package org.fiware.tmforum.common.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

import java.net.URL;

/**
 * General properties to be used for the applications.
 */
@ConfigurationProperties("general")
@Data
public class GeneralProperties {

	/**
	 * ContextUrl for the service to use.
	 */
	private URL contextUrl;

	/**
	 * Base path for the controllers to be deployed at. If nothing is set, the project-individual defaults will be used
	 */
	private String basepath = "/";

	/**
	 * Root URL of the server to be used in the ngsild-subscription callbacks
	 */
	private String serverHost;

	/**
	 * Tenant to be used by the tmforum api.
	 */
	private String tenant = null;

	/**
	 * Character used in target NGSI-LD broker for making
	 * or queries in a specific value
	 */
	private String ngsildOrQueryValue=",";

	/**
	 * Character used in target NGSI-LD broker for making
	 * or queries between multiple parameters
	 */
	private String ngsildOrQueryKey=",";

	/**
	 * Whether to enclose queries using brackets or not
	 */
	private Boolean encloseQuery=true;

	/**
	 * Whether to include the attribute name in each list object or not
	 * E.g. (status==\"Active\",\"Started\") or (status==\"Active\",status==\"Started\")
	 */
	private Boolean includeAttributeInList=false;

	/**
	 * Wether to use a dot as seperator for the attribute paths in queries or not(and the ["path-part"] instead)
	 * E.g.:
	 * true: entity.attribute.subAttribute
	 * false: entity[attribute][subAttribute]
	 */
	private Boolean useDotSeperator=true;

	/**
	 * When true, updateDomainEntity uses batchEntityUpsert replace (read-merge-write) instead of PATCH /attrs.
	 * Required for Scorpio 6.x which appends to array attributes on PATCH. Default false (Orion-LD).
	 */
	private boolean replaceOnUpdate = false;

	/**
	 * Name of the response header the target NGSI-LD broker uses to report the total number of
	 * entities matching a query. Defaults to the header name defined by the NGSI-LD standard; brokers
	 * that deviate from the standard can override this property per profile. Set to null/empty to
	 * disable count reporting - in that case, pagination degrades gracefully (no X-Total-Count, no
	 * exact next/last, status always 200).
	 */
	private String countHeader = "NGSILD-Results-Count";

	/**
	 * Whether to split an AND query with an OR-value condition (e.g. {@code lifecycleStatus=Active,Launched})
	 * into a top-level NGSI-LD OR ({@code |}) of per-value branches, instead of sending the native
	 * {@code attr==(v1,v2)} valueList syntax. Required as a workaround for Scorpio 5, which does not
	 * support the valueList format combined with an AND condition. Scorpio 6 supports the valueList
	 * format directly, and should set this to false - the split triggers a separate, still-unfixed
	 * Scorpio bug where the {@code type=} URL parameter is not applied to every {@code |} branch,
	 * letting entities of other types leak into both the result and the reported total count.
	 */
	private Boolean splitOrValues = true;
}
