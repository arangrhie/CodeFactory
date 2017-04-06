package javax.arang.fastq;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ExtractReadLen extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String readID = "";
		int readLen = 0;
		boolean isRead = false;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (Fastq.isReadID(line)) {
				readID = line.substring(1);
				isRead = true;
			} else if (Fastq.isQualSep(line)) {
				fm.writeLine(readID + "\t" + readLen);
				isRead = false;
				readLen = 0;
			} else if (isRead) {
				readLen += line.length();
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastqExtractReadLen.jar <in.fastq>");
		System.out.println("\t<in.fastq>: a regular fastq file");
		System.out.println("\t<out.fastq.len>: ReadID\tReadLen");
		System.out.println("Arang Rhie, 2017-02-03. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ExtractReadLen().go(args[0], args[0] + ".len");
		} else {
			new ExtractReadLen().printHelp();
		}
	}

}
