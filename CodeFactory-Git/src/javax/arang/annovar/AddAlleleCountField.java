package javax.arang.annovar;

import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.annovar.util.ANNOVAR;
import javax.arang.vcf.VCF;

public class AddAlleleCountField extends I2Owrapper {

	@Override
	public void hooker(FileReader frAnnovar, FileReader frCnt, FileMaker fm) {
		HashMap<String, String>	keyToAlleleCntField = new HashMap<String, String>();
		
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
			keyToAlleleCntField.put(tokens[VCF.CHROM] + "_" + pos, tokens[FIELD]);
		}
		
		String key;
		while (frAnnovar.hasMoreLines()) {
			line = frAnnovar.readLine();
			tokens = line.split(RegExp.TAB);
			key = tokens[ANNOVAR.CHR] + "_" + tokens[ANNOVAR.POS_FROM];
			if (keyToAlleleCntField.containsKey(key)) {
				fm.writeLine(line + "\t" + keyToAlleleCntField.get(key));
			} else {
				fm.writeLine(line + "\tNA");
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("usage: java -jar annovarAddAlleleCountField.jar <in.annovar> <in.allele.cnt> <out.annovar> <FIELD_IDX>");
		System.out.println("\t<in.annovar>: generated with ANNOVAR convert2annovar.pl, table_annovar.pl");
		System.out.println("\t<in.allele.cnt>: allele counts of each ref / alt allele. generated with vcfToAlleleCount.jar");
		System.out.println("\t<out.annovar>: 2 columns will be added: ref, alt allele count or NA NA at the end of <in.annovar>.");
		System.out.println("\t<FIELD_IDX>: 1-based. field to add from <in.allele.cnt>.");
		System.out.println("\t1=CHR, 2=POS, 3=REF, 4=ALT, 5=QUAL, 6=FILTER, 7=GQ, 8=GT, 9=REF_CNT, 10=ALT_CNT");
		System.out.println("2016-02-12. arrhie@gmail.com");
	}
	
	public static int FIELD = 9;

	public static void main(String[] args) {
		if (args.length == 4) {
			FIELD = Integer.parseInt(args[3]) - 1;
			new AddAlleleCountField().go(args[0], args[1], args[2]);
		} else {
			new AddAlleleCountField().printHelp();
		}
	}

}
