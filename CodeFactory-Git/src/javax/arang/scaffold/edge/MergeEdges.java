package javax.arang.scaffold.edge;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class MergeEdges extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		String prevContig = "";
		String contig;
		boolean isFirst = true;
		
		
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			
			contig = tokens[Edge.CONTIG_1];
			if (!prevContig.equals(contig)) {
				if (isFirst) {
					isFirst = false;
				} else {
					// Write down the edges
					
					
					// Initialize
				}
			}
			
			prevContig = contig;
			
			
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar edgeMergeEdges.jar <in.edge> <out.edge>");
		System.out.println("\tMerge edges in EDGE format.");
		System.out.println("\t<in.edge>: cat all .edge files, sort -k1,1.");
		System.out.println("\t\tSUPPORTS will be added.");
		System.out.println("\t\tEdges with C or D will be merged when overlaps > 50%.");
		System.out.println("\t<out.edge>: Merged format, to that edges are uniquely shown.");
		System.out.println("Arang Rhie, 2015-10-12.");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new MergeEdges().go(args[0], args[1]);
		} else {
			new MergeEdges().printHelp();
		}
	}

}
