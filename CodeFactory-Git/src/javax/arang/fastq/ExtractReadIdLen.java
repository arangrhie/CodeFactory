package javax.arang.fastq;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ExtractReadIdLen extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		int lineNum = 0;
		String readID = "";
		int readLen;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (lineNum % 4 == 0) {
				readID = line.substring(1);
				lineNum = 0;
			} else if (lineNum % 4 == 1) {
				readLen = line.length();
				fm.writeLine(readID + "\t" + readLen);
			}
			lineNum++;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastqExtractReadIdLen.jar <in.fastq>");
		System.out.println("\t<in.fastq>: a regular fastq file");
		System.out.println("\t<out.fastq.len>: ReadID\tReadLen");
		System.out.println("Arang Rhie, 2016-08-08. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ExtractReadIdLen().go(args[0], args[0] + ".len");
		} else {
			new ExtractReadIdLen().printHelp();
		}
	}

}
