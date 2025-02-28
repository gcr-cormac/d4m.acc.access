package d4m.acc.access;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.SortedSet;
import java.util.UUID;

import org.apache.accumulo.core.client.Accumulo;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.BatchWriterConfig;
import org.apache.accumulo.core.client.MultiTableBatchWriter;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.apache.accumulo.core.security.Authorizations;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.hl7.fhir.Bundle;
import org.hl7.fhir.BundleEntry;
import org.hl7.fhir.emf.FHIRSDS;
import org.hl7.fhir.emf.Finals.SDS_FORMAT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import edu.mit.ll.cloud.connection.ConnectionProperties;
import edu.mit.ll.d4m.db.cloud.D4mDataSearch;
import edu.mit.ll.d4m.db.cloud.accumulo.D4mDbQueryAccumulo;
import edu.mit.ll.d4m.db.cloud.D4mDbResultSet;
import edu.mit.ll.d4m.db.cloud.D4mException;

@Component
public class AccumuloAccess {

	private static final Logger log = LoggerFactory.getLogger(AccumuloAccess.class);
	protected AccumuloClient client;

	protected FhirProcessor processor;
	final Properties clientProperties;

	public AccumuloAccess() {
		super();
		clientProperties = loadAccumuloProperties();
		this.client = Accumulo.newClient().from(clientProperties).build();
	}

	public SortedSet<String> listTables() {
		log.info("list==>");
		TableOperations ops = client.tableOperations();
		return ops.list();
	}

	public RCVs query(String row, String col, String tableName) {

		D4mDbQueryAccumulo accumuloQuery = new D4mDbQueryAccumulo(client2Connection(clientProperties), tableName) ;
		D4mDbResultSet result = null;
		RCVs rcvs = null;
		try {
			result = accumuloQuery.doMatlabQuery(row, col, AccumuloFinals.FAMILY, "");
			rcvs = D4mResultSet2RCVSConvert.convert(result);
		} catch (D4mException e) {
			log.error("", e);
		}
		return rcvs;
	}
		
	ConnectionProperties client2Connection(Properties clientProperties) {
		ConnectionProperties props = new ConnectionProperties();
		props.setInstanceName(clientProperties.getProperty("instance.name"));
		props.setHost(clientProperties.getProperty("instance.zookeepers"));
		props.setUser(clientProperties.getProperty("auth.principal"));
		props.setPass(clientProperties.getProperty("auth.token"));
		return props;
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
			ops.create(String.format("%s%s", tableName, AccumuloFinals.PAIR_DECOR));
			ops.create(String.format("%s%s", tableName, AccumuloFinals.DEGREE_DECOR));
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

		String resourceType = null;

		if (!client.tableOperations().exists(tableName)) {
			createTablePair(tableName);
		}

		byte[] bytes = resource.getBytes(StandardCharsets.UTF_8);
		InputStream reader = new ByteArrayInputStream(bytes);
		EObject eObject = FHIRSDS.load(reader, format);
		if (processor.isBundle(resource)) {
			EList<BundleEntry> entries = processor.getEntries(eObject);
			doInsert(entries, tableName);
		}
	}

	public void doInsert(EList<BundleEntry> entries, String tableName) {
        
		MultiTableBatchWriter multiTableWriter = client.createMultiTableBatchWriter(new BatchWriterConfig());

		for (BundleEntry entry : entries) {
			EObject eObject = entry.eContents().get(0);
            if (processor.isValidUUID(processor.getResourceId(eObject).getValue())) {
                processor.checkId(eObject);
            }
            doInsert(multiTableWriter, eObject, tableName);
		}

		try {
			multiTableWriter.flush();
			multiTableWriter.close();
		} catch (MutationsRejectedException e) {
			log.error("", e);
		}
	}

	public void doInsert(MultiTableBatchWriter multiTableWriter, EObject eObject, String tableName) {
		MutationBuilder mut = new MutationBuilder();
		MutationTBuilder mutT = new MutationTBuilder();
		MutationDegBuilder mutDeg = new MutationDegBuilder();
		try {
			multiTableWriter.getBatchWriter(tableName).addMutation(mut.doShred(eObject));
			multiTableWriter.getBatchWriter(String.format("%s%s", tableName, AccumuloFinals.PAIR_DECOR))
					.addMutation(mutT.doShred(eObject));
			multiTableWriter.getBatchWriter(String.format("%s%s", tableName, AccumuloFinals.DEGREE_DECOR))
					.addMutation(mutDeg.doShred(eObject));
		} catch (AccumuloException | AccumuloSecurityException | TableNotFoundException e) {
			log.error("", e);
		}
	}

    Properties loadAccumuloProperties() {
        Properties properties = new Properties();
        String propertiesFileName = "accumulo-client.properties";

        try (InputStream input = AccumuloAccess.class.getClassLoader().getResourceAsStream(propertiesFileName)) {
            if (input == null) {
                throw new IOException("Unable to find " + propertiesFileName);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading " + propertiesFileName, e);
        }

        return properties;
    }
}