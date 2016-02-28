package javax.arang.txt;

import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ReplaceWords extends I2Owrapper {

	@Override
	public void hooker(FileReader frMap, FileReader frFile, FileMaker fm) {
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
				fm.writeLine(line);
				continue;
			}
			tokens = line.split(RegExp.TAB);
			if (map.containsKey(tokens[colIdx])) {
				writeContents(fm, tokens, map.get(tokens[colIdx]));
				countReplaced++;
			} else {
				fm.writeLine(line);
			}
		}
		
		System.out.println("Replaced lines / words: " + countReplaced + " / " + map.size());
		
	}

	private void writeContents(FileMaker fm, String[] tokens, String newWord) {
		for (int i = 0; i < colIdx; i++) {
			fm.write(tokens[i] + "\t");
		}
		fm.write(newWord);
		for (int i = colIdx + 1; i < tokens.length; i++) {
			fm.write("\t" + tokens[i]);
		}
		fm.writeLine();
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtReplaceWords.jar <in.map> <in.file> <col_idx> [out.file]");
		System.out.println("\t<in.map>: 1st column = word to be replaced (key), 2nd column = word to replace (value)");
		System.out.println("\t<in.file>: tab-delimited txt file");
		System.out.println("\t<col_idx>: column index to be looked up. 1-based.");
		System.out.println("\t[out.file]: DEFAULT = <in.file.replaced>");
		System.out.println("Arang Rhie, 2015-12-02. arrhie@gmail.com");
	}
	
	private static int colIdx = 0;

	public static void main(String[] args) {
		if (args.length == 3) {
			colIdx = Integer.parseInt(args[2]) - 1;
			new ReplaceWords().go(args[0], args[1], args[1] + ".replaced");
		} else if (args.length == 4) {
			colIdx = Integer.parseInt(args[2]) - 1;
			new ReplaceWords().go(args[0], args[1], args[3]);
		} else {
			new ReplaceWords().printHelp();
		}
	}

}
