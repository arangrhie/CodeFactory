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
		
		int prevSpan = 0;
		
		System.out.println("track type=\"wiggle_0\" name=\"" + name + "\"");
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			int span = Integer.parseInt(tokens[Bed.END]) - Integer.parseInt(tokens[Bed.START]);
			
			seqName = tokens[Bed.CHROM];
			if (!seqName.equals(prevName) || prevSpan != span) {
				System.out.println("variableStep chrom=" + seqName + " span=" + span);
			}
			System.out.println((Integer.parseInt(tokens[Bed.START])+1) + "\t" + tokens[tokens.length-1]);
			prevName = seqName;
			prevSpan = span;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar -Xmx1g bedToWig.jar <in.bed> name");
		System.out.println("\t<in.bed>: bed file with value (coverage) on the last column");
		System.out.println("\t<stdout>: .wig formatted coverage. Use wigToBigWig to make a binary version.");
		System.out.println("\t* Outputs variable steps with span");
		System.out.println("Arang Rhie, 2021-12-22. arrhie@gmail.com");
	}
	
	private static String name="";

	public static void main(String[] args) {
		if (args.length == 2) {
			name = args[1];
			new ToWig().go(args[0]);
		} else {
			new ToWig().printHelp();
		}
	}

}
