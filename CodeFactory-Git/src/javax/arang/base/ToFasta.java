package javax.arang.base;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToFasta extends IOwrapper {

	String chr;
	int pos;	// pos from .base file
	

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		int written = 1;	// written number of bases
		
		fm.writeLine(">" + fr.getFileName());
		String base;
		int countD = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			chr = tokens[Base.CHR];
			pos = Integer.parseInt(tokens[Base.POS]);
//			if (posToWrite < pos) {
//				for (;posToWrite < pos; posToWrite++) {
//					fm.write("N");
//					if (written % 80 == 0)	fm.writeLine();
//					written++;
//				}
//			}
			base = maxLikelyBase(tokens[Base.A], tokens[Base.C], tokens[Base.G], tokens[Base.T], tokens[Base.D]);
			if (!base.equals("D")) {
				fm.write(base);
				if (written % 80 == 0)	fm.writeLine();
				written++;
			} else {
				countD++;
			}
		}
		fm.writeLine();
		
		System.out.println("[DEBUG] :: Number of bases deleted = " + countD);
	}

	private String maxLikelyBase(String a, String c, String g,
			String t, String d) {
		int A = Integer.parseInt(a);
		int C = Integer.parseInt(c);
		int G = Integer.parseInt(g);
		int T = Integer.parseInt(t);
		int D = Integer.parseInt(d);
		int max = Math.max(Math.max(A, C), Math.max(G, T));
		if (max < D && D > 0) {
			return "D";
		} else {
			if (max == 0) {
				return "N";
			}
			if (max > 0
					&& ((max == A && (max == C || max == G || max == T))
					|| (max == C && (max == G || max == T))
					|| (max == G && max == T))) {
				System.out.println("[DEBUG] ::\t" + chr + ":" + pos + "\tA=" + A + "\tC=" + C + "\tG=" + G + "\tT=" + T);
				return "N";
			}
			if (A == max) {
				return "A";
			} else if (C == max) {
				return "C";
			} else if (G == max) {
				return "G";
			} else if (T == max) {
				return "T";
			}
		}
		return "N";
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar baseToFasta.jar <in.base> <out.fa>");
		System.out.println("\tConvert .base to .fa");
		System.out.println("\tBases not covered are inserted as N.");
		System.out.println("\tDeletions will not be reported when Max(ACGT) = 0.");
		System.out.println("\tAlleles on SNPs sites will be called with maximum read depth.");
		System.out.println("Arang Rhie, 2015-08-11. arrhie@gmail.com");
		System.out.println();
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ToFasta().go(args[0], args[1]);
		} else {
			new ToFasta().printHelp();
		}
	}

}
