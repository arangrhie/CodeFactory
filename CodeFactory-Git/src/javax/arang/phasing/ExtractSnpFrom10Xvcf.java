package javax.arang.phasing;

import java.util.ArrayList;
import java.util.Collections;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.genome.util.Util;
import javax.arang.vcf.VCF;

public class ExtractSnpFrom10Xvcf extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		FileMaker phasedBed = new FileMaker(blockBed);
		
		String line;
		String[] tokens;
		
		String chr;
		String prevChr = "";
		double pos = -1;
		String ref;
		String alt;
		String gt;
		String ps;
		String[] sampleFields;
		String prevPs = "";
		
		double phasedStart = 1;
		double phasedEnd = 1;
		double len;
		ArrayList<Double> phasedBlockLenArr = new ArrayList<Double>();
		
		double phasedBlockLenSum = 0;
		int blockCount = 0;
		
		fm.writeLine("#CHROM\tPOS\tHaplotypeA\tHaplotypeB\tPS");
		phasedBed.writeLine("#CHROM\tSTART\tEND\tPS\tLEN");
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			chr = tokens[VCF.CHROM];
			pos = Double.parseDouble(tokens[VCF.POS]); 	// 1-base
			ref = tokens[VCF.REF];
			alt = tokens[VCF.ALT];
			sampleFields = tokens[VCF.SAMPLE].split(":");
			if (sampleFields.length < 3) {
				continue;
			}
			gt = sampleFields[0];	// genotype
			ps = sampleFields[2];	// phased block id
			
			// New phase block
			if (!ps.equals(prevPs)) {
				if ((phasedEnd - phasedStart + 1) > 1) {
					len = (phasedEnd - phasedStart + 1);
					phasedBed.writeLine(prevChr + "\t" + (phasedStart - 1) + "\t" + phasedEnd  + "\t" + prevPs + "\t" + len);	// 0-base, 1-base
					phasedBlockLenSum += len;
					blockCount++;
					phasedBlockLenArr.add(len);
				}
				phasedStart = pos;
				phasedEnd = pos;
			} else {
				phasedEnd = pos;
			}
			if (gt.contains("|")) {
				if (ref.length() < alt.length()) {	// ins
					continue;
				} else if (ref.length() > alt.length()) {	// del
					int delLen = ref.length();
					String tmpgt;
					for (int i = 1; i < delLen; i++) {
						 tmpgt = getGenotype(gt, ref.charAt(i) + "", "D");
						if (!tmpgt.equals("na")) {
							fm.writeLine(chr + "\t" + (pos + i) + "\t" + tmpgt + "\t" + ps);
						}
					}
				} else {
					// skip phased but uncertain variations: Exclude form marker
					gt = getGenotype(gt, ref, alt);
					if (!gt.equals("na")) {
						fm.writeLine(chr + "\t" + pos + "\t" + gt + "\t" + ps);
					}
				}
			}
			prevPs = ps;
			prevChr = chr;
		}
		
		// Write the last ps
		if ((phasedEnd - phasedStart + 1) > 1) {
			len = (phasedEnd - phasedStart + 1);
			phasedBed.writeLine(prevChr + "\t" + (phasedStart - 1) + "\t" + phasedEnd  + "\t" + prevPs + "\t" + len);	// 0-base, 1-base
			phasedBlockLenSum += len;
			blockCount++;
			phasedBlockLenArr.add(len);
		}
		
		phasedBed.closeMaker();
		
		System.out.println("Total sum of block len: " + String.format("%,.0f", phasedBlockLenSum) + " bp");
		System.out.println("Num. of phased blocks: " + blockCount);
		System.out.println("Average block len: " + String.format("%,d", (int)(phasedBlockLenSum / blockCount)) + " bp");
		
		Collections.sort(phasedBlockLenArr);
		double n50 = Util.getN50(phasedBlockLenArr, phasedBlockLenSum);
		System.out.println("Phased block N50: " + String.format("%,.0f",n50) + " bp");
	}

	private String getGenotype(String gt, String ref, String alt) {
		String gtA = gt.substring(0, gt.indexOf("|"));
		String gtB = gt.substring(gt.indexOf("|") + 1);
		
		String out = "na";
		
		if (gtA.equals("0")) {
			out = ref;
		} else if (gtA.equals("1")) {
			out = alt;
		} else {
			return "na";
		}
		
		if (gtB.equals("0")) {
			out = out + "\t" + ref;
		} else if (gtB.equals("1")) {
			out = out + "\t" + alt;
		} else {
			out = "na";
		}
		
		return out;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingExtractSnpFrom10Xvcf.jar <phased.vcf> <out.phased.snp> <out.block.bed>");
		System.out.println("\t<phased.vcf>: 10x genomics phased data");
		System.out.println("\t<out.phased.snp>: Phasing marker SNPs. Del included. CHR POS HaplotypeA_allele HaplotypeB_allele");
		System.out.println("\t<out.block.bed>: phased blocks by PS");
		System.out.println("\t\tEach phased block are based on continues phased snps. If non-phased genotype occurs,");
		System.out.println("Arang Rhie, 2016-07-21. arrhie@gmail.com");
	}

	private static String blockBed = "";
	public static void main(String[] args) {
		if (args.length == 3) {
			blockBed = args[2];
			new ExtractSnpFrom10Xvcf().go(args[0], args[1]);
		} else {
			new ExtractSnpFrom10Xvcf().printHelp();
		}
	}

}

