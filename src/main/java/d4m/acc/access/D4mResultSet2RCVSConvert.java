package d4m.acc.access;

import java.util.ArrayList;

import edu.mit.ll.d4m.db.cloud.D4mDbResultSet;
import edu.mit.ll.d4m.db.cloud.D4mDbRow;

public class D4mResultSet2RCVSConvert {

    public static RCVs convert(D4mDbResultSet result) {
    	
    	ArrayList<D4mDbRow> rows = result.getMatlabDbRow();
    	
        int size = rows.size();
        String[] r = new String[size];
        String[] c = new String[size];
        String[] v = new String[size];
        
        for (int i = 0; i < size; i++) {
            D4mDbRow rowObj = rows.get(i);
            r[i] = rowObj.getRow();
            c[i] = rowObj.getColumn();
            v[i] = rowObj.getValue();
        }
        
        return new RCVs(r, c, v, AccumuloFinals.FAMILY);
    }
}
