package javax.arang.gaf;

import java.util.ArrayList;

public class ReadCov {
	
	private ArrayList<String> rIds;
	private ArrayList<String> rIdys;
	
	private void init() {
		rIds = new ArrayList<String>();
		rIdys = new ArrayList<String>();
	}
	
	public ReadCov() {
		init();
	}
	
	public ReadCov(String rId, boolean isPositive, String rIdy) {
		init();
		addRead(rId, isPositive, rIdy);
	}
	
	public void addRead(String rId, boolean isPositive, String rIdy) {
		if (isPositive) {
			rIds.add(Path.POS + rId);
		} else {
			rIds.add(Path.NEG + rId);
		}
		rIdys.add(rIdy);
	}
	
	public int getCoverage() {
		return rIds.size();
	}

	public String getIds() {
		String out = "";
		for (int i = 0; i < rIds.size(); i++) {
			out += rIds.get(i) + ",";
		}
		return out;
	}
	
	public String getIdys() {
		String out = "";
		for (int i = 0; i < rIdys.size(); i++) {
			out += rIdys.get(i)  + ",";
		}
		return out;
	}
}
