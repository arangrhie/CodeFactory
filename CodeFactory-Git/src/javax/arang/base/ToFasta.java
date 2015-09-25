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
			if (line.startsWith("#"))	continue;
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
			base = Base.maxLikelyBase(chr, pos, tokens[Base.A], tokens[Base.C], tokens[Base.G], tokens[Base.T], tokens[Base.D]);
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
