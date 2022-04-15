package javax.arang.bed;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class ToSparseWig extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String seqName = "";
		int	start = 0;
		int span = 0;
		
		// track type="wiggle_0" name="HiFi"
		System.out.println("track type=\"wiggle_0\" name=\"" + name + "\"");
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			seqName = tokens[Bed.CHROM];
			start = Integer.parseInt(tokens[Bed.START]) + 1;
			span = Integer.parseInt(tokens[Bed.END]) - start;
			
			// fixedStep chrom=chr1 start=1 step=1024 span=1024
			System.out.println("fixedStep chrom=" + seqName + " start=" + start + " step=" + span + " span=" + span);
			if (i > 0) {
				System.out.println(tokens[i]);
			} else {
				System.out.println(1);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar -Xmx1g bedToSparseWig.jar <in.bed> <name> [i]");
		System.out.println("\t<in.bed>: bed file with value on the [i]th column");
		System.out.println("\t<name>  : name of this track. String");
		System.out.println("\t[i]     : index of the value column. 1-base. DEFAULT=0; using fixed value=1");
		System.out.println("\t<stdout>: .wig formatted. Use wigToBigWig to make a binary version.");
		System.out.println("Arang Rhie, 2021-05-04. arrhie@gmail.com");

	}

	private static String name = "";
	private static int i = 0;
	
	public static void main(String[] args) {
		if (args.length >= 2) {
			name=args[1];
			if (args.length > 2) {
				i = Integer.parseInt(args[2]) - 1;	// -1 to make it 0-base
			}
			new ToSparseWig().go(args[0]);
		} else {
			new ToSparseWig().printHelp();
		}

	}

}
