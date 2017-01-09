package javax.arang.delly;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.vcf.VCF;

public class ToBed extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String chrom;
		int start;
		int end;
		String sv_type;
		String gt;
		String consensus;
		String precise;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			
			tokens = line.split(RegExp.TAB);
			
			chrom = tokens[VCF.CHROM];
			start = Integer.parseInt(tokens[VCF.POS]) - 1;
			end = Integer.parseInt(VCF.parseINFO(tokens[VCF.INFO], "END"));
			sv_type = VCF.parseINFO(tokens[VCF.INFO], "SVTYPE");
			gt = VCF.parseGT(tokens[VCF.FORMAT], tokens[VCF.SAMPLE]);
			consensus = VCF.parseINFO(tokens[VCF.INFO], "CONSENSUS");
			precise = tokens[VCF.INFO].substring(0, tokens[VCF.INFO].indexOf(";"));
			if (isToFilter && tokens[VCF.FILTER].equals("PASS") && !gt.equals("0") && !gt.equals("NA")) {
				System.out.println(chrom + "\t" + start + "\t" + end + "\t" + sv_type + "\t" + precise + "\t" + gt + "\t" + (end - start) + "\t" + consensus.length() + "\t" + consensus);
			} else if (!isToFilter) {
				System.out.println(chrom + "\t" + start + "\t" + end + "\t" + sv_type + "\t" + precise + "\t" + gt + "\t" + (end - start) + "\t" + consensus.length() + "\t" + consensus);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar -Xmx256m dellyToBed.jar <in.delly.vcf> [filter=TRUE/FALSE]");
		System.out.println("\tConverts the .vcf formatted SV into bed format.");
		System.out.println("\tstdout: chrom\tstart\tend\tSV_TYPE\tPRECISE/IMPRECISE\tGT(0,1,2)\tSV_LEN\tCONSENSUS_LEN\tCONSENSUS");
		System.out.println("\t[filter]: OPTIONAL. Filters LowQual, genotypes of 0/0. DEFAULT=TRUE");
		System.out.println("Arang Rhie, 2016-10-31. arrhie@gmail.com");
	}

	private static boolean isToFilter = true;
	public static void main(String[] args) {
		if (args.length == 1) {
			new ToBed().go(args[0]);
		} else if (args.length == 2) {
			isToFilter = Boolean.parseBoolean(args[1]);
			new ToBed().go(args[0]);
		} else {
			new ToBed().printHelp();
		}
	}

}
