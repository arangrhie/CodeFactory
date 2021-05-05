package javax.arang.vcf;

import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToAlleleCount extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		System.out.println("#CHROM\tPOS\tREF\tALT\tQUAL\tFILTER\tGQ\tGL\tHET/HOM\tREF_CNT\tALT_CNT");
		String line;
		String[] tokens;
		String info = "";
		HashMap<String, String> sampleInfo = new HashMap<String, String>();
		String gq;
		String gl;
		String gt;
		String ad;
		String refCount;
		String altCount;
		int het = 0;
		int hom = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine(); 
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			info = tokens[VCF.CHROM] + "\t" + tokens[VCF.POS]
					+ "\t" + tokens[VCF.REF] + "\t" + tokens[VCF.ALT]
					+ "\t" + tokens[VCF.QUAL] + "\t" + tokens[VCF.FILTER];
			sampleInfo = VCF.parseFormatSample(tokens[VCF.FORMAT], tokens[VCF.SAMPLE]);
			if (sampleInfo.containsKey("GQ")) {
				gq = sampleInfo.get("GQ");
			} else {
				gq = "NA";
			}
			if (sampleInfo.containsKey("GL")) {
				gl = sampleInfo.get("GL");
			} else {
				gl = "NA";
			}
			gt = sampleInfo.get("GT");
			if (gt.equalsIgnoreCase("0")) {
				gt = "HOM";
				hom++;
			} else if (gt.equals("1")) {
				gt = "HET";
				het++;
			} else if (gt.equals("2")) {
				gt = "HOM";
				hom++;
			} else if (gt.equals(".")) {
				System.err.println("[ ERROR ] :: No genotype available. Filter your vcf :: " + line);
			} else {
				gt = "MULTI";
			}
			if (!sampleInfo.containsKey("AD")) {
				System.out.println(info + "\t" + gq + "\t" + gl + "\t" + gt + "\tNA\tNA");
			} else {
				ad = sampleInfo.get("AD");
				refCount = ad.substring(0, ad.indexOf(","));
				altCount = ad.substring(ad.indexOf(",") + 1);
				System.out.println(info + "\t" + gq + "\t" + gl + "\t" + gt + "\t" + refCount + "\t" + altCount);
			}
		}
		System.err.println("Hom: " + hom);
		System.err.println("Het: " + het);
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar vcfToAlleleCount.jar <in.vcf>");
		System.err.println("\t<in.vcf>: vcf file made with GATK / DeepVariant / freebayes,\n\t\textracting GQ GL GT AK fields (NA if not available)");
		System.err.println("\t<stdout>: CHROM\tPOS\tREF\tALT\tQUAL\tFILTER\tGQ\tGL\tHET/HOM\tREF_CNT\tALT_CNT");
		System.err.println("Arang Rhie, 2020-07-01. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ToAlleleCount().go(args[0]);
		} else {
			new ToAlleleCount().printHelp();
		}
	}

}
