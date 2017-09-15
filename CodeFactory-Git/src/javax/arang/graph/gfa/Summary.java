package javax.arang.graph.gfa;

import java.util.Arrays;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

public class Summary extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		ToGraph graphConstructor = new ToGraph();
		graphConstructor.hooker(fr);
	}

	public static void printSummary(HashMap<String, Segment> segMap) {

		System.err.println("[DEBUG] :: num. segments: " + segMap.size());
		
		HashMap<Integer, Integer> edgeCountMap = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> edgeBalancedMap = new HashMap<Integer, Integer>();
		Segment seg;
		
		int inEdges;
		int outEdges;
		int count = 0;
		
		for (String segment : segMap.keySet()) {
			seg = segMap.get(segment);
			if (seg.isCirculare()) {
				count++;
				//continue;
				//System.err.println("[DEBUG] :: Circular segment: " + seg.getName());
			}
			inEdges = seg.getInPathsSize();
			outEdges = seg.getOutPathsSize();
			if (!edgeCountMap.containsKey(inEdges)) {
				edgeCountMap.put(inEdges, 1);
				if (!edgeBalancedMap.containsKey(inEdges)) {
					edgeBalancedMap.put(inEdges, 0);
				}
			} else {
				edgeCountMap.put(inEdges, edgeCountMap.get(inEdges) + 1);
			}
			if (!edgeCountMap.containsKey(outEdges)) {
				edgeCountMap.put(outEdges, 1);
				if (!edgeBalancedMap.containsKey(outEdges)) {
					edgeBalancedMap.put(outEdges, 0);
				}
			} else {
				edgeCountMap.put(outEdges, edgeCountMap.get(outEdges) + 1);
			}
			if (inEdges == outEdges) {
				edgeBalancedMap.put(inEdges, edgeBalancedMap.get(inEdges) + 1);
			}
		}
		System.err.println("[DEBUG] :: num. circular segments: " + count);
		
		System.out.println("n\tNum. segments with n links\tBalanced (num. in = num. out)");
		Integer[] nList = edgeCountMap.keySet().toArray(new Integer[0]);
		Arrays.sort(nList);
		
		count = 0;
		for (int i = 0; i < nList.length; i++) {
			System.out.println(nList[i] + "\t" + edgeCountMap.get(nList[i]) + "\t" + edgeBalancedMap.get(nList[i]));
			count += nList[i] * edgeCountMap.get(nList[i]);
		}
		System.out.println();
		System.err.println("[DEBUG] :: in + out link count: " + count);
		
	}
	@Override
	public void printHelp() {
		System.out.println("Usgae: java -jar gfaSummary.jar <in.gfa>");
		System.out.println("\t<in.gfa>: gfa v1");
		System.out.println("\t<sysout>: summary of <in.gfa>");
		System.out.println("\t\tn\tNum. segments with n links\tNum. balanced (num. in=out links)");
		System.out.println("\t\t* Test code for constructing the graph out of GFA format 1 *");
		System.out.println("Arang Rhie, 2017-09-11. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new Summary().go(args[0]);
		} else {
			new Summary().printHelp();
		}
	}

}
