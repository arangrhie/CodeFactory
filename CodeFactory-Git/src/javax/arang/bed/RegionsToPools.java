package javax.arang.bed;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class RegionsToPools extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		String line;
		String[] tokens;
		String chr_pos;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			chr_pos = tokens[0] + "\t" + tokens[1] + "\t" + tokens[2];
			
			tokens = tokens[Bed.NOTE].split("[(_,)]");
			if (tokens.length > 5) {
				fm.writeLine(chr_pos + "\t" + tokens[0] + "\t" + tokens[tokens.length - 2].substring(tokens[tokens.length - 2].indexOf("l") + 1)
				+ "\t" + tokens[tokens.length - 1].substring(tokens[tokens.length - 1].indexOf("l") + 1));
			} else if (tokens.length > 2) {
				fm.writeLine(chr_pos + "\t" + tokens[0] + "\t" + tokens[tokens.length - 2].substring(tokens[tokens.length - 2].indexOf("l") + 1)
				+ "\t" + tokens[tokens.length - 1].substring(tokens[tokens.length - 1].indexOf("l") + 1));
//			} else {
//				fm.writeLine("\tUnknown\t" + tokens[0].substring(tokens[0].indexOf("l") + 1));
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar <poolX_region_sort.bed> <out.BestDQ.bed>");
		System.out.println("\tConverts CHR\tSTART\tEND\tBAC_Annotation");
		System.out.println("\t\tto CHR\tSTART\tEND\tBAC_ID\tpool1\tpool2");
		System.out.println("Arang Rhie, 2015-06-08. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new RegionsToPools().go(args[0], args[1]);
		} else {
			new RegionsToPools().printHelp();
		}
	}

}
