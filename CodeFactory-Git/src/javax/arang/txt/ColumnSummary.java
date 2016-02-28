package javax.arang.txt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.arang.IO.INwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ColumnSummary extends INwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtColumnSummary.jar <col_idx> <*.tab-delimited.txt>");
		System.out.println("\t<col_idx>: 1-based, column index to look up");
		System.out.println("\tstdout the result as: category:\tnumCounts");
		System.out.println("\tcategory will be any String, including empty string");
	}

	@Override
	public void hooker(ArrayList<FileReader> frs) {
		
		HashMap<String, Integer> categoryMap = new HashMap<String, Integer>();
		String line;
		String[] tokens;
		String category;
		int numEmptyCells = 0;
		
		for (FileReader fr : frs) {
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				tokens = line.split(RegExp.TAB);
				if (tokens.length - 1 < colIdx) {
					//System.out.println("[DEBUG] :: " + (colIdx + 1) + "th column does not exist. Will be counted as blank in the following line.");
					//System.out.println("[DEBUG] :: " + line);
					numEmptyCells++;
					category = "";
				} else {
					category = tokens[colIdx];
				}
				if (!categoryMap.containsKey(category)) {
					categoryMap.put(category, 1);
				} else {
					categoryMap.put(category, categoryMap.get(category) + 1);
				}
			}
		}
		
		System.err.println();
		System.err.println("[DEBUG] :: Number of empty strings\t" + numEmptyCells);
		String[] keyArray = new String[categoryMap.size()];
		keyArray = categoryMap.keySet().toArray(keyArray);
		Arrays.sort(keyArray);
		for (int i = 0; i < keyArray.length; i++) {
			String key = keyArray[i];
			if (key.equals(""))	System.out.println("(blank)" + "\t" + categoryMap.get(key));
			else System.out.println(key + "\t" + categoryMap.get(key));
		}
	}

	static int colIdx = 0;
	public static void main(String[] args) {
		if (args.length < 2) {
			new ColumnSummary().printHelp();
		} else {
			colIdx = Integer.parseInt(args[0]) - 1;
			String[] files = new String[args.length - 1];
			for (int i = 0; i < files.length; i++) {
				files[i] = args[i + 1];
			}
			new ColumnSummary().go(files);
		}
	}

}
