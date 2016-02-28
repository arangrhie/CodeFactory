package javax.arang.scaffold.edge;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class BionanoEdgeToLink extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String scaffold;
		String prevScaffold = null;
		FileMaker fmLink = new FileMaker(fr.getDirectory(), fr.getFileName().replace(".edge", ".link"));
		FileMaker fmMap = new FileMaker(fr.getDirectory(), fr.getFileName().replace(".edge", ".map"));
		
		String contig1;
		String contig2 = null;
		String ort1;
		String ort2 = null;
		
		String contig1New;
		String contig2New = null;
		
		String start2 = null;
		String end2 = null;
		
		int numNodes = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			scaffold = tokens[Edge.EVIDENCE];
			if (prevScaffold != null && !scaffold.equals(prevScaffold)) {
				fmMap.writeLine(contig2New + "\t" + contig2 + "\t" + start2 + "\t" + end2 + "\t" + ort2);
				numNodes = 0;
			}
			contig1 = tokens[Edge.CONTIG_1].split(":")[0];
			contig2 = tokens[Edge.CONTIG_2].split(":")[0];
			ort1 = tokens[Edge.CONTIG_1].split(":")[1];
			ort2 = tokens[Edge.CONTIG_2].split(":")[1];
			ort1 = (ort1.endsWith("B") ? "-" : "+");
			ort2 = (ort2.endsWith("B") ? "+" : "-");
			contig1New = scaffold + ":" + numNodes;
			numNodes++;
			contig2New = scaffold + ":" + numNodes;
			
			fmLink.writeLine(contig1New + "[H]" + "\t" + contig2New + "[T]" + "\t" + tokens[Edge.GAP] + "\t" + 1);
			fmMap.writeLine(contig1New + "\t" + contig1 + "\t" + tokens[Edge.CONTIG_1_START] + "\t" + tokens[Edge.CONTIG_1_END] + "\t" + ort1);
			
			start2 = tokens[Edge.CONTIG_2_START];
			end2 = tokens[Edge.CONTIG_2_END];
			prevScaffold = scaffold;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Convert .edge file to .link file");
		System.out.println("Usage: java -jar edgeBionanoEdgeToLink.jar <in.edge>");
		System.out.println("\t<in.edge>: CONTIG1\tCONTIG2\tSUPPORTS\tCONTIG_1_START\tCONTIG_1_END\tCONTIG_1_LEN\tCONTIG_2_START\tCONTIG_2_END\tCONTIG_2_LEN\tEVIDENCE\tGAP");
		System.out.println("\t<out>: Files will be generated: <in.link> and <in.map>");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new BionanoEdgeToLink().go(args[0]);
		} else {
			new BionanoEdgeToLink().printHelp();
		}
	}

}
