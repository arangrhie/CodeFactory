/**
 * 
 */
package javax.arang.vcf;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.genome.util.Util;

/**
 * @author Arang Rhie
 *
 */
public class EvaluatePerSample extends IOwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;

		/***
		 * Table for genotypes.
		 * 		sample1	sample2	...
		 * Het
		 * Hom
		 * N/A
		 * Ti
		 * Tv
		 */
		Integer[][] genotypes = new Integer[5][];
		Integer[][] genotypesAuto = new Integer[5][];
		Integer[][] genotypesChrX = new Integer[5][];
		final int HET = 0;
		final int HOM = 1;
		final int NA = 2;
		final int TI = 3;
		final int TV = 4;
		int numSamples = 0;
		String gt;
		boolean isAuto = false;
		boolean isChrX = false;
		boolean isTransition = false;
		
		String samples = "";
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("##")) {
				// Skip header lines
				continue;
			} else if (line.startsWith("#")) {
				// Header line containing sample names
				tokens = line.split("\t");
				
				for (int i = VCF.SAMPLE; i < tokens.length; i++) {
					samples = samples + "\t" + tokens[i];
					numSamples++;
				}
				
				// Initialize genotypes table
				for (int i = 0; i < 5; i++) {
					genotypes[i] = new Integer[numSamples];
					genotypesAuto[i] = new Integer[numSamples];
					genotypesChrX[i] = new Integer[numSamples];
					for (int j = 0; j < numSamples; j++) {
						genotypes[i][j]	= 0;
						genotypesAuto[i][j] = 0;
						genotypesChrX[i][j] = 0;
					}
				}
				continue;
			}
			tokens = line.split("\t");
			
			// Is Ti or Tv?
			isTransition = VCF.isTransition(tokens[VCF.REF], tokens[VCF.ALT]);
			
			// Is Autosome
			if (Util.isAutosome(tokens[VCF.CHROM]))	isAuto = true;
			else	isAuto = false;
			
			// Is ChrX
			if (Util.isChrX(tokens[VCF.CHROM]))	isChrX = true;
			else isChrX = false;
			
			// Get the genotypes
			for (int i = 0; i < numSamples; i++) {
				gt = VCF.parseGT(tokens[VCF.FORMAT], tokens[i + VCF.SAMPLE]);
				if (gt.equals("1")) {
					genotypes[HET][i]++;
					if (isAuto) genotypesAuto[HET][i]++;
					if (isChrX)	genotypesChrX[HET][i]++;
					if (isTransition) {
						genotypes[TI][i]++;
						if (isAuto) genotypesAuto[TI][i]++;
						if (isChrX)	genotypesChrX[TI][i]++;
					} else {
						genotypes[TV][i]++;
						if (isAuto) genotypesAuto[TV][i]++;
						if (isChrX)	genotypesChrX[TV][i]++;
					}
				} else if (gt.equals("2")) {
					genotypes[HOM][i]++;
					if (isAuto) genotypesAuto[HOM][i]++;
					if (isChrX)	genotypesChrX[HOM][i]++;
					if (isTransition) {
						genotypes[TI][i]++;
						if (isAuto) genotypesAuto[TI][i]++;
						if (isChrX)	genotypesChrX[TI][i]++;
					} else {
						genotypes[TV][i]++;
						if (isAuto) genotypesAuto[TV][i]++;
						if (isChrX)	genotypesChrX[TV][i]++;
					}
				} else if (gt.equals("NA")) {
					genotypes[NA][i]++;
					if (isAuto) genotypesAuto[NA][i]++;
					if (isChrX)	genotypesChrX[NA][i]++;
				}
				
			}
		}
		
		// Print the result
		System.out.println("All" + samples);
		fm.writeLine("All" + samples);
		writeLine(fm, "Total # of SNPs", genotypes[HET], genotypes[HOM]);
		writeLine(fm, "Het", genotypes[HET]);
		writeLine(fm, "Hom", genotypes[HOM]);
		writeLine(fm, "N/A", genotypes[NA]);
		writeFloatLine(fm, "Het/Hom", genotypes[HET], genotypes[HOM]);
		writeLine(fm, "Ti", genotypes[TI]);
		writeLine(fm, "Tv", genotypes[TV]);
		writeFloatLine(fm, "Ti/Tv", genotypes[TI], genotypes[TV]);
		System.out.println();
		fm.writeLine();
		
		System.out.println("Autosome" + samples);
		fm.writeLine("Autosome" + samples);
		writeLine(fm, "Total # of SNPs", genotypesAuto[HET], genotypesAuto[HOM]);
		writeLine(fm, "Het", genotypesAuto[HET]);
		writeLine(fm, "Hom", genotypesAuto[HOM]);
		writeLine(fm, "N/A", genotypesAuto[NA]);
		writeFloatLine(fm, "Het/Hom", genotypesAuto[HET], genotypesAuto[HOM]);
		writeLine(fm, "Ti", genotypesAuto[TI]);
		writeLine(fm, "Tv", genotypesAuto[TV]);
		writeFloatLine(fm, "Ti/Tv", genotypesAuto[TI], genotypesAuto[TV]);
		System.out.println();
		fm.writeLine();
		
		System.out.println("ChrX" + samples);
		fm.writeLine("ChrX" + samples);
		writeLine(fm, "Total # of SNPs", genotypesChrX[HET], genotypesChrX[HOM]);
		writeLine(fm, "Het", genotypesChrX[HET]);
		writeLine(fm, "Hom", genotypesChrX[HOM]);
		writeLine(fm, "N/A", genotypesChrX[NA]);
		writeFloatLine(fm, "Het/Hom", genotypesChrX[HET], genotypesChrX[HOM]);
		writeLine(fm, "Ti", genotypesChrX[TI]);
		writeLine(fm, "Tv", genotypesChrX[TV]);
		writeFloatLine(fm, "Ti/Tv", genotypesChrX[TI], genotypesChrX[TV]);

	}
	
	private void writeLine(FileMaker fm, String category, Integer[] value) {
		int numSamples = value.length;
		System.out.print(category);
		fm.write(category);
		for (int i = 0; i < numSamples; i++) {
			System.out.print("\t" + String.format("%,8d", value[i]));
			fm.write("\t" + String.format("%,8d", value[i]));
		}
		System.out.println();
		fm.writeLine();
	}
	
	private void writeLine(FileMaker fm, String category, Integer[] value1, Integer[] value2) {
		int numSamples = value1.length;
		System.out.print(category);
		fm.write(category);
		for (int i = 0; i < numSamples; i++) {
			System.out.print("\t" + String.format("%,8d", value1[i] + value2[i]));
			fm.write("\t" + String.format("%,8d", value1[i] + value2[i]));
		}
		System.out.println();
		fm.writeLine();
	}
	
	private void writeFloatLine(FileMaker fm, String category, Integer[] value1, Integer[] value2) {
		int numSamples = value1.length;
		System.out.print(category);
		fm.write(category);
		for (int i = 0; i < numSamples; i++) {
			System.out.print("\t" + String.format("%,3.4f", (float) value1[i] / value2[i]));
			fm.write("\t" + String.format("%,3.4f", (float) value1[i] / value2[i]));
		}
		System.out.println();
		fm.writeLine();
	}
	
	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar vcfEvaluatePerSample.jar <in.vcf>");
		System.out.println("\t<in.vcf>: Input .vcf file. Make sure that header line is included with sample names.");
		System.out.println("\t<out>: <in.stat> with Total # of SNPs, Het, Hom, N/A, Het/Nom, Ti, Tv, Ti/Tv on All / Autosomes / ChrX.");
		System.out.println("\tArang Rhie, 2014-03-18. arrhie@gmail.com");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new EvaluatePerSample().go(args[0], args[0].replace(".vcf", ".stat"));
		} else {
			new EvaluatePerSample().printHelp();
		}
	}

}
