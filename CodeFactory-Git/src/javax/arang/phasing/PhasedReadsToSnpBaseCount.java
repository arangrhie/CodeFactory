package javax.arang.phasing;

import java.util.Arrays;
import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.genome.util.Util;

public class PhasedReadsToSnpBaseCount extends I2Owrapper {

	@Override
	public void hooker(FileReader frRead, FileReader frSNP, FileMaker fm) {
		String line;
		String[] tokens;
		fm.writeLine("#CHR\tPOS\tHapA\tHapB\tPS\tH\tA\tB\tO\t|\ta\tc\tg\tt\tD\t"
				+ "|\tFIRST_A\tFIRST_B\tHH\tAA\tBB\t"
				+ "BA\tAB\tHAorAH\tOAorAO\t"
				+ "HBorBH\tOBorBO\t"
				+ "OHorHO\tOO\tSINGLE");
		HashMap<Integer, PhasedSNP> posToSNPmap = PhasedSNP.readSNPsStoreSNPs(frSNP, false);
		Integer[] posList = posToSNPmap.keySet().toArray(new Integer[0]);
		Arrays.sort(posList);
		int numSNPs = posList.length;
		System.out.println("SNPs: " + numSNPs);
		
		HashMap<Integer, Integer[]> posToBaseCountMap = new HashMap<Integer, Integer[]>();
		for (int i = 0; i < numSNPs; i++) {
			posToBaseCountMap.put(posList[i], Util.initArr(23));
		}
		
		String haplotype;
		int haplotypeIdx;
		int prevBasePattern = 0;
		int basePattern;
		int baseType;
		Integer[] counts;
		boolean isFirst = true;
		while (frRead.hasMoreLines()) {
			line = frRead.readLine();
			tokens = line.split(RegExp.TAB);
			if (tokens.length <= PhasedRead.HAPLOTYPE)	continue;
			haplotype = tokens[PhasedRead.HAPLOTYPE];
			numSNPs = haplotype.length();
			if (numSNPs == 0)	continue;
			if (numSNPs == 1) {
				counts = posToBaseCountMap.get(Integer.parseInt(tokens[PhasedRead.SNP_POS_LIST]));
				if (counts == null) {
					continue;
				}
				baseType = getBaseType(haplotype.charAt(0));
				counts[baseType]++;
				counts[PhasedSNPBase.SINGLE]++;
			} else {
				haplotypeIdx = 0;
				isFirst = true;
				for (int posIdx = PhasedRead.SNP_POS_LIST; posIdx < PhasedRead.SNP_POS_LIST + numSNPs; posIdx++) {
					counts = posToBaseCountMap.get(Integer.parseInt(tokens[posIdx]));
					if (counts == null) {
						haplotypeIdx++;
						continue;
					}
					baseType = getBaseType(haplotype.charAt(haplotypeIdx));
					basePattern = getBasePattern(haplotype.charAt(haplotypeIdx));
					counts[baseType]++;
					if (isFirst) {
						if (baseType == PhasedSNPBase.A) {
							counts[PhasedSNPBase.FIRST_A]++;
						} else if (baseType == PhasedSNPBase.B) {
							counts[PhasedSNPBase.FIRST_B]++;
						}
						isFirst = false;
					} else {
						counts[getBasePattern(prevBasePattern, basePattern)]++;
					}
					prevBasePattern = basePattern;
					haplotypeIdx++;
				}
			}
		}
		
		PhasedSNP snp;
		for (int pos : posList) {
			snp = posToSNPmap.get(pos);
			counts = posToBaseCountMap.get(pos);
			fm.write(snp.getChr() + "\t" + pos + "\t"
					+ snp.getHaplotypeA() + "\t" + snp.getHaplotypeB() + "\t"
					+ snp.getPS() + "\t"
					+ counts[PhasedSNPBase.H] + "\t"
					+ counts[PhasedSNPBase.A] + "\t"
					+ counts[PhasedSNPBase.B] + "\t"
					+ (counts[PhasedSNPBase.D]
					+ counts[PhasedSNPBase.a]
					+ counts[PhasedSNPBase.c]
					+ counts[PhasedSNPBase.g]
					+ counts[PhasedSNPBase.t]) + "\t|\t"
					+ counts[PhasedSNPBase.a] + "\t"
					+ counts[PhasedSNPBase.c] + "\t"
					+ counts[PhasedSNPBase.g] + "\t"
					+ counts[PhasedSNPBase.t] + "\t"
					+ counts[PhasedSNPBase.D] + "\t|");
			for (int i = PhasedSNPBase.FIRST_A; i < counts.length; i++) {
				fm.write("\t" + counts[i]);
			}
			fm.writeLine();
		}
	}

	private int getBasePattern(int prevBasePattern, int basePattern) {
		if (prevBasePattern == basePattern) {
			switch (basePattern) {
			case PhasedSNPBase.H : return PhasedSNPBase.HH;
			case PhasedSNPBase.A : return PhasedSNPBase.AA;
			case PhasedSNPBase.B : return PhasedSNPBase.BB;
			case PhasedSNPBase.O : return PhasedSNPBase.OO;
			}
		} else {
			switch (prevBasePattern) {
				case PhasedSNPBase.A: {
					switch (basePattern) {
					case PhasedSNPBase.B : return PhasedSNPBase.AB;
					case PhasedSNPBase.O : return PhasedSNPBase.OA_or_AO;
					case PhasedSNPBase.H : return PhasedSNPBase.HA_or_AH;
					}
				}
				case PhasedSNPBase.B : {
					switch (basePattern) {
					case PhasedSNPBase.A : return PhasedSNPBase.BA;
					case PhasedSNPBase.O : return PhasedSNPBase.OB_or_BO;
					case PhasedSNPBase.H : return PhasedSNPBase.HB_or_BH;
					}
				}
				case PhasedSNPBase.O : {
					switch (basePattern) {
					case PhasedSNPBase.A : return PhasedSNPBase.OA_or_AO;
					case PhasedSNPBase.B : return PhasedSNPBase.OB_or_BO;
					case PhasedSNPBase.H : return PhasedSNPBase.OH_or_HO;
					}
				}
				case PhasedSNPBase.H : {
					switch (basePattern) {
					case PhasedSNPBase.A : return PhasedSNPBase.HA_or_AH;
					case PhasedSNPBase.B : return PhasedSNPBase.HB_or_BH;
					case PhasedSNPBase.O : return PhasedSNPBase.OH_or_HO;
					}
				}
			}
		}
		return -1;
	}

	private int getBaseType(char base) {
		switch (base) {
		case 'H': return PhasedSNPBase.H;
		case 'A': return PhasedSNPBase.A;
		case 'B': return PhasedSNPBase.B;
		case 'a': return PhasedSNPBase.a;
		case 'c': return PhasedSNPBase.c;
		case 'g': return PhasedSNPBase.g;
		case 't': return PhasedSNPBase.t;
		case 'D': return PhasedSNPBase.D;
		}
		return -1;
	}
	
	private int getBasePattern (char base) {
		switch (base) {
		case 'H': return PhasedSNPBase.H;
		case 'A': return PhasedSNPBase.A;
		case 'B': return PhasedSNPBase.B;
		}
		return PhasedSNPBase.O;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingPhasedReadsToSnpBaseCount.jar <in.read> <in.snp> <out.snp>");
		System.out.println("\t<in.read> and <in.snp>: generated with phasingSubreadBasedPhasedSNP_v5.jar");
		System.out.println("\t<out.snp>: CHR\tPOS\tHapA\tHapB\tPS\tH\tA\tB\tO\t"
				+ "|\ta\tc\tg\tt\tD\t"
				+ "|\tFIRST_A\tFIRST_B\tHH\tAA\tBB\t"
				+ "BA\tAB\tHAorAH\tOAorAO\t"
				+ "HBorBH\tOBorBO\t"
				+ "OHorHO\tOO\tSINGLE");
		System.out.println("Arang Rhie, 2015-08-06. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new PhasedReadsToSnpBaseCount().go(args[0], args[1], args[2]);
		} else {
			new PhasedReadsToSnpBaseCount().printHelp();
		}
	}

}
