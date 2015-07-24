package javax.arang.phasing;

import java.util.ArrayList;
import java.util.Collections;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.genome.util.Util;

public class VariantsToSnp extends IOwrapper {

	private static short CHR = 0;
	private static short POS = 1;
	private static short HAPLOTYPE_A = 2;
	private static short HAPLOTYPE_B = 3;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		fm.writeLine(fr.readLine() + "\tDistance");
		
		String prevChr = "";
		int pos;
		int prevPos = 1;
		int distance = 0;
		
		double distanceSum = 0;
		int numSnps = 0;
		ArrayList<Integer> distanceArr = new ArrayList<Integer>();
		int allSNPdistance = 0;
		double allSNPdistanceSum = 0;
		int allSNPnumSnps = 0;
		ArrayList<Integer> allSNPdistanceArr = new ArrayList<Integer>();
		
		int allSNPPos = 1;
		int allSNPprevPos = 1;
		boolean isFirstSnp = true;
		boolean isAllSNPFirstSnp = true;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			if (!tokens[CHR].equals(prevChr)) {
				isAllSNPFirstSnp = true;
				isFirstSnp = true;
			}
			allSNPPos = Integer.parseInt(tokens[POS]);
			if (isAllSNPFirstSnp) {
				allSNPdistance = 0;
				isAllSNPFirstSnp = false;
			} else {
				allSNPdistance = allSNPPos - allSNPprevPos;
				allSNPdistanceArr.add(allSNPdistance);
			}
			if (tokens[HAPLOTYPE_A].length() == 1 && tokens[HAPLOTYPE_B].length() == 1) {
				pos = Integer.parseInt(tokens[POS]);
				if (isFirstSnp) {
					distance = 0;
					isFirstSnp = false;
				} else {
					distance = pos - prevPos;
					distanceArr.add(distance);
				}
				fm.writeLine(line + "\t" + distance);
				numSnps++;
				distanceSum += distance;
				prevChr = tokens[CHR];
				prevPos = pos;
			}
			allSNPdistanceSum += allSNPdistance;
			allSNPnumSnps++;
			allSNPprevPos = allSNPPos;
		}
		
		System.out.println("All variants");
		System.out.println("Total num. All SNPs: " + String.format("%,d", allSNPnumSnps));
		System.out.println("Total covered genome size: " + String.format("%,.0f", allSNPdistanceSum));
		System.out.println("SNPs are almost on every " + (int) (allSNPdistanceSum / allSNPnumSnps) + " bp");
		Collections.sort(allSNPdistanceArr);
		int n50 = Util.getN50(allSNPdistanceArr, allSNPdistanceSum);
		System.out.println("Distance N50 " + n50 + " bp");
		
		System.out.println();
		
		System.out.println("SNPs");
		System.out.println("Total num. SNPs: " + String.format("%,d", numSnps));
		System.out.println("Total covered genome size: " + String.format("%,.0f", distanceSum));
		System.out.println("SNPs are almost on every " + (int) (distanceSum / numSnps) + " bp");
		
		Collections.sort(distanceArr);
		n50 = Util.getN50(distanceArr, distanceSum);
		System.out.println("Distance N50 " + n50 + " bp");
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingVaraintstoSnp.jar <phased.snp> <out.phased_noindel.snp>");
		System.out.println("\t<phased.snp>: Generated with ExtractPhasedSnp.jar");
		System.out.println("\t<out.phased_noindel.snp>: Extract only single nucleotide base changes, with no indels");
		System.out.println("Arang Rhie, 2015-07-16. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new VariantsToSnp().go(args[0], args[1]);
		} else {
			new VariantsToSnp().printHelp();
		}
	}

}
