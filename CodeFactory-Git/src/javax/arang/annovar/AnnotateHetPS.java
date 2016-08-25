package javax.arang.annovar;

import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.annovar.util.ANNOVAR;
import javax.arang.vcf.VCF;

public class AnnotateHetPS extends I2Owrapper {

	@Override
	public void hooker(FileReader frAnnovar, FileReader frVCF, FileMaker fm) {
		String line;
		String[] tokens;
		String chr;
		int start;
		String key;
		String ref;
		String alt;
		
		HashMap<String, String> posToAvout = new HashMap<String, String>();
		frAnnovar.readLine();
		System.err.println("Reading " + frAnnovar.getFileName());
		while (frAnnovar.hasMoreLines()) {
			line = frAnnovar.readLine();
			tokens = line.split(RegExp.TAB);
			chr = tokens[ANNOVAR.CHR];
			start = Integer.parseInt(tokens[ANNOVAR.POS_FROM]);
			ref = tokens[ANNOVAR.REF];
			alt = tokens[ANNOVAR.ALT];
			
			if (ref.equals("-")) {
				// INS
				key = chr + "_" + start;
			} else if (alt.equals("-")) {
				// DEL
				key = chr + "_" + (start - 1);
			} else {
				key = chr + "_" + start;
			}
			
			if (posToAvout.containsKey(key)) {
				// Multi-allelic site: remove
				posToAvout.remove(key);
			} else {
				posToAvout.put(key, line);
			}
		}
		System.err.println(posToAvout.size() + " positions stored");
		
		String gt;
		String ps;
		System.err.println("Reading " + frVCF.getFileName());
		while (frVCF.hasMoreLines()) {
			line = frVCF.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			if (tokens[VCF.ALT].contains(","))	continue;
			if (tokens[VCF.FILTER].equals("LowQual"))	continue;
			gt = VCF.parseSAMPLE(tokens[VCF.FORMAT], "GT", tokens[VCF.SAMPLE]);
			if (gt.contains("/"))	continue;
			ps = VCF.parseSAMPLE(tokens[VCF.FORMAT], "PS", tokens[VCF.SAMPLE]);
			if (posToAvout.containsKey(tokens[VCF.CHROM] + "_" + tokens[VCF.POS])
					&& gt.charAt(0) != 2 && gt.charAt(2) != 2) {
				fm.writeLine(posToAvout.get(tokens[VCF.CHROM] + "_" + tokens[VCF.POS]) + "\t" + gt.charAt(0) + "\t" + gt.charAt(2) + "\t" + ps);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar annovarAnnotateHetPS.jar <in.avout> <in.vcf> <out.avout>");
		System.out.println("\t<in.avout>: Annotated <in.vcf>");
		System.out.println("\t<in.vcf>: 10X Unphased");
		System.out.println("\t<out.avout>: <in.avout> + 0|1 + PS");
		System.out.println("\t\tLowQual, unphased variants will not be reported.");
		System.out.println("Arang Rhie, 2015-12-21. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new AnnotateHetPS().go(args[0], args[1], args[2]);
		} else {
			new AnnotateHetPS().printHelp();
		}
	}

}
