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

class MutationBuilderBaseTest {

	static MutationBuilder app;
	static EObject eObject;
	static Bundle bundle;
    static BatchWriter mockBatchWriter;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		app = new MutationBuilder();
		assertNotNull(app);
		InputStream is = FhirProcessorTest.class.getResourceAsStream("/pat-ex.json");
		assertNotNull(is);
		eObject = FHIRSDS.load(is, Finals.SDS_FORMAT.JSON);
		System.out.println("is=" + eObject);
		assertNotNull(eObject);
//		bundle = (Bundle) eObject;
//		mockBatchWriter = Mockito.mock(BatchWriter.class);
//		assertNotNull(mockBatchWriter);

	}

	@Test 
	void test() {
		assertTrue(true);
	}
	
//	@Test
	void testDoShred() {
		OutputStream os = FHIRSDS.save(eObject, Finals.SDS_FORMAT.JSON);
		System.out.println("os=" + os.toString());
		System.out.println("bundle.name=" + bundle.eClass().getName());
		System.out.println("mockBatchWriter=" + mockBatchWriter);
		BundleEntry entry = bundle.getEntry().get(0);
		EObject resource = FhirProcessor.getFHIRResource(entry);
		System.out.println("resource.name=" + resource.eClass().getName());
		BatchWriter bw = app.doShred(entry, mockBatchWriter);
		assertNotNull(bw);
	}
}
