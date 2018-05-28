package javax.arang.phasing.snp;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.PhasedSNP;

public class LongRangeSwitchToBlocks extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String prevChr = "";
		String prevPos = "";
		String chr;
		String pos = "";
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			chr = tokens[PhasedSNP.CHR];
			pos = tokens[PhasedSNP.POS];
			
			if (prevChr.equals("")) {
				System.out.print(chr + "\t" + pos);
			} else if (!prevChr.equals("") && !prevChr.equals(chr)) {
				System.out.println("\t" + prevPos);
				System.out.print(chr + "\t" + pos);
			} else if ( line.endsWith("Long")) {
				System.out.println("\t" + prevPos);
				System.out.print(chr + "\t" + pos);
			}
			prevChr = chr;
			prevPos = pos;
		}
		System.out.println("\t" + pos);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingPhasedSnpLongRangeSwitchToBlocks.jar <in.snp.mark.lrswitch>");
		System.out.println("\t<in.snp.mark.lrswitch>: last column containing Long/Short1/Short2");
		System.out.println("\t<stdout>: bed format blocks.");
		System.out.println("\t\t-Start of a chromosome\n\t\t-Each positional entries before and with \"Long\"\n\t\t-Last entry of a chromosome\n\t\tis considered as block boundaries.");
		System.out.println("Arang Rhie, 2017-12-07. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new LongRangeSwitchToBlocks().go(args[0]);
		} else {
			new LongRangeSwitchToBlocks().printHelp();
		}
	}

}
