package javax.arang.fastq;

import java.util.HashSet;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class Union extends R2wrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2) {
		String line;
		int lineNum = 0;
		double fr1Count = 0;
		double fr2Count = 0;
		double fr1fr2IntersectCount = 0;
		HashSet<String> uniqueSet = new HashSet<String>();
		
		String readid;
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			lineNum++;
			
			if (lineNum %4 == 1) {
				readid = line.substring(1);
				readid = readid.split(RegExp.WHITESPACE)[0];
				uniqueSet.add(readid);
				fr1Count++;
			}
			
			if (lineNum %4 == 0) {
				lineNum = 0;
			}
		}
		
		System.err.println("Num. reads in " + fr1.getFileName() + " : " + String.format("%,.0f", fr1Count));
		while (fr2.hasMoreLines()) {
			line = fr2.readLine();
			lineNum++;
			
			if (lineNum %4 == 1) {
				readid = line.substring(1);
				readid = readid.split(RegExp.WHITESPACE)[0];
				if (uniqueSet.contains(readid)) {
					fr1fr2IntersectCount++;
				} else {
					uniqueSet.add(readid);
				}
				fr2Count++;
			}
			
			if (lineNum %4 == 0) {
				lineNum = 0;
			}
		}
		
		System.err.println("Num. reads in " + fr2.getFileName() + " : " + String.format("%,.0f", fr2Count));
		System.err.println("Num. unique reads : " + String.format("%,d", uniqueSet.size()));
		System.err.println("Num. intersecting reads : " + String.format("%,.0f", fr1fr2IntersectCount));
		for (String read : uniqueSet) {
			System.out.println(read);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastqUnion.jar <some1.fastq> <some2.fastq>");
		System.out.println("\t<sysout>: list of unique fastq read names");
		System.out.println("\t<syserr>: num. of fastq reads in each file, num. of intersecting fastq reads");
		System.out.println("Arang Rhie, 11-27-2017. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new Union().go(args[0], args[1]);
		} else {
			new Union().printHelp();
		}
	}

}
