package javax.arang.graph.gfa;

import java.util.Collection;
import java.util.HashMap;

public class Segment {
	
	private String name;
	private double len;
	private HashMap<String, Path> inPaths;
	private HashMap<String, Path> outPaths;
	private boolean isCircular = false;
	private boolean isOutPalendromic = false;
	private boolean isInPalendromic = false;
	private boolean hasVisited = false;
	
	public Segment(String name, double len) {
		this.name = name;
		this.len = len;
	}
	
	public void addInPath(Segment seg, char dir) {
		if (inPaths == null) {
			inPaths = new HashMap<String, Path>();
		}
		Path path = new Path(seg, dir);
		inPaths.put(path.getName(), path);
	}
	
	public void addOutPath(Segment seg, char dir) {
		if (outPaths == null) {
			outPaths = new HashMap<String, Path>();
		}
		Path path = new Path(seg, dir);
		outPaths.put(path.getName(), path);
	}
	
	public int getEdges(boolean isForward, boolean searchForwrad) {
		if (isForward) {
			if (searchForwrad) {
				return getOutPathsSize();
			} else {
				return getInPathsSize();
			}
		} else {
			if (searchForwrad) {
				return getInPathsSize();
			} else {
				return getOutPathsSize();
			}
		}
	}

	public int getInPathsSize() {
		if (inPaths == null) {
			return 0;
		}
		return inPaths.size();
	}
	
	public Collection<Path> getInPaths() {
		return inPaths.values();
	}
	
	public String printIn() {
		if (this.inPaths == null) {
			return "(0)";
		}
		String inPaths = "(" + this.inPaths.size() + ")";
		for (String name : this.inPaths.keySet()) {
			inPaths += " " + name;
		}
		return inPaths;
	}
	
	public String printOut() {
		if (this.outPaths == null) {
			return "(0)";
		}
		String outPaths = "";
		for (String name : this.outPaths.keySet()) {
			outPaths += name + " ";
		}
		return outPaths +  "(" + this.outPaths.size() + ")";
	}
	
	public void printInOut() {
		System.out.println(printIn() + "\t-|" + getName() + "(" + String.format("%,.0f", this.len) + ")|->\t" + printOut());
	}
	
	public int getOutPathsSize() {
		if (outPaths == null) {
			return 0;
		}
		return outPaths.size();
	}
	
	public Collection<Path> getOutPaths() {
		return outPaths.values();
	}
	
	public String getName() {
		return this.name;
	}
	
	public double getSize() {
		return this.len;
	}
	
	public void setCircular() {
		isCircular = true;
	}
	
	public boolean isCirculare() {
		return isCircular;
	}
	
	public void setOutPalendromic() {
		isOutPalendromic = true;
	}
	
	public boolean isOutPalendromic() {
		return isOutPalendromic;
	}
	
	public void setInPalendromic() {
		isInPalendromic = true;
	}
	
	public boolean isInPalendromic() {
		return isInPalendromic;
	}
	
	
	public boolean isMarkedVisited() {
		return hasVisited;
	}
	
	public void markVisited() {
		hasVisited = true;
	}
}
