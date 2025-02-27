package d4m.acc.access;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.accumulo.core.client.BatchWriter;

public abstract class MutationBaseBuilder {
	
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

}
