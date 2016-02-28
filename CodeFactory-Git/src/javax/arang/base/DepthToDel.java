package javax.arang.base;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.base.util.TotalDepth;

public class DepthToDel extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String prevContig = "";
		String contig;
		int prevPos = 0;
		int pos;
		int prevDepth = 0;
		int depth;
		
		boolean isFirstLine = true;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			if (line.startsWith("#"))	continue;
			contig = tokens[TotalDepth.CONTIG];
			pos = Integer.parseInt(tokens[TotalDepth.POS]);
			depth = Integer.parseInt(tokens[TotalDepth.DEPTH]);
			if (isFirstLine) {
				prevContig = contig;
				prevPos = pos;
				prevDepth = depth;
				isFirstLine = false;
				continue;
			}
			
			if (prevContig.equals(contig) && pos - prevPos > 2) {
				fm.writeLine(contig + "\t" + prevPos + "\t" + (pos - 1) + "\tNO_DEPTH\t" + haplotype + "\t" + (pos - prevPos) + "\t" + prevDepth + "\t" + depth);
			}
			
			prevContig = contig;
			prevPos = pos;
			prevDepth = depth;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar baseDepthToDel.jar <in.depth> <out.del.bed> <haplotype>");
		System.out.println("\tGet positions with no depth available with flanked base depth");
		System.out.println("\t<in.depth>: CONTIG\tPOS\tDEPTH");
		System.out.println("\t<out.del.bed>: CONTIG\tSTART\tEND\tDELETION\tHAPLOTYPE\tLEN\tLEFT_DEPTH\tRIGHT_DEPTH");
		System.out.println("\t\tregions with prevContig.equals(contig) && pos - prevPos > 2 will be reported");
		System.out.println("Arang Rhie, 2015-12-17. arrhie@gmail.com");
	}

	public static String haplotype;
	public static void main(String[] args) {
		if (args.length == 3) {
			haplotype = args[2];
			new DepthToDel().go(args[0], args[1]);
		} else {
			new DepthToDel().printHelp();
		}
	}

}
