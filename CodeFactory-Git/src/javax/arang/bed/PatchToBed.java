package javax.arang.bed;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class PatchToBed extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {

		/**
		 * Example:
		 * chr1.1	0	857831	.	0	+
		 * Super-Scaffold_445_1	721324	726698	.	0	+
		 * chr1.2	0	247525781	.	0	+
		 */
		
		double start;
		double end;
		
		double newStart = -1;
		double newEnd   = -1;
		String name;
		String score;
		String strand;
		
		String[] tokens;
		
		boolean isFirstLine = true;
		
		while (fr.hasMoreLines()) {
			tokens = fr.readLine().split(RegExp.TAB);
			
			start = Double.parseDouble(tokens[Bed.START]);
			end   = Double.parseDouble(tokens[Bed.END]);
			name  = tokens[Bed.CHROM] + ":" + String.format("%.0f", start) + "-" + String.format("%.0f", end);
			score = tokens[Bed.MQ];
			strand = tokens[Bed.STRAND];
			
			if (isFirstLine) {
				newStart = start;
				newEnd   = end;
				isFirstLine = false;
			} else {
				newStart = newEnd;
				newEnd   = newStart + end - start;
			}
			
			System.out.println(chr +
					"\t" + String.format("%.0f", newStart) +
					"\t" + String.format("%.0f", newEnd) +
					"\t" + name + "\t" + score + "\t" + strand);
		}
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar bedPatchToBed.jar chr in.bed");
		System.err.println();
		System.err.println("\tchr   :  target reference scaffold name.");
		System.err.println("\tin.bed:  temporary patch file, only contains contig of origin with no gaps.");
		System.err.println("\tContigOfOrigin <tab> Start <tab> End <tab> Name(.) <tab> Score <tab> Strand");
		System.err.println();
		System.err.println("\tsysout:  a bed file using <chr> as the reference, tiled from <in.bed>.");
		System.err.println();
		System.err.println("*This code is designed specifically to lift over patch.bed from T2T effort*");
		System.err.println("Arang Rhie, 2020-07-27. arrhie@gmail.com");
	}
	
	private static String chr;

	public static void main(String[] args) {
		if (args.length != 2) {
			new PatchToBed().printHelp();
		} else {
			chr = args[0];
			new PatchToBed().go(args[1]);
		}
	}

}
