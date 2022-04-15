package javax.arang.gaf;

import java.util.Comparator;

import javax.arang.IO.basic.RegExp;

public class Path {

	String    path;
	String[]  nodes;
	boolean[] isPositive;	// node direction
	
	public static final String POS=">";
	public static final String NEG="<";
	
	public Path(String p) {
		path = p;
		String[] tmpNodes = p.split(RegExp.ANGLE_BRACKET);
		nodes      = new  String[tmpNodes.length - 1];
		isPositive = new boolean[tmpNodes.length - 1];
		
		int idx = 0;
		// +- is ><
		// tmpNodes[0] is an empty string, so start from 1
		for (int i = 0; i < tmpNodes.length - 1; i++) {
			nodes[i] = tmpNodes[i + 1];
			if (path.indexOf(POS, idx) < 0) {
				// node is -
				isPositive[i] = false;
				idx += nodes[i].length();
			} else if (path.indexOf(NEG, idx) < 0) {
				// node is +
				isPositive[i] = true;
			} else if (path.indexOf(NEG, idx) < path.indexOf(POS, idx)) {
				// node is -
				isPositive[i] = false;
			} else if (path.indexOf(NEG, idx) > path.indexOf(POS, idx)) {
				// node is +
				isPositive[i] = true;
			}
			idx += nodes[i].length();
			//  System.err.println("[ DEBUG ] :: " + nodes[i] + " " + (isPositive[i] ? "+" : "-"));
		}
	}
	
	public String getPath() {
		return this.path;
	}
	
	public int getNumNodes() {
		return this.nodes.length;
	}
	
	public void reversePath() {
		String outPath = "";
		String[] revNodes = new String[nodes.length];
		boolean[] revDir = new boolean[nodes.length];

		int revIdx;
		for (int i = 0; i < nodes.length; i++) {
			revIdx = nodes.length - i - 1;
			if (isPositive[i]) {
				outPath = NEG + nodes[i] + outPath; 
			} else {
				outPath = POS + nodes[i] + outPath;
			}
			revDir[revIdx] = !isPositive[i];
			revNodes[revIdx] = nodes[i];
		}
		path = outPath;
		nodes = revNodes;
		isPositive = revDir;
	}
	
	/***
	 * Normalize if
	 * i)  multiple nodes exist, first node is lexicographically larger than the last node
	 * ii) only one node in path, in reverse (>) direction 
	 * @return true if normalization was applied
	 */
	public boolean normalize() {
		// reverse if first node is lexicographically larger than the last node
		if (nodes.length > 1 && nodes[0].compareTo(nodes[nodes.length - 1]) > 0 ) {
			reversePath();
			return true;
		} else if (nodes.length == 1 && !isPositive[0]) {
			path = POS + nodes[0];
			isPositive[0] = true;
			return true;
		}
		return false;
	}
	
	public static class PathSortingComparator implements Comparator<Path> {

		//  return longer path first
		@Override
		public int compare(Path p1, Path p2) {
			return p2.path.length() - p1.path.length();
		}
		
	}
	

}
