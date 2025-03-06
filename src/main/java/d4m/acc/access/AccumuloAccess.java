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
import org.apache.accumulo.core.client.BatchWriterConfig;
import org.apache.accumulo.core.client.MultiTableBatchWriter;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.hl7.fhir.BundleEntry;
import org.hl7.fhir.emf.FHIRSDS;
import org.hl7.fhir.emf.Finals.SDS_FORMAT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import edu.mit.ll.cloud.connection.ConnectionProperties;
import edu.mit.ll.d4m.db.cloud.D4mDbResultSet;
import edu.mit.ll.d4m.db.cloud.D4mException;
import edu.mit.ll.d4m.db.cloud.accumulo.D4mDbQueryAccumulo;

@Component
public class AccumuloAccess {

	private static final Logger log = LoggerFactory.getLogger(AccumuloAccess.class);
	
	protected AccumuloClient client;
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
			result = accumuloQuery.doMatlabQuery(row, col, AccumuloFinals.FAMILY.toString(), "");
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

		if (!client.tableOperations().exists(tableName)) {
			createTablePair(tableName);
		}

		byte[] bytes = resource.getBytes(StandardCharsets.UTF_8);
		InputStream reader = new ByteArrayInputStream(bytes);
		EObject eObject = FHIRSDS.load(reader, format);
		if (FhirProcessor.isBundle(resource)) {
			EList<BundleEntry> entries = FhirProcessor.getEntries(eObject);
			doInsert(entries, tableName);
		}
	}

	public void doInsert(EList<BundleEntry> entries, String tableName) {
        
		MultiTableBatchWriter multiTableWriter = client.createMultiTableBatchWriter(new BatchWriterConfig());

		System.out.println("entries.size=" + entries.size());
		for (BundleEntry entry : entries) {
			EObject eObject = entry.eContents().get(0);
            FhirProcessor.checkId(eObject);
			EObject resource = FhirProcessor.getFHIRResource(entry);
            doInsert(multiTableWriter, resource, tableName);
		}

		try {
			multiTableWriter.flush();
			multiTableWriter.close();
		} catch (MutationsRejectedException e) {
			log.error("", e);
		}
	}

	public void doInsert(MultiTableBatchWriter multiTableWriter, EObject eObject, String tableName) {
		try {
			BatchWriter bw = multiTableWriter.getBatchWriter(tableName);
			MutationBuilder mut = new MutationBuilder();
			bw = mut.doShred(eObject, bw);
			BatchWriter bwT = multiTableWriter.getBatchWriter(String.format("%s%s", tableName, AccumuloFinals.PAIR_DECOR));
			MutationTBuilder mutT = new MutationTBuilder();
			bwT = mutT.doShred(eObject, bwT);
//			BatchWriter bwDeg = multiTableWriter.getBatchWriter(String.format("%s%s", tableName, AccumuloFinals.DEGREE_DECOR));
//			MutationDegBuilder mutDeg = new MutationDegBuilder();
//			bwDeg.addMutation(mutDeg);
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