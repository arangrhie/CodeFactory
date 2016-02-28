package javax.arang.vcf;

import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToAlleleCount extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		fm.writeLine("#CHROM\tPOS\tREF\tALT\tQUAL\tFILTER\tGQ\tHET/HOM\tREF_CNT\tALT_CNT");
		String line;
		String[] tokens;
		String info = "";
		HashMap<String, String> sampleInfo = new HashMap<String, String>();
		String gq;
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
			gq = sampleInfo.get("GQ");
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
			} else if (gt.equals("3")) {
				gt = "MULTI";
			}
			if (!sampleInfo.containsKey("AD")) {
				fm.writeLine(info + "\t" + gq + "\t" + gt + "\tNA\tNA");
			} else {
				ad = sampleInfo.get("AD");
				refCount = ad.substring(0, ad.indexOf(","));
				altCount = ad.substring(ad.indexOf(",") + 1);
				fm.writeLine(info + "\t" + gq + "\t" + gt + "\t" + refCount + "\t" + altCount);
			}
		}
		System.out.println("Hom: " + hom);
		System.out.println("Het: " + het);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar vcfToAlleleCount.jar <in.vcf> <out.allele.cnt>");
		System.out.println("\t<in.vcf>: vcf file made with GATK, containing AD GQ field");
		System.out.println("\t<out.allele.cnt>: CHROM\tPOS\tREF\tALT\tQUAL\tINFO\tGQ\tHET/HOM\tREF_CNT\tALT_CNT");
		System.out.println("Arang Rhie, 2016-02-12. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ToAlleleCount().go(args[0], args[1]);
		} else {
			new ToAlleleCount().printHelp();
		}
	}

}
