package javax.arang.txt;

import java.util.ArrayList;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class Grepv extends I2Owrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		String line;
		String[] tokens;
		
		ArrayList<String> stringsToRemove = new ArrayList<String>();
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			stringsToRemove.add(line.trim());
		}
		
		System.out.println("[DEBIG] :: strings to remove: " + stringsToRemove.size());
		int countRemoved = 0;
		while (fr2.hasMoreLines()) {
			line = fr2.readLine();
			tokens = line.split(RegExp.TAB);
			if (!stringsToRemove.contains(tokens[colIdx])) {
				fm.writeLine(line);
			} else {
				countRemoved++;
			}
		}
		System.out.println("[DEBUG] :: removed num. lines: " + countRemoved);
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtGrepv.jar <words_to_remove.txt> <in.txt> <col_idx> <out.txt>");
		System.out.println("Similar to grep -v option.");
		System.out.println("Lines containing words from <words_to_remove.txt> in <col_idx>'th column of <in.txt>  will be removed");
		System.out.println("\t<in.txt>: tab delimited txt file");
		System.out.println("\t<col_idx>: 1-based column index.");
		System.out.println("\t<words_to_remove.txt>: a file containing 1 word or strings in 1 line to be removed from <in.txt>");
		System.out.println("Arang Rhie, 2015-05-21. arrhie@gmail.com");
		
	}

	private static int colIdx = 0; 
	public static void main(String[] args) {
		if (args.length == 4) {
			colIdx = Integer.parseInt(args[2]) - 1;
			new Grepv().go(args[0], args[1], args[3]);
		} else {
			new Grepv().printHelp();
		}
	}

}
