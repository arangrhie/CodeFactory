package javax.arang.vcf;

import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class MvcfToSnp extends IOwrapper {

	static String ref = "";
	static boolean isRefCall = false;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {

		HashMap<String, String> formatTable = null;
		int numVars = 0;
		
		READ_LINE : while(fr.hasMoreLines()) {
			String line = fr.readLine();
			if (line.startsWith("#"))	continue;
			String[] tokens = line.split("\t");
			
			if (isRefCall) {
				if (!tokens[VCF.CHROM].equals(ref))	continue;
				else { }	// process below
			}
			
			// filter out if not fits conditions
			// skip INDELs
			HashMap<String, String> infoTable = VCF.parseInfo(tokens[VCF.INFO]);
			if (infoTable.containsKey("INDEL")) {
				continue READ_LINE;
			}
			
			String preString = tokens[VCF.CHROM] + "\t" + 
								tokens[VCF.POS] + "\t" + tokens[VCF.POS] + "\t" +
								tokens[VCF.REF] + "\t" + tokens[VCF.ALT];
			
			for (int i = VCF.SAMPLE; i < tokens.length; i++) {
				formatTable = VCF.parseFormatSample(tokens[VCF.FORMAT], tokens[i]);
				// GT == NA
				if (formatTable.containsKey("GT")) {
					int genotype = Integer.parseInt(formatTable.get("GT"));
					if (genotype == -1)	{
						continue READ_LINE;
					}
				}
				// genotype quality (GQ) < 17
				if (formatTable.containsKey("GQ")) {
					int gq = Integer.parseInt(formatTable.get("GQ"));
					if (gq < 17)	{
						continue READ_LINE;
					} else {
						preString = preString + "\t" + formatTable.get("GT");
					}
				}
			}
			numVars++;
			fm.writeLine(preString);
		}
		System.out.println("Total # of variants\t" + numVars);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar mVcfToSnp.jar <in.vcf> [chrN]");
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new MvcfToSnp().go(args[0], args[0].replace(".vcf", ".snp"));
		} else if (args.length == 2) {
			ref = args[1];
			isRefCall = true;
			new MvcfToSnp().go(args[0], args[0].replace(".vcf", "_" + args[1] + ".snp"));
		} else {
			new MvcfToSnp().printHelp();
		}
	}

}
