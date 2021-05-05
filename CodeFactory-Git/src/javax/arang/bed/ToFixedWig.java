package javax.arang.bed;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class ToFixedWig extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String seqName = "";
		String prevName = "";
		int	start = 0;
		
		// track type="wiggle_0" name="HiFi"
		System.out.println("track type= \"wiggle_0\" name=\"" + name + "\"");
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			seqName = tokens[Bed.CHROM];
			if (!seqName.equals(prevName)) {
				start = Integer.parseInt(tokens[Bed.START]) + 1;
				// fixedStep chrom=chr1 start=1 step=1024 span=1024
				System.out.println("fixedStep chrom=" + seqName + " start=" + start + " step=" + span + " span=" + span);
			}
			System.out.println(tokens[i]);
			prevName = seqName;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar -Xmx1g bedToFixedWig.jar <in.bed> <name> <span> [i]");
		System.out.println("\t<in.bed>: bed file with value on the [i]th column");
		System.out.println("\t<name>  : name of this track. String");
		System.out.println("\t<span>  : span of the interval. INT");
		System.out.println("\t[i]     : index of the value column. 1-base. DEFAULT=4");
		System.out.println("\t<stdout>: .wig formatted. Use wigToBigWig to make a binary version.");
		System.out.println("Arang Rhie, 2021-05-05. arrhie@gmail.com");

	}

	private static int span = 10000;
	private static String name = "";
	private static int i = 3;
	
	public static void main(String[] args) {
		if (args.length >= 3) {
			name=args[1];
			span=Integer.parseInt(args[2]);
			if (args.length > 3) {
				i = Integer.parseInt(args[3]) - 1;	// -1 to make it 0-base
			}
			new ToFixedWig().go(args[0]);
		} else {
			new ToFixedWig().printHelp();
		}

	}

}
