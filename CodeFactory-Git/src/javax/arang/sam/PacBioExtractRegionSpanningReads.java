package javax.arang.sam;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;
import javax.arang.genome.util.Util;

public class PacBioExtractRegionSpanningReads extends I2Owrapper {

	@Override
	public void hooker(FileReader frSam, FileReader frBed, FileMaker fmSam) {
		Bed targetRegions = new Bed(frBed);
		HashMap<String, Integer> targetRegionIdToSpannedNumCount = new HashMap<String, Integer>();
		// targetRegionId: chr_idx
		String line;
		String[] tokens;
		
		// absolute start pos and end pos of read alignment
		int readStart;
		int readEnd;
		
		int regionStartIdx;
		int regionEndIdx;
		
//		String[] seqData;
		String contig;
		ArrayList<Integer> starts = new ArrayList<Integer>();
		ArrayList<Integer> ends = new ArrayList<Integer>();
		ArrayList<String> notes;
		String key;
		boolean isSpanned = false;
		
		FileMaker fmSpan = new FileMaker(prefix + ".span");
		
		//System.out.println("#RegionIdx(0-based)\treadStart\tstarts.get(regionIdx)\tends.get(regionIdx)\treadEnd\tReadName\tM\tD\tI");
		while (frSam.hasMoreLines()) {
			line = frSam.readLine();
			if (line.startsWith("@")) {
				fmSam.writeLine(line);
				continue;
			}
			tokens = line.split(RegExp.TAB);
			contig = tokens[Sam.RNAME];
			readStart = Integer.parseInt(tokens[Sam.POS]);
			readEnd = readStart + SAMUtil.getMatchedBases(tokens[Sam.CIGAR]) - 1;
			
			starts = targetRegions.getStarts(contig);
			ends = targetRegions.getEnds(contig);
			notes = targetRegions.getNotes(contig);
			regionStartIdx = Util.getRegionEndIdxContainingPos(ends, readStart + 1);
			regionEndIdx = Util.getRegionStartIdxContainingPos(starts, readEnd - 1);
			
			isSpanned = false;
			int[] mdi;
			if (regionStartIdx > -1 && regionEndIdx > -1 && regionStartIdx <= regionEndIdx
					&& readStart <= starts.get(regionStartIdx)
					&& ends.get(regionEndIdx) < readEnd) {
				for (int idx = regionStartIdx; idx <= regionEndIdx; idx++) {
					mdi = SAMUtil.getMDI(readStart, tokens[Sam.CIGAR], starts.get(idx), ends.get(idx));
					fmSpan.writeLine(contig + "\t" + starts.get(idx) + "\t" + ends.get(idx) + "\t" +  notes.get(idx) + "\t" + readStart + "\t" + readEnd + "\t"
							+ tokens[Sam.QNAME] + "\t" + mdi[0] + "\t" + mdi[1] + "\t" + mdi[2]);
					isSpanned = true;
					key = contig + "_" + idx;
					if (targetRegionIdToSpannedNumCount.containsKey(key)) {
						targetRegionIdToSpannedNumCount.put(key, targetRegionIdToSpannedNumCount.get(key) + 1);
					} else {
						targetRegionIdToSpannedNumCount.put(key, 1);
					}
				}
			}
			if (isSpanned) {
				fmSam.writeLine(line);
			}
		}
		
		// Write down num. of spanned reads
		FileMaker fmBed = new FileMaker(prefix + ".span.bed");
		ArrayList<String> contigs = targetRegions.getChrStringList();
		for (int i = 0; i < contigs.size(); i++) {
			contig = contigs.get(i);
			starts = targetRegions.getStarts(contig);
			ends = targetRegions.getEnds(contig);
			notes = targetRegions.getNotes(contig);
			for (int idx = 0; idx < starts.size(); idx++) {
				if (targetRegionIdToSpannedNumCount.containsKey(contig + "_" + idx)) {
					fmBed.writeLine(contig + "\t" + starts.get(idx) + "\t" + ends.get(idx) + "\t" + notes.get(idx) + "\t" + targetRegionIdToSpannedNumCount.get(contig + "_" + idx));
				} else {
					fmBed.writeLine(contig + "\t" + starts.get(idx) + "\t" + ends.get(idx) + "\t" + notes.get(idx) + "\t0");
				}
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samPacBioExtractRegionSpanningReads.jar <in.sam> <region.bed> <out.prefix>");
		System.out.println("\t<in.sam>: subreads aligned with blasr");
		System.out.println("\t<region.bed>: sorted region specified in bed format");
		System.out.println("\t<out.prefix>.sam: < (region start-1) and > (end+1) will be checked if it is in a M cigar part");
		System.out.println("\t<out.prefix>.span: <region.bed>\treadStart\treadEnd\tReadName\tM\tD\tI (MDI cigar bases in region)");
		System.out.println("\t\tMultiple lines containing spanned read information with same <region.bed> lines are expected.");
		System.out.println("\t<out.prefix>.span.bed: <region.bed> with the number of spanning read counts");
		System.out.println("Arang Rhie, 2016-01-02. arrhie@gmail.com");
	}

	private static String prefix = "";
	public static void main(String[] args) {
		if (args.length == 3) {
			prefix = args[2];
			new PacBioExtractRegionSpanningReads().go(args[0], args[1], prefix + ".sam");
		} else {
			new PacBioExtractRegionSpanningReads().printHelp();
		}
	}

}
