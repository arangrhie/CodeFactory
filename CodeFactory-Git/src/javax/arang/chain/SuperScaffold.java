package javax.arang.chain;

import java.util.ArrayList;
import java.util.HashMap;

public class SuperScaffold {
	
	public static short COMPONENT_CONTIG = 0;
	public static short COMPONENT_START = 1;
	public static short COMPONENT_END = 2;
	public static short COMPONENT_SIZE = 3;
	public static short COMPONENT_ORIENTATION = 4;

	private ArrayList<Integer> starts = new ArrayList<Integer>();
	private ArrayList<Integer> ends = new ArrayList<Integer>();
	
	private HashMap<Integer, String> startComponentMap = new HashMap<Integer, String>();
	private HashMap<Integer, Integer[]> startComponentStartEndMap = new HashMap<Integer, Integer[]>();
	private HashMap<Integer, String> startComponentOrientationMap = new HashMap<Integer, String>();
	private HashMap<Integer, Integer> startComponentSizeMap = new HashMap<Integer, Integer>();
	
	public void addPosition(int start, int end, String component,
							int componentStart, int componentEnd,
							int componentSize, String orientation) {
		starts.add(start);
		ends.add(end);
		startComponentMap.put(start, component);
		Integer[] pos = new Integer[2];
		pos[0] = componentStart;
		pos[1] = componentEnd;
		startComponentStartEndMap.put(start, pos);
		Integer[] compPos = new Integer[2];
		compPos[0] = 
		startComponentSizeMap.put(start, componentSize);
		startComponentOrientationMap.put(start, orientation);
	}
	
	/***
	 * 
	 * @param start
	 * @param end
	 * @return contig,start,end,size,orientation
	 */
	public String[] getComponent(int start, int end) {
		String[] components = new String[5];
		int scaffoldStart = -1;
		int componentStart = -1;
		int componentEnd = -1;
		int componentSize = -1;
		String orientation = "";
		for (int i = 0; i < starts.size() - 1; i++) {
			if (starts.get(i) <= start && start < ends.get(i)) {
				if (end > ends.get(i)) {
					i++;
					//System.err.println("[DEBUG] :: " + components[0] + " " + scaffoldStart + "-" + ends.get(i) + " start: " + start + " end: " + end);
					//System.exit(-1);
				}
				scaffoldStart = starts.get(i);
				components[0] = startComponentMap.get(scaffoldStart);
				componentStart = startComponentStartEndMap.get(scaffoldStart)[0];
				componentEnd = startComponentStartEndMap.get(scaffoldStart)[1];
				componentSize = startComponentSizeMap.get(scaffoldStart);
				orientation = startComponentOrientationMap.get(scaffoldStart);
				break;
			}
		}
		if (scaffoldStart == -1) {
			System.out.println("[DEBUG] :: " + start + " " + end);
			for (int i = 0; i < components.length; i++) {
				components[i] = "N";
			}
		} else {
			int offset = start - scaffoldStart;
			int tmp = componentStart + offset;
			offset = end - scaffoldStart;
			componentEnd = componentStart + offset;
			componentStart = tmp;
			if(offset < 0) {
				System.out.println("[DEBUG] :: " + components[0] + " start: " + start + " end: " + scaffoldStart + " componentStart: " + componentStart + " componentEnd: " + componentEnd);
				//System.exit(-1);
			}
			components[1] = String.valueOf(componentStart);
			components[2] = String.valueOf(componentEnd);
			components[3] = String.valueOf(componentSize);
			components[4] = orientation;
		}
		return components;
	}
}
