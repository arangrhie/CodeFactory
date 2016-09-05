package javax.arang.annovar;

import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.annovar.util.ANNOVAR;
import javax.arang.vcf.VCF;

public class AddOtherVcf extends I2Owrapper {

	private static int colToAdd;
	
	@Override
	public void hooker(FileReader frAnnovar, FileReader frCnt, FileMaker fm) {
		HashMap<String, String>	keyToValue = new HashMap<String, String>();
		
		String line;
		String[] tokens;
		int pos;
		
		while (frCnt.hasMoreLines()) {
			line = frCnt.readLine();
			if (line.startsWith("#CHR") || line.startsWith("contig"))	continue;
			tokens = line.split(RegExp.TAB);
			pos = Integer.parseInt(tokens[VCF.POS]);
			if (tokens[VCF.REF].length() > tokens[VCF.ALT].length()) {
				pos++;
			}
			keyToValue.put(tokens[VCF.CHROM] + "_" + pos + "_" + tokens[VCF.ALT], tokens[colToAdd]);
		}
		
		String key;
		while (frAnnovar.hasMoreLines()) {
			line = frAnnovar.readLine();
			tokens = line.split(RegExp.TAB);
			key = tokens[ANNOVAR.CHR] + "_" + tokens[ANNOVAR.POS_FROM] + "_" + tokens[ANNOVAR.ALT];
			if (keyToValue.containsKey(key)) {
				fm.writeLine(line + "\t" + keyToValue.get(key));
			} else {
				fm.writeLine(line + "\tNA");
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("usage: java -jar annovarAddOtherVcf.jar <in.annovar> <vcf.to.add> <col_to_add> <out.annovar>");
		System.out.println("\t<in.annovar>: generated with ANNOVAR convert2annovar.pl, table_annovar.pl");
		System.out.println("\t<vcf.to.add>: vcf formatted CHR\tPOS\tID\tREF\tALT table with columns seperated with tab");
		System.out.println("\t<col_to_add>: column idx of <annovar.to.add> to add. 1-based");
		System.out.println("\t<out.annovar>: <in.annovar> with <annovar.to.add> added.");
		System.out.println("2016-06-01. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 4) {
			colToAdd = Integer.parseInt(args[2]) - 1;
			new AddOtherVcf().go(args[0], args[1], args[3]);
		} else {
			new AddOtherVcf().printHelp();
		}
	}

}
