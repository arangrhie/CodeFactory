package javax.arang.plink;

import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class CompareBims extends I2Owrapper {

	static final short ID = 1;
	static final short ALT = 5;
	
	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		String line;
		String[] tokens;
		HashMap<String, String> genotypes = new HashMap<String, String>();

		int shared = 0;
		int fr1Only = 0;
		int fr2Only = 0;
		int count = 0;
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			tokens = line.split("\t");
			genotypes.put(tokens[ID], tokens[ALT]);
			fr1Only++;
			count++;
			if (count == 1000000) {
				System.out.println("Fetching 1,000,000 lines of " + fr1.getFileName());
				count = 0;
			}
		}
		System.out.println("Total number of SNPs in " + fr1.getFileName() + " : " + fr1Only);
		count = 0;
		
		int pos = 0;
		
		while (fr2.hasMoreLines()) {
			line = fr2.readLine();
			tokens = line.split("\t");
			if (genotypes.containsKey(tokens[ID])) {
				shared++;
			} else {
				fr2Only++;
				pos = Integer.parseInt(tokens[3]);
				fm.writeLine("chr" + tokens[0] + "\t" + (pos-1) + "\t" + pos);
			}
			if (count == 1000000) {
				System.out.println("Fetching 1,000,000 lines of " + fr2.getFileName());
				count = 0;
			}
		}
		
		fr1Only = fr1Only - shared;
		
		System.out.println("Shared number of SNPs: " + shared);
		System.out.println(fr1.getFileName() + " only: " + fr1Only);
		System.out.println(fr2.getFileName() + " only: " + fr2Only);
	}

	@Override
	public void printHelp() {
		System.out.println("Compare two .bim files.");
		System.out.println("Usage: java -jar plinkCompareBims.jar <in1.bim> <in2.bim> <out:in2only.bed>");
		System.out.println("\tCompares the second column of the .bim files. Put <in1.bim> with the smaller number of lines.");
		System.out.println("\t<in1.bam> should be less than " + Integer.MAX_VALUE + "");
		System.out.println("Arang Rhie, 2014-11-04. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if(args.length == 3) {
			new CompareBims().go(args[0], args[1], args[2]);
		} else {
			new CompareBims().printHelp();
		}
		
	}

}
