package d4m.acc.access;

import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.data.Value;
import org.apache.hadoop.io.Text;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.hl7.fhir.Resource;

public class MutationBuilder extends MutationBaseBuilder {
	
 	public MutationBuilder(BatchWriter bw) {
		super(bw);
	}
	
	public void doShred(final EObject eObject) {
		EClass eClass = eObject.eClass();
		Resource resource = (Resource)eObject;
		org.hl7.fhir.String id = resource.getId();
		Mutation mut  = new Mutation(id.getValue());
		for(EAttribute eAttribute : eClass.getEAllAttributes()) {
			String name = eAttribute.getName();
			Object fromValue = eObject.eGet(eAttribute);
			byte[] toValue = objectToByteArray(fromValue);
			mut.put(new Text(""), new Text(name), new Value(toValue));
		}
		try {
			bw.addMutation(mut);
		} catch (MutationsRejectedException e) {
			e.printStackTrace();
		}
	}
}
