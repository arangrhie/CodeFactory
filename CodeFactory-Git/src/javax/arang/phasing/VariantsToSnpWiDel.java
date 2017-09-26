package javax.arang.phasing;

import java.util.ArrayList;
import java.util.Collections;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.genome.util.Util;
import javax.arang.phasing.util.PhasedSNP;

public class VariantsToSnpWiDel extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		fm.writeLine(fr.readLine() + "\tDistance");
		
		String prevChr = "";
		double pos;
		double prevPos = 1;
		double distance = 0;
		
		double distanceSum = 0;
		int numSnps = 0;
		ArrayList<Double> distanceArr = new ArrayList<Double>();
		double allSNPdistance = 0;
		double allSNPdistanceSum = 0;
		int allSNPnumSnps = 0;
		ArrayList<Double> allSNPdistanceArr = new ArrayList<Double>();
		
		double allSNPPos = 1;
		double allSNPprevPos = 1;
		boolean isFirstSnp = true;
		boolean isAllSNPFirstSnp = true;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			if (!tokens[PhasedSNP.CHR].equals(prevChr)) {
				isAllSNPFirstSnp = true;
				isFirstSnp = true;
			}
			if (tokens[PhasedSNP.HAPLOTYPE_A].length() == tokens[PhasedSNP.HAPLOTYPE_B].length()) {
				continue;
			}
			allSNPPos = Integer.parseInt(tokens[PhasedSNP.POS]);
			if (isAllSNPFirstSnp) {
				allSNPdistance = 0;
				isAllSNPFirstSnp = false;
			} else {
				allSNPdistance = allSNPPos - allSNPprevPos;
				allSNPdistanceArr.add(allSNPdistance);
			}
			if (tokens[PhasedSNP.HAPLOTYPE_A].length() == 1 && tokens[PhasedSNP.HAPLOTYPE_B].length() == 1) {
				pos = Double.parseDouble(tokens[PhasedSNP.POS]);
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
				prevChr = tokens[PhasedSNP.CHR];
				prevPos = pos;
			} else if (tokens[PhasedSNP.HAPLOTYPE_A].length() > tokens[PhasedSNP.HAPLOTYPE_B].length()) {
				pos = Integer.parseInt(tokens[PhasedSNP.POS]);
				if (isFirstSnp) {
					distance = 0;
					isFirstSnp = false;
				} else {
					distance = pos - prevPos;
					distanceArr.add(distance);
				}
				for (int i = (int) pos + 1; i < pos + tokens[PhasedSNP.HAPLOTYPE_A].length(); i++) {
					fm.writeLine(tokens[PhasedSNP.CHR] + "\t" + i + "\t"
							+ tokens[PhasedSNP.HAPLOTYPE_A].charAt((int) (i-pos)) + "\t" + "D" + "\t"
							+ tokens[PhasedSNP.PS] + "\t" + distance);
					numSnps++;
					distanceSum += distance;
					distance = 1;
				}
				prevChr = tokens[PhasedSNP.CHR];
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
		double n50 = Util.getN50(allSNPdistanceArr, allSNPdistanceSum);
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
		System.out.println("Usage: java -jar phasingVaraintstoSnpWiDel.jar <phased.snp> <out.phased_noins.snp>");
		System.out.println("\t<phased.snp>: Generated with ExtractPhasedSnp.jar");
		System.out.println("\t<out.phased_noins.snp>: Extract only single nucleotide base changes, with DELs");
		System.out.println("\t\tInsertions will be removed.");
		System.out.println("Arang Rhie, 2016-01-06. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new VariantsToSnpWiDel().go(args[0], args[1]);
		} else {
			new VariantsToSnpWiDel().printHelp();
		}
	}

}
