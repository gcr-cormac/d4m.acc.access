package d4m.acc.access;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.hl7.fhir.Bundle;
import org.hl7.fhir.BundleEntry;
import org.hl7.fhir.FhirFactory;
import org.hl7.fhir.ResourceContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class FhirProcessor {

	private static final Logger log = LoggerFactory.getLogger(FhirProcessor.class);
	/**
	 * Recursively processes an EObject. Its EAttributes are stored in a database.
	 * Its EReferences are processed as EObjects: if a referenced EObject is a FHIR
	 * resource, its id is checked for validity and then either inserted or updated
	 * in the database.
	 *
	 * @param eObject the starting EObject
	 */


	// public void processEObject(EObject eObject) {
	// // Store the current object's attributes.
	// storeAttributes(eObject);

	// // If this EObject is a FHIR resource, check its id and process accordingly.
	// if (eObject instanceof Resource) {
	// String resourceId = getResourceId(eObject);
	// if (isValidUUID(resourceId)) {
	// if (resourceExistsInDatabase(resourceId)) {
	// updateResourceInDatabase(eObject);
	// } else {
	// insertResourceIntoDatabase(eObject);
	// }
	// } else {
	// System.out.println("Invalid resource ID: " + resourceId);
	// }
	// }

	// // Recurse over all EReferences of the EObject.
	// for (EReference reference : eObject.eClass().getEAllReferences()) {
	// Object value = eObject.eGet(reference);
	// if (value instanceof EObject) {
	// processEObject((EObject) value);
	// } else if (value instanceof List<?>) {
	// for (Object item : (List<?>) value) {
	// if (item instanceof EObject) {
	// processEObject((EObject) item);
	// }
	// }
	// }
	// }
	// }
	
	/**
	 * Extracts the actual FHIR resource from a ResourceContainer.
	 * Uses EMF reflection to find the non-null getter method.
	 */
	public static EObject extractFHIRResource(BundleEntry entry) {
		ResourceContainer resourceContainer = (ResourceContainer)entry.eContents().get(0);
        log.trace("resourceContainer=" + resourceContainer);
	    for (EReference eReference : resourceContainer.eClass().getEAllReferences()) {
	        log.trace("trace=" + eReference.getName());
	        Object value = resourceContainer.eGet(eReference);
	        log.trace("value=" + value);
	        if (value instanceof EObject) {
	            return (EObject) value;
	        }
	    }
	    throw new IllegalArgumentException("No FHIR resource found in ResourceContainer");
	}
	
	public static EObject getFHIRResource(BundleEntry bundleEntry) {
	    if (bundleEntry == null) {
	        throw new IllegalArgumentException("BundleEntry cannot be null.");
	    }

	    // Step 1: Get the 'resource' feature from BundleEntry
	    EStructuralFeature resourceFeature = bundleEntry.eClass().getEStructuralFeature("resource");
	    if (resourceFeature == null) {
	        throw new IllegalStateException("Feature 'resource' not found in BundleEntry.");
	    }

	    // Get the ResourceContainer (which holds the actual FHIR resource)
	    EObject resourceContainer = (EObject) bundleEntry.eGet(resourceFeature);
	    if (resourceContainer == null) {
	        System.out.println("BundleEntry contains no resource.");
	        return null;
	    }
	    
	    resourceContainer.eClass().getEStructuralFeature("resourceType");
	    String resourceType = (String) resourceContainer.eGet(resourceFeature);
	    if (resourceType == null || resourceType.isEmpty()) {
	        System.out.println("ResourceContainer has no resourceType.");
	        return null;
	    }

	    // Step 3: Use 'resourceType' to find the correct feature inside ResourceContainer
	    EStructuralFeature correctFeature = resourceContainer.eClass().getEStructuralFeature(resourceType);
	    if (correctFeature == null) {
	        System.out.println("No matching feature for resourceType: " + resourceType);
	        return null;
	    }

	    // Step 4: Retrieve and return the correct FHIR resource as EObject
	    EObject fhirResource = (EObject) resourceContainer.eGet(correctFeature);
	    if (fhirResource != null) {
	        System.out.println("Extracted FHIR resource of type: " + fhirResource.eClass().getName());
	    } else {
	        System.out.println("No resource found for resourceType: " + resourceType);
	    }

	    return fhirResource;
	}


	
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
    static boolean isValidUUID(java.lang.String id) {
        if (id == null) {
            return false;
        }
        try {
            UUID.fromString(id);
            return true;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format: {}", id);
            return false;
        }
    }

	static boolean isValidUUID(org.hl7.fhir.String id) {
		if (id == null) {
			return false;
		} else {
			return isValidUUID(id.getValue());
		}
	}

	public static EObject checkId(EObject eObject) {
		org.hl7.fhir.String id = getResourceId(eObject);
		if (!isValidUUID(id)) {
			setResourceId(eObject, UUID.randomUUID().toString());
		}
		return eObject;
	}

	public static EList<BundleEntry> getEntries(EObject eObject) {
	    if (eObject instanceof Bundle) {
	        return ((Bundle) eObject).getEntry();
	    } else {
	        log.error("eObject is not of type Bundle: " + eObject.eClass().getName());
	        return ECollections.emptyEList(); // Return an empty list instead of null
	    }
	}


	public static Boolean isBundle(String resource) {

		String resourceType = null;
		JsonFactory factory = new JsonFactory();
		JsonParser parser;
		try {
			parser = factory.createParser(resource);

			while (!parser.isClosed()) {
				JsonToken token = parser.nextToken();
				if (token == JsonToken.FIELD_NAME && AccumuloFinals.RESOURCE_TYPE.equals(parser.getCurrentName())) {
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
		return AccumuloFinals.BUNDLE.equals(resourceType);
	}

	public static String toFhirPath(EAttribute eAttribute, EObject eObject) {
//		EClass eClass = eObject.eClass();
		// Let us be sure we have a BundleEntry
		if (eObject instanceof BundleEntry) {
			BundleEntry bundleEntry = (BundleEntry) eObject;
			// A BundleEntry has in it a ResourceContainer.
			ResourceContainer resourceContainer = bundleEntry.getResource();
			// A BundleEntry has in it a StructuralFeature named "resource".
			// We extract the resourceFeature and use it to get a resourceEObject.
			EStructuralFeature resourceFeature = bundleEntry.eClass().getEStructuralFeature("resource");
			EObject resourceEObject = (EObject) resourceContainer.eGet(resourceFeature);
			// resourceEObject has in it a resourceTypeFeature.
			EStructuralFeature resourceTypeFeature = resourceEObject.eClass().getEStructuralFeature("resourceType");
			EObject resourceTypeEObject = (EObject) resourceEObject.eGet(resourceTypeFeature);
			Object resourceTypeValue = resourceEObject.eGet(resourceTypeFeature);
			String resourceName = (String) resourceTypeValue;
			return String.format("%s.%s", resourceName, eAttribute.getName());
		}
		return null;
	}

	public String extraceResourceType(EObject resourceEObject) {
		EStructuralFeature resourceTypeFeature = resourceEObject.eClass().getEStructuralFeature("resourceType");
		Object resourceTypeValue = resourceEObject.eGet(resourceTypeFeature);
		String resourceName = (String) resourceTypeValue;
		return resourceName;
	}
}