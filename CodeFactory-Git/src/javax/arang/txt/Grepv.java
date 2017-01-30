package javax.arang.txt;

import java.util.ArrayList;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class Grepv extends R2wrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2) {
		String line;
		String[] tokens;
		
		ArrayList<String> stringsToRemove = new ArrayList<String>();
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			stringsToRemove.add(line.trim());
		}
		
		System.err.println("[DEBUG] :: strings to remove: " + stringsToRemove.size());
		int countRemoved = 0;
		while (fr2.hasMoreLines()) {
			line = fr2.readLine();
			tokens = line.split(RegExp.TAB);
			if (!stringsToRemove.contains(tokens[colIdx])) {
				System.out.println(line);
			} else {
				countRemoved++;
			}
		}
		System.err.println("[DEBUG] :: removed num. lines: " + countRemoved);
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtGrepv.jar <words_to_remove.txt> <in.txt> <col_idx>");
		System.out.println("Similar to grep -v option. Outputs are written in standard output.");
		System.out.println("Lines containing words from <words_to_remove.txt> in <col_idx>'th column of <in.txt>  will be removed");
		System.out.println("\t<words_to_remove.txt>: a file containing 1 word or strings in 1 line to be removed from <in.txt>");
		System.out.println("\t<in.txt>: tab delimited txt file.");
		System.out.println("\t<col_idx>: 1-based column index.");
		System.out.println("Arang Rhie, 2017-01-24. arrhie@gmail.com");
		
	}

	private static int colIdx = 0; 
	public static void main(String[] args) {
		if (args.length == 3) {
			colIdx = Integer.parseInt(args[2]) - 1;
			new Grepv().go(args[0], args[1]);
		} else {
			new Grepv().printHelp();
		}
	}

}
