package javax.arang.falcon;

import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ExtractOverlapsFromPreads extends I2Owrapper {

	private static int FLANKED = 10;
	private static int OVERLAPPED = 40;
	
	@Override
	public void hooker(FileReader frCtgTilingPath, FileReader fr2, FileMaker fm) {
		
		HashMap<String, String> preadFastaMap = new HashMap<String, String>();
		HashMap<String, Contig> preadObjMap = new HashMap<String, Contig>();
		
		String contig;
		String pread1;
		String pread2;
		
		int edgeStart;
		int edgeEnd;
		int overlap;
		int tmp;
		
		String line;
		String[] tokens;
		
		boolean isPread1DirectionReversed = false;
		boolean isPread2DirectionReversed = false;
		
		while (fr2.hasMoreLines()) {
			line = fr2.readLine();
			tokens = line.split(RegExp.TAB);
			preadFastaMap.put(tokens[0], tokens[1]);
		}
		
		String pread1OvlLeftEnd;
		String pread1OvlLeft;
		String pread1OvlRight;
		
		String pread2OvlRightEnd;
		String pread2OvlLeft;
		String pread2OvlRight;
		int left;
		//int right;
		
		StringBuffer blank = new StringBuffer();
		for (int i = 0; i < FLANKED; i++) {
			blank.append(" ");
		}
		
		while (frCtgTilingPath.hasMoreLines()) {
			line = frCtgTilingPath.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			
			// Set variables
			contig = tokens[Falcon.CONTIG];
			pread1 = tokens[Falcon.NODE_BEGIN];
			pread2 = tokens[Falcon.NODE_END];
			
			if (pread1.endsWith(":B")) {
				isPread1DirectionReversed = true;
			} else {
				isPread1DirectionReversed = false;
			}
			
			if (pread2.endsWith(":B")) {
				isPread2DirectionReversed = true;
			} else {
				isPread2DirectionReversed = false;
			}
			
			pread1 = pread1.substring(0, pread1.indexOf(":"));
			pread2 = pread2.substring(0, pread2.indexOf(":"));
			edgeStart = Integer.parseInt(tokens[Falcon.EDGE_START]);
			edgeEnd = Integer.parseInt(tokens[Falcon.EDGE_END]);
			overlap = Integer.parseInt(tokens[Falcon.OVERLAP]);
			
			if (edgeStart > edgeEnd) {
				tmp = edgeEnd;
				edgeEnd = edgeStart;
				edgeStart = tmp;
			}
			
			Contig pread1Contig;
			if (!preadObjMap.containsKey(pread1)) {
				pread1Contig = getContigFromFasta(pread1, preadFastaMap);
				preadObjMap.put(pread1, pread1Contig);
			}
			pread1Contig = preadObjMap.get(pread1);
			
			Contig pread2Contig;
			if (!preadObjMap.containsKey(pread2)) {
				pread2Contig = getContigFromFasta(pread2, preadFastaMap);
				preadObjMap.put(pread2, pread2Contig);
			}
			pread2Contig = preadObjMap.get(pread2);
			
			if (isPread1DirectionReversed) {
				left = Math.min(pread1Contig.getSeqLen() - FLANKED, overlap);
				pread1OvlLeftEnd = pread1Contig.getReversedSeq(overlap, overlap + FLANKED);
				pread1OvlLeft = pread1Contig.getReversedSeq(overlap - OVERLAPPED, overlap);
				pread1OvlRight = pread1Contig.getReversedSeq(1, OVERLAPPED + 1);
				fm.writeLine(contig + "\t" + pread1 + ":B\t" + overlap + "\t" + pread1OvlLeftEnd + "|" + pread1OvlLeft + " - " + pread1OvlRight + "|" + blank.toString() + " 1");
			} else {
				pread1OvlLeftEnd = pread1Contig.getSeq(pread1Contig.getSeqLen() - overlap - FLANKED, pread1Contig.getSeqLen() - overlap);
				pread1OvlLeft = pread1Contig.getSeq(pread1Contig.getSeqLen() - overlap,  pread1Contig.getSeqLen() - overlap + OVERLAPPED);
				pread1OvlRight = pread1Contig.getSeq(pread1Contig.getSeqLen() - OVERLAPPED, pread1Contig.getSeqLen());
				fm.writeLine(contig + "\t" + pread1 + ":E\t" + (pread1Contig.getSeqLen() - overlap) + "\t" + pread1OvlLeftEnd + "|" + pread1OvlLeft + " - " + pread1OvlRight + "|" + blank.toString() + " " + pread1Contig.getSeqLen());
			}
			
			if (isPread2DirectionReversed) {
				left = Math.min(edgeEnd + overlap, pread2Contig.getSeqLen());
				pread2OvlLeft = pread2Contig.getReversedSeq(left - OVERLAPPED, left);
				pread2OvlRight = pread2Contig.getReversedSeq(edgeEnd + 1, edgeEnd + 1 + OVERLAPPED);
				pread2OvlRightEnd = pread2Contig.getReversedSeq(edgeEnd - FLANKED, edgeEnd + 1);
				fm.writeLine(contig + "\t" + pread2 + ":B\t" + (edgeEnd + overlap) + "\t" + blank.toString() + "|" + pread2OvlLeft + " - " + pread2OvlRight + "|" + pread2OvlRightEnd + " " + edgeEnd);
			} else {
				pread2OvlLeft = pread2Contig.getSeq(0, OVERLAPPED);
				pread2OvlRight = pread2Contig.getSeq(edgeStart - OVERLAPPED, edgeStart);
				pread2OvlRightEnd = pread2Contig.getSeq(edgeStart, edgeStart + FLANKED);
				fm.writeLine(contig + "\t" + pread2 + ":E\t" + 0 + "\t" + blank.toString() + "|" + pread2OvlLeft + " - " + pread2OvlRight + "|" + pread2OvlRightEnd + " " + edgeStart);
			}
			fm.writeLine();
		}
	}
	
	
	
	private Contig getContigFromFasta(String contigToGet, HashMap<String, String> contigFastaMap) {
		Contig contig = null;
		System.out.println(".." + contigToGet + " | Reading file: " + contigFastaMap.get(contigToGet));
		FileReader fr = new FileReader(contigFastaMap.get(contigToGet));
		String line;
		READ_FA_LOOP : while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith(">" + contigToGet)) {
				StringBuffer contigSeq = new StringBuffer();
				contig = new Contig(contigToGet, contigSeq);
				line = fr.readLine();
				contigSeq.append(line);
				while (fr.hasMoreLines()) {
					line = fr.readLine();
					if (line.startsWith(">")) {
						break READ_FA_LOOP;
					}
					contigSeq.append(line.trim());
				}
			}
		}
		fr.closeReader();
		return contig;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar falconExtractOverlapsFromPreads.jar <p_ctg_tiling_unused> <preads4falcon.fasta.map> <out> [OVERLAPPED]");
		System.out.println("\t<p_ctg_tiling_unused>: Generated with falconExtractFirstTwoPreads.jar");
		System.out.println("\t<preads4falcon.fasta.map>: file having <contig_name>\t<contig.fa>");
		System.out.println("\t<out>: Overlapping preads are described in 2 lines:");
		System.out.println("\t[OVERLAPPED=30] Number of bases to see for overlapping on both ends");
		System.out.println("\t\tpread1\tOverlap1PosWithinFasta\t20bp...20bp\tOverlap1PosWithinFasta-outedge");
		System.out.println("\t\tpread2\tOverlap1PosWithinFasta(0)\t20bp...20bp\tOverlap2PosWithinFasta");
	}

	public static void main(String[] args) {
		if (args.length == 4) {
			OVERLAPPED = Integer.parseInt(args[3]);
			new ExtractOverlapsFromPreads().go(args[0], args[1], args[2]);
		}
		if (args.length == 3) {
			new ExtractOverlapsFromPreads().go(args[0], args[1], args[2]);
		} else {
			new ExtractOverlapsFromPreads().printHelp();
		}
		
	}

}
