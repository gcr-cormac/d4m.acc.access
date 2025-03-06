package d4m.acc.access;

import org.apache.hadoop.io.Text;

public interface AccumuloFinals {
	
	public final static String PAIR_DECOR = "T";
	public final static String DEGREE_DECOR = "Deg";
	public final static Text FAMILY = new Text("");
	
	public final static String RESOURCE_TYPE = "resourceType";
	public final static String BUNDLE = "Bundle";
}
