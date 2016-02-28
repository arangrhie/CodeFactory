package javax.arang.base;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.base.util.Base;

public class CollectMultiAllelicSites extends IOwrapper {
	private static String noMulti = null;
	private static int cutOffFraction = 30;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		FileMaker noMultiFm = null;
		boolean writeNoMulti = false;
		if (noMulti != null) {
			noMultiFm = new FileMaker(noMulti);
			writeNoMulti = true;
		}
		
		String line;
		String[] tokens;
		int a;
		int c;
		int g;
		int t;
		int d;
		
		float cutOff;
		
		int countAllele = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			countAllele = 0;
			
			a = Integer.parseInt(tokens[Base.A]);
			c = Integer.parseInt(tokens[Base.C]);
			g = Integer.parseInt(tokens[Base.G]);
			t = Integer.parseInt(tokens[Base.T]);
			d = Integer.parseInt(tokens[Base.D]);
			
			if ((a+c+g+t) == 0) {
				continue;
			}
			
			cutOff = (a+c+g+t+d) * cutOffFraction / 100.0f;
			
			if (a >= cutOff) {
				countAllele++;
			}
			
			if (c >= cutOff) {
				countAllele++;
			}
			
			if (g >= cutOff) {
				countAllele++;
			}
			
			if (t >= cutOff) {
				countAllele++;
			}
			
			if (countAllele == 1 && writeNoMulti) {
				noMultiFm.writeLine(line + "\t" + fr.getFileName());
			} else if (countAllele > 1) {
				fm.writeLine(line + "\t" + fr.getFileName());
			}
		}
		
		if (writeNoMulti) {
			noMultiFm.closeMaker();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar baseCollectMultiAllelicSites.jar <in.base> <cut-off-fraction> <out.multi.base> [out.no-multi.base]");
		System.out.println("\tCollect Multi-Allelic Base Counts.");
		System.out.println("\t<in.base>: generated with bamToBaseDepth.jar. CHR\tPOS(1-base)\tA\tC\tG\tT\tD");
		System.out.println("\t<cut-off-fraction>: < cut-off-fraction(%) will be counted as \'no depth\'");
		System.out.println("\t<out.multi.base>: Sites with >=2 alleles observed with A/C/G/T.");
		System.out.println("\t[out.no-multi.base]: Sites with only 1 allele reported. (A/C/G/T, no D)");
		System.out.println("2015-09-14. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			cutOffFraction = Integer.parseInt(args[1]);
			new CollectMultiAllelicSites().go(args[0], args[2]);
		} else if (args.length == 4) {
			cutOffFraction = Integer.parseInt(args[1]);
			noMulti=args[3];
			new CollectMultiAllelicSites().go(args[0], args[2]);
		} else {
			new CollectMultiAllelicSites().printHelp();
		}
	}

}
