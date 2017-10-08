package javax.arang.graph.gfa;

import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToGraph extends Rwrapper {

	HashMap<String, Segment> segMap = new HashMap<String, Segment>();
	
	public HashMap<String, Segment> getSegMap() {
		return segMap;
	}
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		Segment seg1;
		Segment seg2;
		char dir1;
		char dir2;
		
		//Link link;
		//HashSet<String> linkId = new HashSet<String>();
		
		int numLinks = 0;
		int numCircular = 0;
		int numPalendromic = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("H")) {	continue; }
			else if (line.startsWith("S")) {
				tokens = line.split(RegExp.TAB);
				seg1 = new Segment(tokens[GFA.S_NAME], GFA.getLen(tokens[GFA.S_LEN]));
				segMap.put(tokens[GFA.S_NAME], seg1);
			}
			else if (line.startsWith("L")) {
				tokens = line.split(RegExp.TAB);
				seg1 = segMap.get(tokens[GFA.L_SEG1]);
				seg2 = segMap.get(tokens[GFA.L_SEG2]);
				dir1 = tokens[GFA.L_SEG1_DIR].charAt(0);
				dir2 = tokens[GFA.L_SEG2_DIR].charAt(0);

				numLinks++;
				
				// Mark as circular if seg1 == seg2 && dir1 == dir2
				if (GFA.isCircularOrPalendromic(seg1, dir1, seg2, dir2)) {
					if (dir1 == dir2 && !seg1.isCirculare()) {
						seg1.setCircular();
						numCircular++;
					} else if (dir1 == '-' && dir2 == '+' && !seg1.isInPalendromic()){
						seg1.setInPalendromic();
						numPalendromic++;
					} else if (dir1 == '+' && dir2 == '-' && !seg2.isOutPalendromic()) {
						seg1.setOutPalendromic();
						numPalendromic++;
					}
					// don't add the link
					continue;
				}
				if (GFA.isForward(dir1)) {
					
					seg1.addOutPath(seg2, dir2);
					if (GFA.isForward(dir2)) {
						// ----->
						//    ----->
						seg2.addInPath(seg1, dir1);
					} else {
						// ----->
						//    <-----
						seg2.addOutPath(seg1, GFA.switchDirection(dir1));
					}
				} else {
					seg1.addInPath(seg2, GFA.switchDirection(dir2));
					if (GFA.isForward(dir2)) {
						// <-----
						//    ----->
						seg2.addInPath(seg1, dir1);
					} else {
						// <-----
						//    <-----
						seg2.addOutPath(seg1, GFA.switchDirection(dir1));
					}
				}
			} else if (line.startsWith("P")) {
				System.out.println("[DEBUG] :: " + line);
			}
		}
		System.err.println("[DEBUG] :: total links: " + numLinks);
		System.err.println("[DEBUG] :: num. circular segments: " + numCircular);
		System.err.println("[DEBUG] :: num. palendromic links: " + numPalendromic);
		
	}
	
	@Override
	public void printHelp() {
		System.out.println("Usgae: java -jar gfaToGraph.jar <in.gfa>");
		System.out.println("\t<in.gfa>: gfa v1");
		System.out.println("\t<sysout>: summary of <in.gfa>");
		System.out.println("\t\tn\tNum. of segments with n links");
		System.out.println("\t\t* Test code for constructing the graph out of GFA format 1 *");
		System.out.println("Arang Rhie, 2017-09-11. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			ToGraph graph = new ToGraph();
			graph.go(args[0]);
			Summary.printSummary(graph.segMap);
		} else {
			new ToGraph().printHelp();
		}
	}

}
