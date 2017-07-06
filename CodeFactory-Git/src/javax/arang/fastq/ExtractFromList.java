package javax.arang.fastq;

import java.util.ArrayList;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ExtractFromList extends R2wrapper {

	@Override
	public void hooker(FileReader frFastq, FileReader frReadIdList) {
		ArrayList<String> readIdList = new ArrayList<String>();
		
		String line;
		String[] tokens;
		String readIdInFq;
		
		while (frReadIdList.hasMoreLines()) {
			line = frReadIdList.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			readIdInFq = tokens[0];
			readIdList.add(readIdInFq);
		}
		System.err.println(readIdList.size() + " readIds successfully loaded on memory");
		
		short fastqLine = 0;
		boolean extract = false;
		int numExtracted = 0;
		while (frFastq.hasMoreLines()) {
			line = frFastq.readLine();
			
			// if the line is readid
			if (fastqLine == Fastq.READID_LINE) {
				tokens = line.split(RegExp.WHITESPACE);
				//System.err.println("[DEBUG] :: " + tokens[0].substring(1));
				if (readIdList.contains(tokens[0].substring(1))) {
					extract = true;
					numExtracted++;
				} else {
					extract = false;
				}
			}
			
			if (extract) {
				System.out.println(line);
			}
			
			fastqLine++;
			fastqLine %= 4;
		}
		System.err.println(numExtracted + " reads found and extracted from " + frFastq.getFileName());
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastqExtractFromList.jar <in.fastq> <in.readid.list>");
		System.out.println("\t<sysout>: prints the fastqs listed in <in.readid.list>");
		System.out.println("\t<in.fastq>: FASTQ formatted file, with readid on the first string seperated via whitespace.");
		System.out.println("\t<in.readid.list>: List of read ids to extract.");
		System.out.println("Arang Rhie, 2017-01-23. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ExtractFromList().go(args[0], args[1]);
		} else {
			new ExtractFromList().printHelp();
		}
	}

}
