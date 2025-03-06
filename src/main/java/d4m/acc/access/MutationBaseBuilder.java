package d4m.acc.access;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.hadoop.io.Text;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.hl7.fhir.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MutationBaseBuilder {
	
	static final Logger log = LoggerFactory.getLogger(MutationBaseBuilder.class);

	public BatchWriter doShred(final EObject eObject, BatchWriter bw) {
		EClass eClass = eObject.eClass();
		for (EAttribute eAttribute : eClass.getEAllAttributes()) {
			bw = doShred(eObject, eAttribute, bw);
		}

		for (EReference eReference : eClass.getEAllReferences()) {
			Object value = eObject.eGet(eReference);
			if (value instanceof EObject) {
				EObject referenced = (EObject) value;
				if (referenced instanceof Resource) {
					bw = doShred(eObject, eReference, bw);
				}
			}
			bw = doShred(eObject, eReference, bw);
		}
		return bw;
	}
	
	BatchWriter doShred(final EObject eObject, final EAttribute eAttribute, BatchWriter bw) {
		if (isOr(eObject, eAttribute)) {
			return bw;
		}
		if (eAttribute.isMany()) {
			log.info("isMany=" + eAttribute.getName());
			for (EObject eObject1 : eAttribute.eContents()) {
				bw = doShredOne(eObject1, eAttribute, bw);
			}
		} else {
			bw = doShredOne(eObject, eAttribute, bw);
		}
		return bw;
	}
	
	abstract BatchWriter doShredOne(final EObject eObject, final EAttribute eAttribute, BatchWriter bw);
	
	BatchWriter doShred(final EObject eObject, final EReference eReference, BatchWriter bw) {
		if (isOr(eObject, eReference)) {
			return null;
		}
		if (eReference.isMany()) {
			log.info("isMany=" + eReference.getName());
			for (EObject eObject1 : eReference.eContents()) {				
				bw = doShredOne(eObject1, bw);
			}
		} else {
			bw = doShredOne(eObject, bw);
		}
		return bw;
	}
	
	BatchWriter doShredOne(EObject eObject, BatchWriter bw) {
		eObject = FhirProcessor.checkId(eObject);
		return doShred(eObject, bw);
	}
	
	public byte[] objectToByteArray(Object obj) {
	    try {
	        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
	        ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
	        objectStream.writeObject(obj);
	        objectStream.close();
	        return byteStream.toByteArray();
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}

	public Object byteArrayToObject(byte[] bytes) {
	    try {
	        ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
	        ObjectInputStream objectStream = new ObjectInputStream(byteStream);
	        Object obj = objectStream.readObject();
	        objectStream.close();
	        return obj;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	public boolean isOr(final EObject eObject, final EAttribute eAttribute) {
		boolean _or = false;
		boolean _or_1 = false;
		boolean _isDerived = eAttribute.isDerived();
		if (_isDerived) {
			_or_1 = true;
		} else {
			boolean _isTransient = eAttribute.isTransient();
			_or_1 = _isTransient;
		}
		if (_or_1) {
			_or = true;
		} else {
			boolean _eIsSet = eObject.eIsSet(eAttribute);
			boolean _not = (!_eIsSet);
			_or = _not;
		}

		return _or;
	}

	public boolean isOr(final EObject eObject, final EReference eReference) {
		boolean _or = false;
		boolean _or_1 = false;
		boolean _isDerived = eReference.isDerived();
		if (_isDerived) {
			_or_1 = true;
		} else {
			boolean _isTransient = eReference.isTransient();
			_or_1 = _isTransient;
		}
		if (_or_1) {
			_or = true;
		} else {
			boolean _eIsSet = eObject.eIsSet(eReference);
			boolean _not = (!_eIsSet);
			_or = _not;
		}

		return _or;
	}
	
	Mutation createMutation(EObject eObject) {
		org.hl7.fhir.String id = FhirProcessor.getResourceId(eObject);
		return new Mutation(id.getValue());
	}
	
	Mutation createMutation(EAttribute eAttribute) {
		return new Mutation(eAttribute.getName());
	}
	
	Mutation createMutation(EReference eReference) {
		return new Mutation(eReference.getName());
	}
	
	Text createQualifier(EObject eObject) {
		org.hl7.fhir.String id = FhirProcessor.getResourceId(eObject);
		return new Text(id.getValue());
	}
	
	Text createQualifier(EAttribute eAttribute) {
		return new Text(eAttribute.getName());
	}
	
	Text createQualifier(EReference eReference) {
		return new Text(eReference.getName());
	}
	
//	Value createValue(EObject eObject) {
//		return new Value();
//	}
	
	Value createValue(final EObject eObject, EAttribute eAttribute) {
		Object fromValue = eObject.eGet(eAttribute);
		byte[] toValue = objectToByteArray(fromValue);
		return new Value(toValue);
	}
	
	Value createValue(final EObject eObject, EReference eReference) {
		System.out.println("eReference=" + eReference);
		Object fromValue = eObject.eGet(eReference);
		System.out.println("fromValue=" + fromValue);
		byte[] toValue = objectToByteArray(fromValue);
		System.out.println("toValue=" + toValue);
		return new Value(toValue);
	}
}
