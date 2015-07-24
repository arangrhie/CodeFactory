package javax.arang.ref.util;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class Cut200 extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String firstLine;
		String secondLine;
		int num200 = 0;
		int num17 = 0;
		while (fr.hasMoreLines()) {
			firstLine = fr.readLine().toString();
			if (firstLine.equals("")) continue;
			secondLine = fr.readLine().toString();
			if (secondLine.length() > 200) {
				num200++;
				secondLine = secondLine.substring(0, 200);
			} else if (secondLine.length() < 17) {
				num17++;
				continue;
			}
			fm.writeLine(firstLine);
			fm.writeLine(secondLine);
		}
		System.out.println(num17 + " reads have been trimmed out with read length < 17");
		System.out.println(num200 + " reads have been cut to 200 bp with read length > 200");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String inFile = "C://Documents and Settings/아랑/바탕 화면/mito/AK45_1st_run/R_2011_09_21_02_28_38_user_SN1-8_Mitochon_hg19.fastq/2011_09_21_SN1-8_Mitochon_hg19.fastq";
		String outFile = "2011_09_21_SN1-8_Mitochon_hg19_trimmed.fastq";
		if (args.length > 0) {
			inFile = args[0];
			outFile = args[1];
		}
		new Cut200().go(inFile, outFile);
	}

	@Override
	public void printHelp() {
		System.out.println("Cut reads of length < 17 bp");
		System.out.println("Reads with length > 200 have been cut to 200 pb");
	}

}
