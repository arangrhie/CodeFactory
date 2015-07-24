/**
 * 
 */
package javax.arang.bed;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.bed.util.Bed;
import javax.arang.genome.Chromosome;


/**
 * @author Arang Rhie
 *
 */
public class FindIntersect extends R2wrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.INwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedFindIntersect.jar <in1.bed> <in2.bed>");
		System.out.println("\tFind intersected region in <in1.bed> and <in2.bed>");
		System.out.println("\t3 <output> files will be generated: in1_in2_intersect.bed, in1_only.bed, in2_only.bed");
		System.out.println("Arang Rhie, 2014-03-11. arrhie@gmail.com");
	}
	
	Long start1 = 0l;
	Long start2 = 0l;
	Long end1 = 0l;
	Long end2 = 0l;
	int chr1 = 0;
	int chr2 = 0;
	String note1 = "";
	String note2 = "";

	String[] tokens1 = null;
	String[] tokens2 = null;
	
	
	/* (non-Javadoc)
	 * @see javax.arang.IO.INwrapper#hooker(java.util.ArrayList)
	 */
	@Override
	public void hooker(FileReader fr1, FileReader fr2) {
		
		FileMaker fmIntersect = new FileMaker(fr1.getDirectory(), fr1.getFileName().replace(".bed", "_") + fr2.getFileName().replace(".bed", "_intersect.bed"));
		FileMaker fmF1only = new FileMaker(fr1.getDirectory(), fr1.getFileName().replace(".bed", "_only.bed"));
		FileMaker fmF2only = new FileMaker(fr1.getDirectory(), fr2.getFileName().replace(".bed", "_only.bed"));
		
		readLine(fr1, tokens1, true);
		readLine(fr2, tokens2, false);
		while (fr1.hasMoreLines() && fr2.hasMoreLines()) {
			
			// read fr1
			if (chr1 < chr2) {
				// line1 is not in fr2
				writeBed(fmF1only, chr1, start1, end1, note1);
				if (!readLine(fr1, tokens1, true)) {
					break;
				}
			}
			
			// read fr2
			else if (chr1 > chr2) {
				// line2 is not in fr1
				writeBed(fmF2only, chr2, start2, end2, note2);
				if (!readLine(fr2, tokens2, false)) {
					break;
				}
			}
			
			else if (chr1 == chr2) {
				if (findIntersect(fmF1only, fmF2only, fmIntersect, fr1, fr2)) {
					if (start1 == end1 && !readLine(fr1, tokens1, true)) {
						break;
					}
					if (start2 == end2 && !readLine(fr2, tokens2, false)) {
						break;
					}
				} else {
					break;
				}
				
			}
		}
		
		// read fr1
		while (fr1.hasMoreLines()) {
			fmF1only.writeLine(fr1.readLine());
		}
		
		// read fr2
		while (fr2.hasMoreLines()) {
			fmF2only.writeLine(fr2.readLine());
		}
	}
	
	/***
	 * Finds intersecting region for the following cases:
	 * 1) #1 is not overlapping with #2
	 *  #1 |----|
	 *  #2        |----|
	 *  -> write non-overlapping region of #1
	 *  -> #1: read new line
	 *  -> #2: leave as it is
	 * 
	 * 2) #1 is partially overlapping with #2 
	 *  #1 |----|
	 *  #2   |----|
	 *  -> write non-overlapping region of #1
	 *  -> write overlapping region between #1 and #2
	 *  -> #1: read new line
	 *  -> #2: mark the next #2 starting position as the end of #1
	 * 
	 * 3) #2 is inclusive to #1
	 *  #1 |-----|
	 *  #2  |--|
	 *  -> write non-overlapping region of #1
	 *  -> write overlapping region #2
	 *  -> #1: mark the next #1 starting position at the end of #2
	 *  -> #2: read new line
	 * 
	 * and reverse case of #1 and #2 of 1-3).
	 * 
	 * After findIntersect(), check
	 * if (start == end) readLine(fr, tokens)
	 * 
	 * @param fmF1only
	 * @param fmF2only
	 * @param fmIntersect
	 * @param fr1
	 * @param fr2
	 * @return	false if no more lines are available to read in either fr1 or fr2
	 */
	private boolean findIntersect(FileMaker fm1only, FileMaker fm2only, FileMaker fmIntersect,
			FileReader fr1, FileReader fr2) {
		
		String note = (!note1.equals(""))?
				(note1 + ((!note2.equals(""))? "\t" + note2 : ""))
				: (!note2.equals(""))? note2 : "";
				
		/**
		 *1) #1 is not overlapping with #2
		 *  #1 |----|
		 *  #2        |----|
		 *  -> write non-overlapping region of #1
		 *  -> #1: read new line
		 *  -> #2: leave as it is
		 */
		if (end1 <= start2) {
			writeBed(fm1only, chr1, start1, end1, note1);
			return readLine(fr1, tokens1, true);
		} else if (end2 <= start1) {
			writeBed(fm2only, chr2, start2, end2, note2);
			return readLine(fr2, tokens2, false);
		}
		/***
		 * 2) #1 is partially overlapping with #2 
		 *  #1 |----|
		 *  #2   |----|
		 *  -> write non-overlapping region of #1
		 *  -> write overlapping region between #1 and #2
		 *  -> #2: mark the next #2 starting position as the end of #1
		 *  -> #1: read new line
		 */
		else if (start1 <= start2 && end1 <= end2){
			if(start1 < start2) {
				writeBed(fm1only, chr1, start1, start2, note1);
			}
			writeBed(fmIntersect, chr1, start2, end1, note);
			start2 = end1;
			if (readLine(fr1, tokens1, true)) {
				return true;
			} else {
				writeBed(fm2only, chr2, start2, end2, note2);
				return false;
			}
		} else if (start2 <= start1 && end2 <= end1) {
			if(start2 < start1) {
				writeBed(fm2only, chr2, start2, start1, note2);
			}
			writeBed(fmIntersect, chr2, start1, end2, note);
			start1 = end2;
			if (readLine(fr2, tokens2, false)) {
				return true;
			} else {
				writeBed(fm1only, chr1, start1, end1, note1);
				return false;
			}
		}
		/***
		 * 3) #2 is inclusive to #1
		 *  #1 |-----|
		 *  #2  |--|
		 *  -> write non-overlapping region of #1
		 *  -> write overlapping region #2
		 *  -> #1: mark the next #1 starting position at the end of #2
		 *  -> #2: read new line
		 */
		else if (start1 <= start2 && end2 <= end1) {
			if (start1 < start2) {
				writeBed(fm1only, chr1, start1, start2, note1);
			}
			writeBed(fmIntersect, chr2, start2, end2, note);
			start1 = end2;
			if (readLine(fr2, tokens2, false)) {
				return true;
			} else {
				writeBed(fm1only, chr1, start1, end1, note1);
				return false;
			}
		} else if (start2 <= start1 && end1 <= end2) {
			if (start2 < start1) {
				writeBed(fm2only, chr2, start2, start1, note2);
			}
			writeBed(fmIntersect, chr1, start1, end1, note);
			start2 = end1;
			if (readLine(fr1, tokens1, true)) {
				return true;
			} else {
				writeBed(fm2only, chr2, start2, end2, note2);
				return false;
			}
		} else {
			System.out.println("DEBUG :: bed1 : " + chr1 + " " + start1 + " " + end1);
			System.out.println("DEBUG :: bed2 : " + chr2 + " " + start2 + " " + end2);
		}
		return true;
	}
	
	private boolean readLine(FileReader fr, String[] tokens, boolean isBed1) {
		if (fr.hasMoreLines()) {
			tokens = fr.readLine().split("\t");
			if (isBed1) {
				chr1 = Bed.getChromIntVal(tokens);
				start1 = Bed.getStart(tokens);
				end1 = Bed.getEnd(tokens);
				note1 = Bed.getNotes(tokens);
			} else {
				chr2 = Bed.getChromIntVal(tokens);
				start2 = Bed.getStart(tokens);
				end2 = Bed.getEnd(tokens);
				note2 = Bed.getNotes(tokens);
			}
			return true;
		} else {
			return false;
		}
	}
	
	private void writeBed(FileMaker fm, int chrom, long start, long end, String note) {
		if (note.equals("")) {
			fm.writeLine(Chromosome.getChromStringVal(chrom) + "\t" + start + "\t" + end);
		} else {
			fm.writeLine(Chromosome.getChromStringVal(chrom) + "\t" + start + "\t" + end + "\t" + note);
		}
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new FindIntersect().go(args[0], args[1]);
		} else {
			new FindIntersect().printHelp();
		}
	}

}
