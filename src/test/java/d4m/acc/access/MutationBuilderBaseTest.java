package d4m.acc.access;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;

import org.apache.accumulo.core.client.BatchWriter;
import org.eclipse.emf.ecore.EObject;
import org.hl7.fhir.emf.FHIRSDS;
import org.hl7.fhir.emf.Finals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MutationBuilderBaseTest {

	static MutationBuilder app;
	static EObject eObject;
    static BatchWriter mockBatchWriter;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		app = new MutationBuilder();
		InputStream is = FhirProcessorTest.class.getResourceAsStream("/fhir_adverse_event.0.json");
		assertNotNull(is);
		eObject = FHIRSDS.load(is, Finals.SDS_FORMAT.JSON);
		assertNotNull(eObject);
		mockBatchWriter = Mockito.mock(BatchWriter.class);

	}

	@Test
	void testDoShred() {
		BatchWriter bw = app.doShred(eObject, mockBatchWriter);
		assertNotNull(bw);
	}
}
