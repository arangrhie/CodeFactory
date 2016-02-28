package javax.arang.phasing.snp;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.PhasedSNP;

public class ToHetDistance extends IOwrapper {

	@Override
	public void hooker(FileReader frSnp, FileMaker fmHet) {
		String line;
		String[] tokens;
		
		FileMaker fmRoh = new FileMaker(rohListPath);
		
		String prevContig = "";
		String contig;
		String prevPS = "";
		String ps;
		int prevPos = 0;
		int pos;
		
		boolean isFirst = true;
		
		while (frSnp.hasMoreLines()) {
			line = frSnp.readLine();
			tokens = line.split(RegExp.TAB);
			contig = tokens[PhasedSNP.CHR];
			if (tokens[PhasedSNP.POS].equals("POS"))	continue;
			pos = Integer.parseInt(tokens[PhasedSNP.POS]);
			ps = tokens[PhasedSNP.PS];
			if (isFirst) {
				isFirst = false;
				prevContig = contig;
				prevPS = ps;
				prevPos = pos;
				continue;
			}
			
			if (prevContig.equals(contig)) {
				if (prevPS.equals(ps)) {
					fmHet.writeLine((pos - prevPos + 1) + "");
				} else {
					fmRoh.writeLine((pos - prevPos + 1) + "");
				}
			}
			prevContig = contig;
			prevPS = ps;
			prevPos = pos;
		}
		
		
		fmRoh.closeMaker();
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar snpToHetDistance.jar <in.snp> <out.het.distance.list> <out.roh.distance.list>");
		System.out.println("\tcat *.het.snp > <in.snp>, and run this code.");
		System.out.println("\t<out.het.distance.list>: distance list within a phased block");
		System.out.println("\t<out.roh.distance.list>: distance list between phased blocks");
		System.out.println("Arang Rhie, 2015-12-21. arrhie@gmail.com");
	}

	private static String rohListPath;
	public static void main(String[] args) {
		if (args.length == 3) {
			rohListPath = args[2];
			new ToHetDistance().go(args[0], args[1]);
		} else {
			new ToHetDistance().printHelp();
		}
	}

}
