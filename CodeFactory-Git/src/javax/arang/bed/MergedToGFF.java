package javax.arang.bed;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class MergedToGFF extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String chr;
		String start;
		String end;
		
		String line;
		String[] tokens;
		String[] notes;
		String poolID;
		
		String id;
		String note;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			chr = tokens[Bed.CHROM];
			start = tokens[Bed.START];
			end = tokens[Bed.END];
			notes = tokens[5].split(sep);
			poolID = notes[0].split(":")[0];
			note = notes[0].split(":")[1];
			//System.out.println("[DEBUG] :: " + note);
			if (note.indexOf("(") > 0) {
				id = note.substring(0, note.indexOf("("));
				for (int i = 1; i < notes.length; i++) {
					note = note + ", " + notes[i];;
					id = id + "," +notes[i].substring(0, notes[i].indexOf("("));
				}
			} else {
				id = note;
			}
			fm.writeLine(chr + "\tGMI\t" + poolID + "\t" + start + "\t" + end + "\t.\t+\t.\tID=\"" + id + "\";Name=\"" + id + "\";Note=\"" + note + "\"");
			
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedMergeToGFF.jar <in.bed> <out.gff> [sep]");
		System.out.println("Convert <in.bed> file to <out.gff>");
		System.out.println("\t<in.bed>: Generated with bedMergeWhenBoundaryOverlaps.jar");
		System.out.println("\t<out.gff>: CHR\tGMI\tGroupID\tSTART\tEND\t.\t+\t.\tName=BAC_IDs with , sep.\tNote=BAC_IDs with , sep.\tNote=Detailed description");
	}
	
	private static String sep = ";"; 

	public static void main(String[] args) {
		if (args.length == 2) {
			new MergedToGFF().go(args[0], args[1]);
		} else if (args.length == 3) {
			if (args[2].equals("tab")) {
				sep = "\t";
			} else {
				sep = args[2];
			}
			new MergedToGFF().go(args[0], args[1]);
		} else {
			new MergedToGFF().printHelp();
		}
	}

}
