/**
 * 
 */
package javax.arang.snp.allele;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class AddAlleleFrequency extends I2Owrapper {

	@Override
	public void hooker(FileReader frCov, FileReader frRef, FileMaker fm) {
		
		frCov.readLine();
		String lineCov;
		String[] covTokens;
		
		String lineRef;
		String[] refTokens;
		
		
		int CHR = 0;
		int POS = 1;
		
		int REF = 3;
		int SNP = 4;	
		
		int A_COUNT = 2;
		int C_COUNT = 3;
		int G_COUNT = 4;
		int T_COUNT = 5;
		int A_QUAL = 6;
		int C_QUAL = 7;
		int G_QUAL = 8;
		int T_QUAL = 9;
//		int BASE = 10;
//		int QUALITY = 11;
//		int URM = 12;
		
		while (frCov.hasMoreLines()) {
			lineCov = frCov.readLine();
			covTokens = lineCov.split("\t");
			
			lineRef = frRef.readLine();
			refTokens = lineRef.split("\t");
			
			if (!covTokens[CHR].equals(refTokens[CHR]) && !covTokens[POS].equals(refTokens[POS])) {
				System.out.println("Position differs. Check " + frCov.getFileName() + " and " + frRef.getFileName() + " chr and position.");
				break;
			}
			
			int countA = Integer.parseInt(covTokens[A_COUNT]);
			int countC = Integer.parseInt(covTokens[C_COUNT]);
			int countG = Integer.parseInt(covTokens[G_COUNT]);
			int countT = Integer.parseInt(covTokens[T_COUNT]);
			int refCount = getAlleleCount(refTokens[REF], countA, countC, countG, countT);
			int alleleCount = getAlleleCount(refTokens[SNP], countA, countC, countG, countT);
			
			int qualA = Integer.parseInt(covTokens[A_QUAL]);
			int qualC = Integer.parseInt(covTokens[C_QUAL]);
			int qualG = Integer.parseInt(covTokens[G_QUAL]);
			int qualT = Integer.parseInt(covTokens[T_QUAL]);
			if (refCount + alleleCount < 5) {
				// totalCount < 5
				fm.writeLine(lineRef + "\t" + "NA" + "\t" + "NA");
				continue;
			}
			
			float af = ((float)alleleCount*100) / (refCount + alleleCount);
			if (af < 20) {
				fm.writeLine(lineRef + "\t" + String.format("%,.2f", af) + "\t0");
			} else {
				// qual
				if (getAlleleCount(refTokens[SNP], qualA, qualC, qualG, qualT) < 20) {
					fm.writeLine(lineRef + "\t" + "NA" + "\t" + "NA");
					continue;
				}
				if (af < 80 || covTokens[CHR].equals("chrX")) {
					fm.writeLine(lineRef + "\t" + String.format("%,.2f", af) + "\t1");
				} else {
					fm.writeLine(lineRef + "\t" + String.format("%,.2f", af) + "\t2");
				}
			} 
		}
	}
	
	public int getAlleleCount(String base, int countA, int countC, int countG, int countT) {
		if (base.equals("A"))	return countA;
		else if (base.equals("C"))	return countC;
		else if (base.equals("G"))	return countG;
		else if (base.equals("T"))	return countT;
		else return -1;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar addAlleleFreq.jar <cov> <ref_bases>");
		System.out.println("\t<cov>: tab delemeted file; chr\tpos\tuniqueA/C/G/T\tuniqueA/C/G/Tqual\tbase\tquality\turm");
		System.out.println("\t<ref_bases>: chr\tpos\tpos\tref\tsnp_allele");
		System.out.println("\t<out>: <in>.afq with allele_freq and NA/0/1/2.");
		System.out.println("\t\tAdd a tab with allele frequency using unique >= 5, qual >= 20. Het/Hom encoded as 1/2.");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new AddAlleleFrequency().go(args[0], args[1], args[0] + ".afq");
		} else {
			new AddAlleleFrequency().printHelp();
		}
	}

}
