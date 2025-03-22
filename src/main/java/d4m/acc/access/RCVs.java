package d4m.acc.access;

public class RCVs {

	final String[] rr;
	final String[] cc;
	final String[] vv;
	final String f;
	final String delimiter = ",";

	public RCVs(String[] rr, String[] cc, String[] vv, String f) {
		super();
		this.rr = rr;
		this.cc = cc;
		this.vv = vv;
		this.f = f;
	}

	public String getRows() {
		return String.format("%s%s", String.join(delimiter, rr), delimiter);
	}

	public String getCols() {
		return String.format("%s%s", String.join(delimiter, cc), delimiter);
	}

	public String getVals() {
		return String.format("%s%s", String.join(delimiter, vv), delimiter);
	}

	public String[] getRr() {
		return rr;
	}

	public String[] getCc() {
		return cc;
	}

	public String[] getVv() {
		return vv;
	}

	public String getF() {
		return f;
	}

	public String toString() {
		StringBuilder bld = new StringBuilder();
		bld.append(String.join(",", getRr()));
		bld.append(String.join(",", getCc()));
		bld.append(String.join(",", getVv()));
		bld.append(getF());
		return bld.toString();
	}
}
