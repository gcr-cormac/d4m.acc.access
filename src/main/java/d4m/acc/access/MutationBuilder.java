package d4m.acc.access;

import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.hadoop.io.Text;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MutationBuilder extends MutationBaseBuilder {

	static final Logger log = LoggerFactory.getLogger(MutationBuilder.class);

	BatchWriter doShredOne(EObject eObject, EAttribute eAttribute, BatchWriter bw) {
		try {
			eObject = FhirProcessor.checkId(eObject);
			Mutation mut = createMutation(eObject);
			Text qualifier = createQualifier(eAttribute);
			Value value = createValue(eObject, eAttribute);
			mut.put(AccumuloFinals.FAMILY, qualifier, value);
			bw.addMutation(mut);
		} catch (MutationsRejectedException e) {
			log.error("", e);
		}
		return bw;
	}
}
