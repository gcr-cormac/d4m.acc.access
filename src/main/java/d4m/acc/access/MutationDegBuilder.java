package d4m.acc.access;

import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.hadoop.io.Text;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.hl7.fhir.Resource;

public class MutationDegBuilder extends MutationBaseBuilder {
	
	public BatchWriter doShred(final EObject eObject, BatchWriter bw) {
		EClass eClass = eObject.eClass();
		Resource resource = (Resource)eObject;
		org.hl7.fhir.String id = resource.getId();
		Mutation mut = null;
		for(EAttribute eAttribute : eClass.getEAllAttributes()) {
			String name = eAttribute.getName();
			mut = new Mutation(name);
			byte[] toValue = objectToByteArray(id.getValue());
			mut.put(new Text(""), new Text("id"), new Value(toValue));
		}
		return bw;
	}

	@Override
	BatchWriter doShredOne(EObject eObject, EAttribute eAttribute, BatchWriter bw) {
		// TODO Auto-generated method stub
		return null;
	}
}
