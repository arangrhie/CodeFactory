package javax.arang.txt;

import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class AddOccurrence extends IOwrapper {

	private static int colIdx = 0;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String tokens[];
		HashMap<String, Integer> occurrenceMap = new HashMap<String, Integer>();
		String value;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			value = tokens[colIdx];
			if (!occurrenceMap.containsKey(value)) {
				occurrenceMap.put(value, 0);
			}
			occurrenceMap.put(value, occurrenceMap.get(value) + 1);
		}
		
		fr.reset();
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#")) {
				fm.writeLine(line + "\tOCCURRENCE");
				continue;
			}
			tokens = line.split(RegExp.TAB);
			value = tokens[colIdx];
			fm.writeLine(line + "\t" + occurrenceMap.get(value));
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtAddOccurrence.jar <in.tab> <out.tab> <COL_IDX>");
		System.out.println("\t<in.tab>: input tab-delimited text file.");
		System.out.println("\t<out.tab>: last column containing occurrence in integer.");
		System.out.println("\t<COL_IDX>: 1-based, column index to look up for occurrence.");
		System.out.println("\t\tLine starting with # are automatically detected as header line, and OCCURRENCE will be written.");
		System.out.println("Arang Rhie, 2015-09-24. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			colIdx = Integer.parseInt(args[2]) - 1;
			new AddOccurrence().go(args[0], args[1]);
		} else {
			new AddOccurrence().printHelp();
		}
	}

}
