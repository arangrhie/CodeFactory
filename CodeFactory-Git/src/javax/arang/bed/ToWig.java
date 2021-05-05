package javax.arang.bed;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class ToWig extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String seqName = "";
		String prevName = "";
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			seqName = tokens[Bed.CHROM];
			if (!seqName.equals(prevName)) {
				System.out.println("variableStep chrom=" + seqName);
			}
			System.out.println((Integer.parseInt(tokens[Bed.START])+1) + "\t" + Integer.parseInt(tokens[tokens.length-1]));
			prevName = seqName;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar -Xmx1g bedToWig.jar <in.bed>");
		System.out.println("\t<in.bed>: bed file with value (coverage) on the last column");
		System.out.println("\t<stdout>: .wig formatted coverage. Use wigToBigWig to make a binary version.");
		System.out.println("Arang Rhie, 2020-10-21. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ToWig().go(args[0]);
		} else {
			new ToWig().printHelp();
		}
	}

}
