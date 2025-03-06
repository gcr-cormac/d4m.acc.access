package d4m.acc.access;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.accumulo.core.client.BatchWriter;
import org.eclipse.emf.ecore.EObject;
import org.hl7.fhir.Bundle;
import org.hl7.fhir.BundleEntry;
import org.hl7.fhir.emf.FHIRSDS;
import org.hl7.fhir.emf.Finals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MutationBuilderBaseTest {

	private static final Logger log = LoggerFactory.getLogger(MutationBuilderBaseTest.class);

	static MutationBuilder app;
	static EObject eObject;
	static Bundle bundle;
    static BatchWriter mockBatchWriter;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		app = new MutationBuilder();
		assertNotNull(app);
		InputStream is = FhirProcessorTest.class.getClassLoader().getResourceAsStream("pat-ex.json");
		log.debug("is=" + is);
		assertNotNull(is);
		eObject = FHIRSDS.load(is, Finals.SDS_FORMAT.JSON);
		log.debug("eObject=" + eObject);
		assertNotNull(eObject);
		bundle = (Bundle) eObject;
		mockBatchWriter = Mockito.mock(BatchWriter.class);
		assertNotNull(mockBatchWriter);

	}

	@Test 
	void test() {
		log.info("test==>");
		assertTrue(true);
	}
	
	@Test
	void testDoShred() {
		assertNotNull(eObject);
		// OutputStream os = FHIRSDS.save(eObject, Finals.SDS_FORMAT.JSON);
		// log.info("os=" + os.toString());
		log.info("bundle.name=" + bundle.eClass().getName());
		// log.info("mockBatchWriter=" + mockBatchWriter);
		// BundleEntry entry = bundle.getEntry().get(0);
		// EObject resource = FhirProcessor.getFHIRResource(entry);
		// log.info("resource.name=" + resource.eClass().getName());
		// BatchWriter bw = app.doShred(entry, mockBatchWriter);
		// assertNotNull(bw);
	}
}
