package javax.arang.annovar;

import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.annovar.util.ANNOVAR;
import javax.arang.vcf.VCF;

public class AddAlleleCount extends I2Owrapper {

	@Override
	public void hooker(FileReader frAnnovar, FileReader frCnt, FileMaker fm) {
		HashMap<String, String>	keyToRefAlleleCnt = new HashMap<String, String>();
		HashMap<String, String>	keyToAltAlleleCnt = new HashMap<String, String>();
		
		String line;
		String[] tokens;
		int pos;
		
		while (frCnt.hasMoreLines()) {
			line = frCnt.readLine();
			tokens = line.split(RegExp.TAB);
			pos = Integer.parseInt(tokens[VCF.POS]);
			if (tokens[VCF.REF].length() > tokens[VCF.ALT].length()) {
				pos++;
			}
			keyToRefAlleleCnt.put(tokens[VCF.CHROM] + "_" + pos, tokens[tokens.length - 2]);
			keyToAltAlleleCnt.put(tokens[VCF.CHROM] + "_" + pos, tokens[tokens.length - 1]);
		}
		
		String key;
		while (frAnnovar.hasMoreLines()) {
			line = frAnnovar.readLine();
			tokens = line.split(RegExp.TAB);
			key = tokens[ANNOVAR.CHR] + "_" + tokens[ANNOVAR.POS_FROM];
			if (keyToRefAlleleCnt.containsKey(key)) {
				fm.writeLine(line + "\t" + keyToRefAlleleCnt.get(key) + "\t" + keyToAltAlleleCnt.get(key));
			} else {
				fm.writeLine(line + "\tNA\tNA");
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("usage: java -jar annovarAddAlleleCount.jar <in.annovar> <in.allele.cnt> <out.annovar>");
		System.out.println("\t<in.annovar>: generated with ANNOVAR convert2annovar.pl, table_annovar.pl");
		System.out.println("\t<in.allele.cnt>: allele counts of each ref / alt allele. generated with vcfToAlleleCount.jar");
		System.out.println("\t<out.annovar>: 2 columns will be added: ref, alt allele count or NA NA at the end of <in.annovar>.");
		System.out.println("2015-12-22. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new AddAlleleCount().go(args[0], args[1], args[2]);
		} else {
			new AddAlleleCount().printHelp();
		}
	}

}
