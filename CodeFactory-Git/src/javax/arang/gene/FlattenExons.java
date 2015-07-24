package javax.arang.gene;

import java.util.Arrays;
import java.util.PriorityQueue;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class FlattenExons extends IOwrapper {

	private static final int GENE_ID = 0;
//	private static final int EXON_ID = 1;
	private static final int CHR = 2;
	private static final int START = 3;
	private static final int END = 4;
	private static final int STRAND = 5;
//	private static final int TRANSCRIPT_ID = 6;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		String line;
		String[] tokens;
		
		String prevGeneID = "";
		PriorityQueue<ExonUnion> exonUnions = null;
		ExonUnion exonUnion = null;
		String chr;
		int start;
		int end;
		String strand;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			chr = tokens[CHR];
			start = Integer.parseInt(tokens[START]);
			end = Integer.parseInt(tokens[END]);
			strand = tokens[STRAND];
			
			// For each gene_id
			if (!prevGeneID.equals(tokens[GENE_ID])) {
				// TODO write flattened genes
				if (!prevGeneID.equals(""))	writeExonUnions(fm, exonUnions);
				
				prevGeneID = tokens[GENE_ID];
				exonUnions = new PriorityQueue<ExonUnion>(1, new ExonUnionComparator());
				exonUnion = new ExonUnion(chr, start, end, prevGeneID, strand);
				exonUnions.add(exonUnion);
				continue;
			}

			boolean isOverlapped = false;
			for (ExonUnion exon : exonUnions) {
				if (exon.isOverlapping(start, end)) {
					// Add exon info to exonset
					exon.addExon(start, end);
					isOverlapped = true;
				}
			}
			if (!isOverlapped) {
				exonUnion = new ExonUnion(chr, start, end, prevGeneID, strand);
				exonUnions.add(exonUnion);
			}
		}
		writeExonUnions(fm, exonUnions);
	}
	
	

	private void writeExonUnions(FileMaker fm, PriorityQueue<ExonUnion> exon) {
		if (exon==null)	return;
		ExonUnion[] exonUnions = exon.toArray(new ExonUnion[0]);
		Arrays.sort(exonUnions);
		for (int i = 0; i < exonUnions.length; i++) {
			ExonUnion exonUnion = exonUnions[i];
			if (mode.equals("GENE")) {
				if (i == 0)	fm.write(exonUnion.getChr() + "\t" + exonUnion.getMin());
				if (i == exonUnions.length - 1)	fm.writeLine("\t" + exonUnion.getMax() + exonUnion.getGeneID() + "\t" + exonUnion.getStrand());
			} else if (mode.equals("EXON")) {
				fm.writeLine(exonUnion.getChr() + "\t" + exonUnion.getMin() + "\t" + exonUnion.getMax() + "\t" + exonUnion.getGeneID() + "\t" + exonUnion.getStrand());
			} else if (mode.equalsIgnoreCase("SPLIT")) {
				Integer[] exonBorder = exonUnion.getBorders().toArray(new Integer[0]);
				Arrays.sort(exonBorder);
				fm.writeLine(exonUnion.getChr() + "\t" +  (exonBorder[0] - 1) + "\t" + exonBorder[1] + "\t" + exonUnion.getGeneID() + "\t" + exonUnion.getStrand());
				for (int j = 2; j < exonBorder.length - 1; j+=2) {
					fm.writeLine(exonUnion.getChr() + "\t" +  (exonBorder[j] - 1) + "\t" + exonBorder[j + 1] + "\t" + exonUnion.getGeneID() + "\t" + exonUnion.getStrand());
				}
			}
		}
	}



	@Override
	public void printHelp() {
		System.out.println("usage: java -jar txtFlattenExons.jar <in.txt> <out.bed> <mode>");
		System.out.println("\t<in.txt>: gene_id\texon_id\tchr\tstart\tend\tstrand\ttranscript_id");
		System.out.println("\t<out.bed>: chr\tstart\tend\tgene_id");
		System.out.println("\t<mode>: gene or exon or split. DEFAULT: EXON");
		System.out.println("\t\tgene: the flattened gene (start of first exon and end of last exon) is written line by line");
		System.out.println("\t\texon: the flattened exon is written line by line");
		System.out.println("\t\tsplit: each of the splitted, non-overlapping exon is written line by line");
		System.out.println("Arang Rhie, 2015-03-20. arrhie@gmail.com");
	}

	private static String mode = "EXON";
	public static void main(String[] args) {
		if (args.length == 2) {
			new FlattenExons().go(args[0], args[1]);
		} else if (args.length == 3) {
			mode = args[2].toUpperCase();
			new FlattenExons().go(args[0], args[1]);
		} else {
			new FlattenExons().printHelp();
		}
	}

}
