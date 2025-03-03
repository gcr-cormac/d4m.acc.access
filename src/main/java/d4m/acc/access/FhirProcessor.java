package d4m.acc.access;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.hl7.fhir.Bundle;
import org.hl7.fhir.BundleEntry;
import org.hl7.fhir.FhirFactory;
import org.hl7.fhir.FhirPackage;
import org.hl7.fhir.Resource; // Adjust this import based on your actual FHIR base type
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
	
	public class FhirProcessor {
	
		private static final Logger log = LoggerFactory.getLogger(FhirProcessor.class);
		/**
		 * Recursively processes an EObject. Its EAttributes are stored in a database.
		 * Its EReferences are processed as EObjects: if a referenced EObject is a FHIR resource,
		 * its id is checked for validity and then either inserted or updated in the database.
		 *
		 * @param eObject the starting EObject
		 */

		public final String RESOURCE_TYPE = "resourceType";
		public final String BUNDLE = "Bundle";
	
		// public void processEObject(EObject eObject) {
		// 	// Store the current object's attributes.
		// 	storeAttributes(eObject);
	
		// 	// If this EObject is a FHIR resource, check its id and process accordingly.
		// 	if (eObject instanceof Resource) {
		// 		String resourceId = getResourceId(eObject);
		// 		if (isValidUUID(resourceId)) {
		// 			if (resourceExistsInDatabase(resourceId)) {
		// 				updateResourceInDatabase(eObject);
		// 			} else {
		// 				insertResourceIntoDatabase(eObject);
		// 			}
		// 		} else {
		// 			System.out.println("Invalid resource ID: " + resourceId);
		// 		}
		// 	}
	
		// 	// Recurse over all EReferences of the EObject.
		// 	for (EReference reference : eObject.eClass().getEAllReferences()) {
		// 		Object value = eObject.eGet(reference);
		// 		if (value instanceof EObject) {
		// 			processEObject((EObject) value);
		// 		} else if (value instanceof List<?>) {
		// 			for (Object item : (List<?>) value) {
		// 				if (item instanceof EObject) {
		// 					processEObject((EObject) item);
		// 				}
		// 			}
		// 		}
		// 	}
		// }

		public static org.hl7.fhir.String getResourceId(EObject eObject) {
			if (eObject instanceof org.hl7.fhir.Resource) {
				org.hl7.fhir.Resource resource = (org.hl7.fhir.Resource) eObject;
				return resource.getId();
			} else {
				return null;
			}	
		}

		public static void setResourceId(EObject eObject, String id) {
			if (eObject instanceof org.hl7.fhir.Resource) {
				org.hl7.fhir.Resource resource = (org.hl7.fhir.Resource) eObject;
				org.hl7.fhir.String fhirId = FhirFactory.eINSTANCE.createString();
				fhirId.setValue(id);
				resource.setId(fhirId);
			}
		}
		
		/**
		 * Checks whether the given string is a valid UUID.
		 */
		static boolean isValidUUID(String id) {
			if (id == null) return false;
			try {
				java.util.UUID.fromString(id);
				return true;
			} catch (Exception e) {
				log.error("", e);
				return false;
			}
		}
	
		public static EObject checkId(EObject eObject) {
			org.hl7.fhir.String id = getResourceId(eObject);
			if (!isValidUUID(id.getValue())) {
				setResourceId(eObject, UUID.randomUUID().toString());
			}
			return eObject;
		}

	public static EList<BundleEntry> getEntries(EObject eObject) {
		if (eObject instanceof Bundle) {
			Bundle bundle = (Bundle)eObject;
			return bundle.getEntry();
		} else {
			log.error("eObject is not of type Bundle.");
		}
		return null;
	}

	public Boolean isBundle(String resource) {

		String resourceType = null;
		JsonFactory factory = new JsonFactory();
		JsonParser parser;
		try {
			parser = factory.createParser(resource);

			while (!parser.isClosed()) {
				JsonToken token = parser.nextToken();
				if (token == JsonToken.FIELD_NAME && RESOURCE_TYPE.equals(parser.getCurrentName())) {
					parser.nextToken();
					resourceType = parser.getValueAsString();
					break;
				}
			}
			parser.close();
		} catch (IOException e) {
			log.error("", e);
		}
		log.info("Resource Type: " + resourceType);
		return BUNDLE.equals(resourceType);
	}
}
