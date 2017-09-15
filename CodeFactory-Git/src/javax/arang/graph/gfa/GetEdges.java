package javax.arang.graph.gfa;

import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

public class GetEdges extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		ToGraph graph = new ToGraph();
		graph.hooker(fr);
		HashMap<String, Segment> segMap = graph.getSegMap();
		Segment seg = segMap.get(quereySegment);
		seg.printInOut();
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar gfaGetEdges.jar <in.gfa> <segment>");
		System.out.println("Print in/out edges of a given <segment>");
		System.out.println("\t<in.gfa>: gfa v1");
		System.out.println("\t<segment>: segment (contig) id defined in <in.gfa>");
		System.out.println("\t<stdout>: (numInEdges) [edge+/-]* -<segment>-> [edge+/-] (numOutEdges)");
		System.out.println("\t*Relative orientation is printed with <segment> being in + orientation.");
		System.out.println("Arang Rhie, 2017-09-14. arrhie@gmail.com");
	}

	private String quereySegment;
	public static void main(String[] args) {
		if (args.length == 2) {
			GetEdges printEdges = new GetEdges();
			printEdges.setQuerey(args[1]);
			printEdges.go(args[0]);
		} else {
			new GetEdges().printHelp();
		}
	}

	private void setQuerey(String segment) {
		quereySegment = segment;
	}

}
