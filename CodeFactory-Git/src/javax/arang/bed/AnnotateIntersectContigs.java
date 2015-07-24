package javax.arang.bed;

import java.util.HashMap;
import java.util.Vector;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class AnnotateIntersectContigs extends IOwrapper {

	//private static final int CHR_1=0;
	private static final int START_1=1;
	private static final int END_1=2;
	private static final int FRAGMENT_1=3;
	private static final int LEN_1=4;
	private static final int CONTIG_ID_1=5;
	//private static final int CHR_2=6;
	private static final int START_2=7;
	private static final int END_2=8;
	private static final int FRAGMENT_2=9;
	private static final int LEN_2=10;
	private static final int CONTIG_ID_2=11;
	private static final int INTERSECT_LEN=12;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String tokens[];
		
		Vector<String[]> originBed = new Vector<String[]>();
		Vector<String[]> additionalColumns = new Vector<String[]>();
		
		int start1 = 0;
		int start2 = 0;
		int end1 = 0;
		int end2 = 0;
		int len1;
		int len2;
		int intersectLen;
		int fragments_1;
		int fragments_2;
		String overlap = "";
		int DQ;
		double overlapP;
		double d1;
		double d2;
		double d1xd2;
		String well;
		HashMap<String, Integer> wellOccurrenceMap = new HashMap<String, Integer>();
		HashMap<String, Integer> wellBestDQMap = new HashMap<String, Integer>();
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			String[] origin = tokens.clone();
			originBed.add(origin);
			// Calculate values for additionalColumns, except [Num. overlapping regions] and [Best DQ/Low DQ]
			start1 = Integer.parseInt(tokens[START_1]);
			end1 = Integer.parseInt(tokens[END_1]);
			start2 = Integer.parseInt(tokens[START_2]);
			end2 = Integer.parseInt(tokens[END_2]);
			len1 = Integer.parseInt(tokens[LEN_1]);
			len2 = Integer.parseInt(tokens[LEN_2]);
			intersectLen = Integer.parseInt(tokens[INTERSECT_LEN]);
			fragments_1 = Integer.parseInt(tokens[FRAGMENT_1]);
			fragments_2 = Integer.parseInt(tokens[FRAGMENT_2]);
			
			overlap = (Math.abs(start1-start2) < 1000 || Math.abs(end1-end2) < 1000) ? "Overlapping" : "NA";
			overlapP = ((double) intersectLen * 100) / Math.max(len1, len2);
			d1 = ((double) fragments_1 * 100) / len1;
			d2 = ((double) fragments_2 * 100) / len2;
			d1xd2 = d1*d2;
			DQ = (int) (d1xd2 * overlapP);
			well = tokens[CONTIG_ID_1].substring(0, tokens[CONTIG_ID_1].indexOf("_") + 1)
					+ tokens[CONTIG_ID_2].substring(0, tokens[CONTIG_ID_2].indexOf("_"));
			if (overlap.equals("Overlapping")) {
				if (wellOccurrenceMap.containsKey(well)) {
					wellOccurrenceMap.put(well, wellOccurrenceMap.get(well) + 1);
					if (wellBestDQMap.get(well) < DQ) {
						wellBestDQMap.put(well, DQ);
					}
				} else {
					wellOccurrenceMap.put(well, 1);
					wellBestDQMap.put(well, DQ);
				}
			}
			
			// Assign calculated additional column except the last 2 columns
			String[] additionalColumn = new String[7];
			additionalColumn[0] = overlap;
			additionalColumn[1] = DQ + "";
			additionalColumn[2] = String.format("%2.2f", overlapP);
			additionalColumn[3] = d1 + "";
			additionalColumn[4] = d2 + "";
			additionalColumn[5] = d1xd2 + "";
			additionalColumn[6] = well;
			
			// Add to additionalColumns
			additionalColumns.add(additionalColumn);
		}
		System.out.println("[DEBUG] :: Finishing reading... Start to writing");
		fm.writeLine("CHR\tSTART\tEND\tFRAGMENTS\tLEN\tCONTIG_ID\t"
				+ "CHR_2\tSTART_2\tEND_2\tFRAGMENTS_2\tLEN_2\tCONTIG_ID_2\t"
				+ "INTERSECT_LEN\tOVERLAPPING\tDQ\tOverlap%\td1\td2\td1xd2\tWELL\tWELL_OCCURRENCE\tBest_DQ");
		for (int i = 0; i < originBed.size(); i++) {
			String[] origin = originBed.get(i);
			String[] additional = additionalColumns.get(i);
			//System.out.println("[DEBUG] :: origin.length = " + origin.length);
			for (int j = 0; j < origin.length; j++) {
				fm.write(origin[j] + "\t");
			}
			for (int j = 0; j < additional.length; j++) {
				fm.write(additional[j] + "\t");
			}
			if(additional[0].equals("Overlapping")) {
				fm.write(wellOccurrenceMap.get(additional[6]) + "\t");
				if (additional[1].equals(wellBestDQMap.get(additional[6]) + "")) {
					fm.write("BestDQ");
				} else {
					fm.write("LowDQ");
				}
			} else {
				fm.write("\t\t");
			}
			fm.writeLine();
			
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedAnnotateIntersectContigs.jar <in.bed> <out.bed>");
		System.out.println("\t<in.bed>: output of bedtools intersect -a <a.bed> -b <b.bed> -wo");
		System.out.println("\t<out.bed>: <in.bed> with additional columns as following");
		System.out.println("\t\tOverlapping <1kb bounds\tDQ\tOverlap%\td1\td2\td1xd2\tWELL\tNum. overlapping regions (in Overlapping <1kb bounds=TRUE)\tBest DQ/Low DQ (if Num. overlapping regions > 1)");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new AnnotateIntersectContigs().go(args[0], args[1]);
		} else {
			new AnnotateIntersectContigs().printHelp();
		}

	}

}
