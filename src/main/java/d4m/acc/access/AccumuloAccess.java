package d4m.acc.access;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.SortedSet;

import org.apache.accumulo.core.client.Accumulo;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.eclipse.emf.ecore.EObject;
import org.hl7.fhir.emf.FHIRSDS;
import org.hl7.fhir.emf.Finals.SDS_FORMAT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Table;

@Component
public class AccumuloAccess {

	private static final Logger log = LoggerFactory.getLogger(AccumuloAccess.class);

	protected AccumuloClient client;

	final String pairDecor = "T";
	final String degreeDecor = "Deg";

	public AccumuloAccess() {
		super();
		Properties clientProperties = new Properties();
		try {
			clientProperties
					.load(AccumuloAccess.class.getClassLoader().getResourceAsStream("accumulo-client.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.client = Accumulo.newClient().from(clientProperties).build();
	}

	public SortedSet<String> listTables() {
		log.info("list==>");
		TableOperations ops = client.tableOperations();
		return ops.list();
	}

	public String createTable(String tableName) {
		TableOperations ops = client.tableOperations();
		try {
			ops.create(tableName);
		} catch (AccumuloException | AccumuloSecurityException | TableExistsException e) {
			e.printStackTrace();
		}
		return tableName;
	}

	public String createTablePair(String tableName) {
		TableOperations ops = client.tableOperations();
		try {
			ops.create(tableName);
			ops.create(tableName + pairDecor);
			ops.create(tableName + degreeDecor);
		} catch (AccumuloException | AccumuloSecurityException | TableExistsException e) {
			e.printStackTrace();
		}
		return tableName;
	}

	public void insert(String resource, SDS_FORMAT format, String tableName) {

		if (!client.tableOperations().exists(tableName)) {
			createTable(tableName);
		}

		byte[] bytes = resource.getBytes(StandardCharsets.UTF_8);
		InputStream reader = new ByteArrayInputStream(bytes);
		EObject eObject = FHIRSDS.load(reader, format);
	}

	public void insertPair(String resource, SDS_FORMAT format, String tableName) {

		if (!client.tableOperations().exists(tableName)) {
			createTablePair(tableName);
		}

		byte[] bytes = resource.getBytes(StandardCharsets.UTF_8);
		InputStream reader = new ByteArrayInputStream(bytes);
		EObject eObject = FHIRSDS.load(reader, format);
		FHIRD4M app = new FHIRD4M();
		app.serialize(eObject);

		try {
			final BatchWriter bw = client.createBatchWriter(tableName);
			final BatchWriter bwT = client.createBatchWriter(tableName + pairDecor);
			final BatchWriter bwDeg = client.createBatchWriter(tableName + degreeDecor);	
			
		} catch (TableNotFoundException e) {
			e.printStackTrace();
		}
	}
}
