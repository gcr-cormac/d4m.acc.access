package d4m.acc.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.hl7.fhir.FhirFactory;
import org.hl7.fhir.emf.FHIRSDS;
import org.hl7.fhir.emf.Finals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FhirProcessorTest {

//	private static final Logger log = LoggerFactory.getLogger(FhirProcessorTest.class);
	
	static FhirProcessor app;
	static EObject eObject;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		app = new FhirProcessor();
		InputStream is = FhirProcessorTest.class.getResourceAsStream("/fhir_adverse_event.0.json");
		assertNotNull(is);
		eObject = FHIRSDS.load(is, Finals.SDS_FORMAT.JSON);
		assertNotNull(eObject);
	}

//	@Test
	void testProcessEObject() {
		fail("Not yet implemented");
	}

	@Test
	void testGetResourceId() {
		org.hl7.fhir.String id = FhirProcessor.getResourceId(eObject);
		assertNull(id);
	}

	@Test
	void testSetResourceId() {
		String id = "testId";
		org.hl7.fhir.String fhirId = FhirFactory.eINSTANCE.createString();
		fhirId.setValue(id);
		FhirProcessor.setResourceId(eObject, id);
		org.hl7.fhir.String id1 = FhirProcessor.getResourceId(eObject);
		assertNotNull(id1);
		assertEquals(fhirId.getValue(), id1.getValue());
	}

	@Test
	void testIsValidUUID() {
		String id = "testId";
		assertFalse(FhirProcessor.isValidUUID(id));
		String id1 = "1";
		assertFalse(FhirProcessor.isValidUUID(id1));
		String id2 = UUID.randomUUID().toString();
		assertTrue(FhirProcessor.isValidUUID(id2));
	}

	@Test
	void testCheckId() {
		String id = "testId";
		assertFalse(FhirProcessor.isValidUUID(id));
		FhirProcessor.setResourceId(eObject, id);
		FhirProcessor.checkId(eObject);
		String id1 = FhirProcessor.getResourceId(eObject).getValue();
		assertTrue(FhirProcessor.isValidUUID(id1));
	}

	@Test
	void testGetEntries() {
		Object o = FhirProcessor.getEntries(eObject);
		assertNotNull(o);
		assertTrue(o instanceof EList);
	}

	@Test
	void testIsBundle() {
		OutputStream os = FHIRSDS.save(eObject, Finals.SDS_FORMAT.JSON);
		assertTrue(os instanceof ByteArrayOutputStream);
		assertTrue(app.isBundle(((ByteArrayOutputStream)os).toString()));
	}
	
	@Test
	void testToFhirPath() {
		Object o = FhirProcessor.getEntries(eObject);
		EList<EObject> eList = (EList)o;
		EObject eObject = eList.get(0);
		EClass eClass = eObject.eClass();
		EAttribute eAttribute = eClass.getEAllAttributes().get(0);
		String path = FhirProcessor.toFhirPath(eAttribute, eObject);
		System.out.println("path=" + path);
		assertEquals("AdverseEvent.resourceType", path);
	}
}
