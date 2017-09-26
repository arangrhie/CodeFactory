package javax.arang.phasing;

import java.util.ArrayList;
import java.util.Collections;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.genome.util.Util;
import javax.arang.phasing.util.PhasedBlock;
import javax.arang.phasing.util.PhasedRead;

public class PhasedReadsToPhasedBlocks extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fmPhasedBlocks) {
		String line;
		String[] tokens;
		
		// Read frPhasedReads
		int seqStart;
		int seqEnd;
		int endHetPos = -1;
		ArrayList<PhasedBlock> phasedBlocks = new ArrayList<PhasedBlock>();
		PhasedBlock block = null;
		boolean isFirst = true;
		int countA;
		int countB;
		String haplotype;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			countA = Integer.parseInt(tokens[PhasedRead.NUM_A]);
			countB = Integer.parseInt(tokens[PhasedRead.NUM_B]);
			
			// SNPs are homo or else : undeterminable
			if (countA == 0 && countB == 0) {
				continue;
			}

			endHetPos = -1;
			haplotype = tokens[PhasedRead.HAPLOTYPE];
			for (int i = 0; i < haplotype.length(); i++) {
				if (haplotype.charAt(i) == 'A' || haplotype.charAt(i) == 'B') {
					endHetPos = Integer.parseInt(tokens[PhasedRead.SNP_POS_LIST + i]);
				}
			}
			
			// no het snp
			if (endHetPos == -1) {
				continue;
			}
			
			seqStart = Integer.parseInt(tokens[PhasedRead.START]);
			seqEnd =  Integer.parseInt(tokens[PhasedRead.END]);
			
			// phased block extension
			if (isFirst || block.getLastHetMarker() < seqStart) {
				if (!isFirst && block.getEnd() > seqStart) {
					block.setBlockEnd(seqStart - 1);
				}
				block = new PhasedBlock(chr, seqStart, endHetPos, seqEnd, tokens[PhasedRead.START]);
				phasedBlocks.add(block);
				isFirst = false;
			} else if (block.getLastHetMarker() < endHetPos) {
				block.setLastHetMarker(endHetPos);
			}
			
			if (block.getEnd() < seqEnd) {
				block.setBlockEnd(seqEnd);
			}
		}
		
		ArrayList<Double> blockArr = new ArrayList<Double>();
		System.out.println("Total num. of phased blocks: " + phasedBlocks.size());
		
		if (phasedBlocks.size() == 0) {
			System.err.println("No phased blocks available.");
			fmPhasedBlocks.closeMaker();
			System.exit(-1);
		}
		double lenSum = 0;
		double len;
		for (int i = 0; i < phasedBlocks.size(); i++) {
			block = phasedBlocks.get(i);
			len = block.getEnd() - block.getStart() + 1;
			blockArr.add(len);
			lenSum += len;
			fmPhasedBlocks.writeLine(block.getChr() + "\t" + (block.getStart() - 1) + "\t" + block.getEnd() + "\t" + block.getPS() + "\t" + len);
		}
		fmPhasedBlocks.closeMaker();
		Collections.sort(blockArr);
		double n50 = Util.getN50(blockArr, lenSum);
		System.out.println("Phased block coverage: " + String.format("%,.0f", lenSum));
		System.out.println("N50: " + String.format("%,.1f", n50));
		System.out.println("For simplicity; order is longest block size, block N50, num. blocks, genome covered bases:");
		System.out.println(blockArr.get(blockArr.size() - 1));
		System.out.println(n50);
		System.out.println(phasedBlocks.size());
		System.out.println(String.format("%.0f", lenSum));
		System.out.println();
	}

	private static String chr;
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingPhasedReadsToPhasedBlocks.jar <in.read> <out.bed> <chr>");
		System.out.println("\t<in.read>: generated with phasingSubreadBasedPhasedSNP.jar");
		System.out.println("\t<out.bed>: phased block bed");
		System.out.println("\t\tBlock Start: Seq start with no heterozygous SNP overlapping with previous block");
		System.out.println("\t\tBlock End: Last overlapped heterozygous SNP position");
		System.out.println("Arang Rhie, 2015-12-12. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			chr = args[2];
			new PhasedReadsToPhasedBlocks().go(args[0], args[1]);
		} else {
			new PhasedReadsToPhasedBlocks().printHelp();
		}
	}

}
