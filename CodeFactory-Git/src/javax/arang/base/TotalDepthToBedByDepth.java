package javax.arang.base;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.base.util.TotalDepth;

public class TotalDepthToBedByDepth extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String contig = "";
		int pos;
		int depth;
		
		int start = 0;
		int end = 0;
		boolean isToReport = false;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			contig = tokens[TotalDepth.CONTIG];
			pos = Integer.parseInt(tokens[TotalDepth.POS]);
			depth = Integer.parseInt(tokens[TotalDepth.DEPTH]);
			if (depthFrom <= depth && depth <= depthTo) {
				if (!isToReport) {
					// first time
					isToReport = true;
					start = pos - 1;
					end = pos;
				} else {
					if (end == pos - 1) {
						end = pos;
					} else {
						if (hasName) {
							fm.writeLine(contig + "\t" + start + "\t" + end + "\t" + name);
						} else {
							fm.writeLine(contig + "\t" + start + "\t" + end);
						}
						start = pos - 1;
						end = pos;
					}
				}
			} else {
				if (isToReport) {
					isToReport = false;
					if (hasName) {
						fm.writeLine(contig + "\t" + start + "\t" + end + "\t" + name);
					} else {
						fm.writeLine(contig + "\t" + start + "\t" + end);
					}
				}
			}
			
		}
		
		if (isToReport) {
			if (hasName) {
				fm.writeLine(contig + "\t" + start + "\t" + end + "\t" + name);
			} else {
				fm.writeLine(contig + "\t" + start + "\t" + end);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar baseTotalDepthToBedByDepth.jar <in.depth> <out.bed> <depthFrom> <depthTo> [name]");
		System.out.println("\t<in.depth>: CHR\tPOS\tDEPTH");
		System.out.println("\t<out.bed>: bed format file with <depthFrom> <= DEPTH <= <depthTo>");
		System.out.println("\t\t[name] will be added at the end of the line in <out.bed> if provided.");
		System.out.println("Arang Rhie, 2015-12-12. arrhie@gmail.com");
	}
	
	private static boolean hasName = false;
	private static String name = "";
	private static int depthFrom = 1;
	private static int depthTo = 10000;
	
	public static void main(String[] args) {
		if (args.length == 4) {
			depthFrom = Integer.parseInt(args[2]);
			depthTo = Integer.parseInt(args[3]);
			new TotalDepthToBedByDepth().go(args[0], args[1]);
		} else if(args.length == 5) {
			name = args[4];
			hasName = true;
			depthFrom = Integer.parseInt(args[2]);
			depthTo = Integer.parseInt(args[3]);
			new TotalDepthToBedByDepth().go(args[0], args[1]);
		} else {
			new TotalDepthToBedByDepth().printHelp();
		}
	}

}
