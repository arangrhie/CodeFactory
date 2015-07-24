package javax.arang.genome.base;

import java.util.ArrayList;

import javax.arang.IO.INwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class SnpCall extends INwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar baseSnpCall.jar <*.bas.chrN>");
	}

	int numCount = 0;

	@Override
	public void hooker(ArrayList<FileReader> frs) {
		String line;
		String[] tokens;
		for (FileReader fr : frs) {
			FileMaker fm = new FileMaker("snp", fr.getFileName() + ".snp");
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				if (line.equals(""))	continue;
				tokens = line.split("\t");
				int totalCount = Integer.parseInt(tokens[Base.TOTAL_COUNT]);
				if (totalCount < 10)	continue;
				if (tokens[Base.REF].equals("N"))	continue;
				int countA = Integer.parseInt(tokens[Base.COUNT_A]);
				int countC = Integer.parseInt(tokens[Base.COUNT_C]);
				int countG = Integer.parseInt(tokens[Base.COUNT_G]);
				int countT = Integer.parseInt(tokens[Base.COUNT_T]);
				int[] counts = Base.getAllelCount(tokens[Base.REF],
						countA, countC, countG, countT);
				int alleleCount = Integer.parseInt(tokens[counts[Base.ALLELE_BASE1]]);
				if (alleleCount < 5)	continue;
 				// write down
				writeSnp(fm, tokens, Base.getAlleleBase(counts[Base.ALLELE_BASE1]), totalCount, alleleCount);
				if (counts[Base.ALLELE_BASE2] != 0) {
					writeSnp(fm, tokens, Base.getAlleleBase(counts[Base.ALLELE_BASE2]), totalCount, alleleCount);	
				}
			}
			System.out.println(fm.getFileName() + " completed");
			fm.closeMaker();
		}
		System.out.println("# of SNPs\t" + numCount );
	}

	private void writeSnp(FileMaker fm, String[] tokens, String alleleBase, int totalCount, int alleleCount) {
		float af = ((float)alleleCount*100) / totalCount;
		String zygote = "Hom";
		if (Float.parseFloat(tokens[Base.QUAL_AVG]) < 20)	return;
		if (af < 20)	return;
		if (af < 80) {
			zygote = "Het";
		}
		
		if (tokens[Base.CHR].equals("chrX") || tokens[Base.CHR].equals("chrY")) {
			zygote = "Hom";
		}
		fm.writeLine(tokens[Base.CHR] + "\t" + tokens[Base.POS]+ "\t" + tokens[Base.POS]
		                 + "\t" + tokens[Base.REF] + "\t" + alleleBase
		                 + "\t" + zygote
		                 + "\t" + tokens[Base.COUNT_A] + "\t" + tokens[Base.COUNT_C]
		                 + "\t" + tokens[Base.COUNT_G] + "\t" + tokens[Base.COUNT_T]
		                 + "\t" + tokens[Base.QUAL_AVG]
		                 + "\t" + totalCount + "\t" + alleleCount
		                 + "\t" + String.format("%,.2f", af));
		numCount++;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			new SnpCall().printHelp();
		} else {
			new SnpCall().go(args);
		}
	}

}
