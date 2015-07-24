package javax.arang.bed;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class MergeWhenBoundaryOverlaps extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		line = fr.readLine();
		tokens = line.split(RegExp.TAB);
		String prevChr = tokens[Bed.CHROM];
		int start1 = Integer.parseInt(tokens[Bed.START]);
		int end1 = Integer.parseInt(tokens[Bed.END]);
		String note1 = tokens[noteIdx];
		if (tokens.length > noteIdx + 1) {
			for (int i = noteIdx + 1; i < tokens.length; i++) {
				note1 = note1 + "\t" + tokens[i];
			}
		}
		int start2;
		int end2;
		int count = 1;
		String note2 = "";
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			start2 = Integer.parseInt(tokens[Bed.START]);
			end2 = Integer.parseInt(tokens[Bed.END]);
			note2 = tokens[noteIdx];
			if (tokens.length > noteIdx + 1) {
				for (int i = noteIdx + 1; i < tokens.length; i++) {
					note2 = note2 + "\t" + tokens[i];
				}
			}
			// New chromosome
			if (!prevChr.equals(tokens[Bed.CHROM])) {
				writeRegion(fm, prevChr, start1, end1, count, note1);
				prevChr = tokens[Bed.CHROM];
				start1 = start2;
				end1 = end2;
				count = 1;
				note1 = note2;
			} else {
				
				// Compare START and END
				if (Math.abs(start1 - start2) < boundLim || Math.abs(end1 - end2) < boundLim) {
					// Assume to be same region
					start1 = Math.min(start1, start2);
					end1 = Math.max(end1, end2);
					note1 = note1 + sep + note2;
					count++;
				} else {
					// Not same region
					writeRegion(fm, prevChr, start1, end1, count, note1);
					start1 = start2;
					end1 = end2;
					count = 1;
					note1 = note2;
				}
			}
		}
		writeRegion(fm, prevChr, start1, end1, count, note1);
	}
	
	private void writeRegion(FileMaker fm, String chr, int start, int end, int count, String note) {
		fm.writeLine(chr + "\t" + start + "\t" + end + "\t" + (end - start) + "\t" + count + "\t" + note);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar -Xmx512m bedMergeWhenBoundaryOverlaps.jar <in.bed> <boundLim> [noteIdx] [sep]");
		System.out.println("\t<in.bed>: Sorted bed file, formatted as: CHR\tSTART\tEND/tNote");
		System.out.println("\t<boundLim>: Regions with abs(START1-START2) < boundLim || abs(END1-END2) < boundLim will be merged.");
		System.out.println("\t<noteIdx>: Optional. DEFAULT=4. 1-based. Column index for note to annotate.");
		System.out.println("\t<sep>: Seperator for multiple notes. Set to \"tab\" for tabs. DEFAULT=;");
		System.out.println("\t<out.bed>: <in_bounaray_bps.bed>. Merge regions when START or END boundaries overlaps.");
		System.out.println("\t\tCHR\tSTART\tEND\tNUM_MERGED_REGIONS\tNOTEs seperated by <sep>");
		System.out.println("Arang Rhie, 2015-05-21. arrhie@gmail.com");
	}

	private static int boundLim = 0;
	private static int noteIdx = Bed.NOTE;
	private static String sep = ";";
	
	public static void main(String[] args) {
		if (args.length == 2) {
			boundLim = Integer.parseInt(args[1]);
			new MergeWhenBoundaryOverlaps().go(args[0], args[0].replace(".bed", "_" + args[1] + ".bed"));
		} else if (args.length == 3) {
			noteIdx = Integer.parseInt(args[2]) -1;
			boundLim = Integer.parseInt(args[1]);
			new MergeWhenBoundaryOverlaps().go(args[0], args[0].replace(".bed", "_" + args[1] + ".bed"));
		} else if (args.length == 4) {
			sep = args[3];
			if (sep.toLowerCase().equals("tab")) {
				sep = "\t";
			}
			noteIdx = Integer.parseInt(args[2]) -1;
			boundLim = Integer.parseInt(args[1]);
			new MergeWhenBoundaryOverlaps().go(args[0], args[0].replace(".bed", "_" + args[1] + ".bed"));
		} else {
			new MergeWhenBoundaryOverlaps().printHelp();
		}
	}

}
