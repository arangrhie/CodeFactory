package javax.arang.base;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.base.util.Base;

public class ToBedByDepth extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		System.out.println("Depth threshold: " + depthThreshold + "x");
		
		fr.readLine();	// skip header line
		
		String contig = "";
		int pos;
		int depth;
		
		boolean isPassingThreshold = false;
		int start = 0;
		int end = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			contig = tokens[Base.CHR];
			
			depth = Base.getTotalDepth(tokens[Base.A], tokens[Base.C], tokens[Base.G], tokens[Base.T], tokens[Base.D]);
			pos = Integer.parseInt(tokens[Base.POS]);

			if (depth > depthThreshold) {
				if (isPassingThreshold) {
					if (end == pos - 1) {
						end = pos;
					} else {
						fm.writeLine(contig + "\t" + start + "\t" + end + "\t>" + depthThreshold + "x");
						start = pos - 1;
						end = pos;
					}
				} else {
					start = pos - 1;
					end = pos;
					isPassingThreshold = true;
				}
			} else {
				if (isPassingThreshold) {
					fm.writeLine(contig + "\t" + start + "\t" + end + "\t>" + depthThreshold + "x");
				}
				isPassingThreshold = false;
			}
		}
		
		if (isPassingThreshold) {
			fm.writeLine(contig + "\t" + start + "\t" + end + "\t>" + depthThreshold + "x");
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar baseToBedByDepth.jar <in.base> <out.bed> [depth]");
		System.out.println("\tCollect regions with total coverage (A+C+G+T+D) > [depth]x");
		System.out.println("\t<in.base>: generated with samBaseDepth.jar or bamBaseDepth.jar");
		System.out.println("\t[depth]: DEFAULT=0");
		System.out.println("Arang Rhie, 2015-12-11. arrhie@gmail.com");
	}

	private static int depthThreshold = 0;
	public static void main(String[] args) {
		if (args.length == 3) {
			depthThreshold = Integer.parseInt(args[2]);
			new ToBedByDepth().go(args[0], args[1]);
		} else if (args.length == 2) {
			depthThreshold = 0;
			new ToBedByDepth().go(args[0], args[1]);
		} else {
			new ToBedByDepth().printHelp();
		}
	}

}
