package javax.arang.base;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;
import javax.arang.genome.Chromosome;

@Deprecated
public class Merge extends R2wrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2) {
		FileReader bed = new FileReader(bedFile);
		String chr;
		int pos;
		int start;
		int end;

		String line;
		String[] tokens;

		String line1 = fr1.readLine();
		String[] tokens1 = line1.split(RegExp.TAB);
		String chr1 = tokens1[Base.CHR];
		int pos1 = Integer.parseInt(tokens1[Base.POS]);
		
		String line2 = fr2.readLine();
		String[] tokens2 = line2.split(RegExp.TAB);
		String chr2 = tokens2[Base.CHR];
		int pos2 = Integer.parseInt(tokens2[Base.POS]);
		
		FileMaker fm = null;
		if (!isPerRegion) {
			fm = new FileMaker(fr1.getFileName().replace(".base", "_") + fr2.getFileName());
		}

		while (bed.hasMoreLines()) {
			line = bed.readLine();
			tokens = line.split(RegExp.TAB);
			
			chr = tokens[Bed.CHROM];
			start = Integer.parseInt(tokens[Bed.REGION_START]);
			end = Integer.parseInt(tokens[Bed.REGION_END]);
			
			
			if (isPerRegion) {
				fm = new FileMaker(tokens[Bed.NOTE] + ".base");
			}

			for (pos = start; pos < end; pos++) {
				
				if (tokens1 != null) {
					// seek fr1.base
					if (Chromosome.getChromIntVal(chr) > Chromosome.getChromIntVal(chr1)
							|| (Chromosome.getChromIntVal(chr) == Chromosome.getChromIntVal(chr1) && pos < pos1)) {
						tokens1 = goTo(fr1, chr, pos);
						if (tokens1 == null) {
							// fr1 reached the EOF
							break;
						}
					}
					pos1 = Integer.parseInt(tokens1[Base.POS]);
				}
				
				
				// seek fr2.base
				if (Chromosome.getChromIntVal(chr) > Chromosome.getChromIntVal(chr2)
						|| (Chromosome.getChromIntVal(chr) == Chromosome.getChromIntVal(chr2) && pos < pos2)) {
					tokens2 = goTo(fr2, chr, pos);
				}
				pos2 = Integer.parseInt(tokens2[Base.POS]);
				
				
			}
		}
		
	}
	
	private String[] goTo(FileReader fr, String chr, int pos) {
		while (fr.hasMoreLines()) {
			String line1 = fr.readLine();
			String[] tokens1 = line1.split(RegExp.TAB);
			String chr1 = tokens1[Base.CHR];
			int pos1 = Integer.parseInt(tokens1[Base.POS]);
			if (chr.equals(chr1)) {
				if (pos1 >= pos) {
					return tokens1;
				}
			}
		}
		return null;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar baseMerge.jar <in1.base> <in2.base> <region.bed> [per_region=TRUE]");
		System.out.println("\tMerge <in1.base> and <in2.base> at <region.bed>");
		System.out.println("\t<in1.base> and <in2.base>: Output of bamBaseDepth.jar");
		System.out.println("\t[per_region=TRUE]: TRUE = prints output as per-region.base");
		System.out.println("\t\t\t<region.bed> Assumed it is sorted. should contain 4th column containing the region name.");
		System.out.println("\t\tFALSE = prints region into 1 output, <in1_in2.base>");
		System.out.println("Arang Rhie, 2015-06-08. arrhie@gmail.com");
	}
	
	private static boolean isPerRegion = true;
	private static String bedFile;
	
	public static void main(String[] args) {
		if (args.length == 3) {
			bedFile = args[2];
			new Merge().go(args[0], args[1]);
		} else if (args.length == 4) {
			bedFile = args[2];
			isPerRegion = Boolean.parseBoolean(args[3]);
			new Merge().go(args[0], args[1]);
		} else {
			new Merge().printHelp();
		}
	}

}
