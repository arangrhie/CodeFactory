package javax.arang.txt;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class AddMatrix extends INOwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtAddMatrix.jar <in1.matrix> <in2.matrix> ... <inN.matrix> <num_headers>");
		System.out.println("\t<out>: matrixAdded.txt");
		System.out.println("\tSkips 2 lines");
	}

	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		String line;
		String[] tokens;
		
		HashMap<String, Integer[]> map = new HashMap<String, Integer[]>();
		ArrayList<String> keyList = new ArrayList<String>();
		boolean header = true;
		int lineCount = 0;
		for (FileReader fr : frs) {
			lineCount = 0;
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				if (header && lineCount == numHeaders) {
					header = false;
				}
				if (lineCount < numHeaders) {
					if (header) {
						fm.writeLine(line);
					}
					lineCount++;
					continue;
				}
				tokens = line.split(RegExp.TAB);
				addToMap(map, tokens[0], tokens);
				keyList.add(tokens[0]);
			}
		}
		
		Integer[] values;
		for (String key : keyList) {
			fm.write(key);
			values = map.get(key);
			for (int i = 0; i < values.length; i++) {
				fm.write("\t" + values[i]);
			}
			fm.writeLine();
		}
	}

	private void addToMap(HashMap<String, Integer[]> map, String key, String[] tokens) {
		Integer[] values;
		if (!map.containsKey(key)) {
			values = new Integer[tokens.length - 1];
			for (int i = 1; i < tokens.length; i++) {
				values[i-1] = Integer.parseInt(tokens[i]);
			}
		} else {
			values = map.get(key);
			for (int i = 1; i < tokens.length; i++) {
				values[i-1] += Integer.parseInt(tokens[i]);
			}
		}
		map.put(key, values);
	}
	
	public static int numHeaders = 2;

	public static void main(String[] args) {
		if (args.length > 1) {
			String[] files = new String[args.length - 1];
			for (int i = 0; i < files.length; i++) {
				files[i] = args[i];
			}
			numHeaders = Integer.parseInt(args[args.length - 1]);
			new AddMatrix().go(files, "matrixAdded.txt");
		} else {
			new AddMatrix().printHelp();
		}
	}

}
