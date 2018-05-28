package mummer.nucmer;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;
import javax.arang.bed.util.Region;

public class MergeAlignments extends R2wrapper {

	@Override
	public void hooker(FileReader frNucmer, FileReader frGap) {
		
		String line;
		String[] tokens;
		Bed gaps = new Bed(frGap);
		
		boolean isFirstBlock = true;
		
		// 1: ref coords
		// 2: qry coords
		String prevChr1 = "";
		String chr1 = "";
		int prevChr1len = 0;
		int prevChr2len = 0;
		String prevChr2 = "";
		String chr2 = "";
		int coords1start = 0;
		int coords1end = 0;
		int coords2start = 0;
		int coords2end = 0;
		int tmp;
		
		int block1start = 0;
		int block1end = 0;
		int block2start = 0;
		int block2end = 0;
		
		while (frNucmer.hasMoreLines()) {
			line = frNucmer.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			
			chr1 = tokens[tokens.length - 2];
			chr2 = tokens[tokens.length - 1];
			
			if (isFirstBlock || !prevChr1.equals(chr1) || !prevChr2.equals(chr2)) {
				if (!isFirstBlock) {
					outputBlock(prevChr1, prevChr2, block1start, block2start, block1end, block2end, prevChr1len, prevChr2len);
					//System.out.println(prevChr1 + "\t" + block1start + "\t" + block1end + "\t" + prevChr2 + "\t" + block2start + "\t" + block2end + "\talign\t800\t+\t+");
				}
				
				// initialize
				prevChr1 = tokens[tokens.length - 2];
				prevChr2 = tokens[tokens.length - 1];
				prevChr1len = Integer.parseInt(tokens[tokens.length - 4]);
				prevChr2len = Integer.parseInt(tokens[tokens.length - 3]);
				chr1 = prevChr1;
				chr2 = prevChr2;
				
				coords1start = Integer.parseInt(tokens[COORDS.R_START]) - 1;
				coords2start = Integer.parseInt(tokens[COORDS.Q_START]) - 1;
				
				coords1end = Integer.parseInt(tokens[COORDS.R_END]);
				coords2end = Integer.parseInt(tokens[COORDS.Q_END]);
				
				if (coords2start > coords2end) {
					tmp = coords2end;
					coords2end = coords2start;
					coords2start = tmp;
				}
				
				block1start = coords1start;
				block1end = coords1end;
				block2start = coords2start;
				block2end = coords2end;
				isFirstBlock = false;
				continue;
			}
			
			coords1start = Integer.parseInt(tokens[COORDS.R_START]) - 1;
			coords2start = Integer.parseInt(tokens[COORDS.Q_START]) - 1;
			coords1end = Integer.parseInt(tokens[COORDS.R_END]);
			coords2end = Integer.parseInt(tokens[COORDS.Q_END]);
			
			if (coords2start > coords2end) {
				tmp = coords2end;
				coords2end = coords2start;
				coords2start = tmp;
			}
			
			if (gaps.hasRegion(chr1, block1end, coords1start) || gaps.hasRegion(chr2, block2end, coords2start) || block1end > coords1start || block2end > coords2start) {
				outputBlock(prevChr1, prevChr2, block1start, block2start, block1end, block2end, prevChr1len, prevChr2len);
				block1start = coords1start;
				block1end = coords1end;
				block2start = coords2start;
				block2end = coords2end;
			} else {
				block1end = coords1end;
				block2end = coords2end;
			}
		}
		outputBlock(prevChr1, prevChr2, block1start, block2start, block1end, block2end, prevChr1len, prevChr2len);
	}

	
	private void outputBlock(String chr1, String chr2, int start1, int start2, int end1, int end2, int chr1len, int chr2len) {
		System.out.println((start1 + 1) + "\t" + end1 + "\t" + (start2 + 1) + "\t" + end2 + "\t" + (end1 - start1) + "\t" + (end2 - start2) + "\t" + 99.00 + "\t" + chr1len + "\t" + chr2len + "\t" + chr1 + "\t" + chr2);
	}
	
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar nucmerMergeAlignments.jar <nucmer.coords> <gaps.bed>");
		System.out.println("\tMerge alignments not split by gaps");
		System.out.println("\t<nucmer.coords>: any show-coords with -H (no header) turned on");
		System.out.println("\t<gaps.bed>: java -jar fastaGetGaps.jar");
		System.out.println("\t<stdout>: filtered <nucmer.coords>, which gives the 'alignment block's without the gaps. scores = 800, strand = all +");
		System.out.println("Arang Rhie, 2018-02-12. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new MergeAlignments().go(args[0], args[1]);
		} else {
			new MergeAlignments().printHelp();
		}
	}

}
