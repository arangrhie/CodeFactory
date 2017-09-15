package javax.arang.txt;

import java.util.HashMap;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ReplaceWords extends R2wrapper {

	@Override
	public void hooker(FileReader frMap, FileReader frFile) {
		String line;
		String[] tokens;
		HashMap<String, String> map = new HashMap<String, String>();
		
		while (frMap.hasMoreLines()) {
			line = frMap.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			map.put(tokens[0], tokens[1]);
		}
		
		int countReplaced = 0;
		while (frFile.hasMoreLines()) {
			line = frFile.readLine();
			if (line.startsWith("#")) {
				System.out.println(line);
				continue;
			}
			tokens = line.split(RegExp.TAB);
			if (map.containsKey(tokens[colIdx])) {
				writeContents(tokens, map.get(tokens[colIdx]));
				countReplaced++;
			} else {
				System.out.println(line);
			}
		}
		
		System.err.println("Replaced lines / words: " + countReplaced + " / " + map.size());
		
	}

	private void writeContents(String[] tokens, String newWord) {
		for (int i = 0; i < colIdx; i++) {
			System.out.print(tokens[i] + "\t");
		}
		System.out.print(newWord);
		for (int i = colIdx + 1; i < tokens.length; i++) {
			System.out.print("\t" + tokens[i]);
		}
		System.out.println();
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtReplaceWords.jar <in.map> <in.file> <col_idx>");
		System.out.println("\t<in.map>: 1st column = word to be replaced (key), 2nd column = word to replace (value)");
		System.out.println("\t<in.file>: tab-delimited txt file");
		System.out.println("\t<col_idx>: column index to be looked up. 1-based.");
		System.out.println("\t<sysout>: <in.file> <col_idx> being replaced according to <in.map>");
		System.out.println("Arang Rhie, 2017-08-24. arrhie@gmail.com");
	}
	
	private static int colIdx = 0;

	public static void main(String[] args) {
		if (args.length == 3) {
			colIdx = Integer.parseInt(args[2]) - 1;
			new ReplaceWords().go(args[0], args[1]);
		} else {
			new ReplaceWords().printHelp();
		}
	}

}
