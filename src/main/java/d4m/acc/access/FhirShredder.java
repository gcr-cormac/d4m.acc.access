package d4m.acc.access;

import java.util.ArrayList;
import java.util.List;

import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.hadoop.io.Text;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.hl7.fhir.Resource;

public class FhirShredder {

    public List<Mutation> doShred(final EObject eObject) {
        List<Mutation> mutations = new ArrayList<>();
        shredEObject(eObject, mutations);
        return mutations;
    }

    private void shredEObject(final EObject eObject, List<Mutation> mutations) {
        EClass eClass = eObject.eClass();
        Resource resource = (Resource) eObject;
        org.hl7.fhir.String id = resource.getId();

        if (id == null || id.getValue() == null) {
            throw new IllegalArgumentException("FHIR Resource must have an ID to be stored in Accumulo.");
        }

        Mutation mut = new Mutation(id.getValue());

        // Process attributes (EAttributes)
        for (EAttribute eAttribute : eClass.getEAllAttributes()) {
            String name = eAttribute.getName();
            Object fromValue = eObject.eGet(eAttribute);
            if (fromValue != null) {
                byte[] toValue = objectToByteArray(fromValue);
                mut.put(new Text(eClass.getName()), new Text(name), new Value(toValue));
            }
        }

        // Process references (EReferences) and recursively traverse them
        for (EReference eReference : eClass.getEAllReferences()) {
            String refName = eReference.getName();
            Object refObject = eObject.eGet(eReference);

            if (refObject instanceof EObject) {
                EObject referencedEObject = (EObject) refObject;
                org.hl7.fhir.String refId = ((Resource) referencedEObject).getId();

                if (refId != null && refId.getValue() != null) {
                    mut.put(new Text(eClass.getName()), new Text(refName), new Value(refId.getValue().getBytes()));

                    // Recursively process referenced object
                    shredEObject(referencedEObject, mutations);
                }
            }
        }

        // Store mutation for this object
        mutations.add(mut);
    }

    private byte[] objectToByteArray(Object obj) {
        if (obj instanceof org.hl7.fhir.String) {
            return ((org.hl7.fhir.String) obj).getValue().getBytes();
        } else if (obj instanceof String) {
            return ((String) obj).getBytes();
        } else if (obj instanceof Integer) {
            return String.valueOf(obj).getBytes();
        } else if (obj instanceof Boolean) {
            return ((Boolean) obj) ? "true".getBytes() : "false".getBytes();
        } else if (obj != null) {
            return obj.toString().getBytes(); // Fallback for unknown types
        }
        return new byte[0]; // Return empty bytes if null
    }
}

