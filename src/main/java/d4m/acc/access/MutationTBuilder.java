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

public class MutationTBuilder extends MutationBaseBuilder {

	static final Logger log = LoggerFactory.getLogger(MutationTBuilder.class);

	BatchWriter doShredOne(final EObject eObject, final EAttribute eAttribute, BatchWriter bw) {
		try {
			Mutation mut = createMutation(eAttribute);
			Text qualifier = createQualifier(eObject);
			Value value = createValue(eObject, eAttribute);
			mut.put(new Text(""), qualifier, value);
			bw.addMutation(mut);
		} catch (MutationsRejectedException e) {
			log.error("", e);
		}
		return bw;
	}

	BatchWriter doShredOne(final EObject eObject, final EReference eReference, BatchWriter bw) {
		Mutation mut = createMutation(eReference);
		Text qualifier = createQualifier(eObject);
		Value value = createValue(eObject, eReference);
		mut.put(new Text(""), qualifier, value);
		try {
			bw.addMutation(mut);
		} catch (MutationsRejectedException e) {
			log.error("", e);
		}
		return bw;
	}
}
