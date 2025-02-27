package d4m.acc.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.hl7.fhir.emf.FHIRSDS;
import org.hl7.fhir.emf.Finals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FhirProcessorTest {

	static FhirProcessor app;
	static String resource;
	static EObject eObject;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		app = new FhirProcessor();
		InputStream is = FhirProcessorTest.class.getResourceAsStream("/fhir_adverse_event.0.json");
		assertNotNull(is);
		eObject = FHIRSDS.load(is, Finals.SDS_FORMAT.JSON);
		assertNotNull(eObject);
	}

	@Test
	void testProcessEObject() {
		fail("Not yet implemented");
	}

	@Test
	void testGetResourceId() {
		String id = app.getResourceId(eObject);
		assertNotNull(id);
	}

	@Test
	void testSetResourceId() {
		String id = "testId";
		app.setResourceId(eObject, id);
		String id1 = app.getResourceId(eObject);
		assertNotNull(id1);
		assertEquals(id, id1);
	}

	@Test
	void testIsValidUUID() {
		String id = "testId";
		assertFalse(app.isValidUUID(id));
		String id1 = "1";
		assertFalse(app.isValidUUID(id1));
		String id2 = UUID.randomUUID().toString();
		assertTrue(app.isValidUUID(id2));
	}

	@Test
	void testCheckId() {
		String id = "testId";
		assertFalse(app.isValidUUID(id));
		app.setResourceId(eObject, id);
		app.checkId(eObject);
		String id1 = app.getResourceId(eObject);
		assertTrue(app.isValidUUID(id1));
	}

	@Test
	void testGetEntries() {
		Object o = app.getEntries(eObject);
		assertNotNull(o);
		assertTrue(o instanceof EList);
	}

	@Test
	void testIsBundle() {
		OutputStream os = FHIRSDS.save(eObject, Finals.SDS_FORMAT.JSON);
		assertTrue(os instanceof ByteArrayOutputStream);
		assertTrue(app.isBundle(((ByteArrayOutputStream)os).toString()));
	}

}
