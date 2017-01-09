package javax.arang.txt;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ReplaceToMultipleValues extends Rwrapper {

	private static final int KEY = 0;
	private static final int VAL = 1;
	
	@Override
	public void hooker(FileReader frInMap) {
		FileReader frVal1map = new FileReader(val1mapFileName);
		FileReader frVal2map = new FileReader(val2mapFileName);
		
		HashMap<String, ArrayList<String>> val1map = new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<String>> val2map = new HashMap<String, ArrayList<String>>();
		
		String line;
		String[] tokens;
		
		
		// read val1map
		System.err.println("Reading " + val1mapFileName);
		while (frVal1map.hasMoreLines()) {
			line = frVal1map.readLine();
			tokens = line.split(RegExp.TAB);
			if (!val1map.containsKey(tokens[KEY])) {
				val1map.put(tokens[KEY], new ArrayList<String>());
			}
			val1map.get(tokens[KEY]).add(tokens[VAL]);
		}
		
		// read val2map
		System.err.println("Reading " + val2mapFileName);
		while (frVal2map.hasMoreLines()) {
			line = frVal2map.readLine();
			tokens = line.split(RegExp.TAB);
			if (!val2map.containsKey(tokens[KEY])) {
				val2map.put(tokens[KEY], new ArrayList<String>());
			}
			val2map.get(tokens[KEY]).add(tokens[VAL]);
		}
		
		ArrayList<String> val1List;
		ArrayList<String> val2List;
		
		// read in.map
		System.err.println("Reading " + frInMap.getFileName());
		while (frInMap.hasMoreLines()) {
			line = frInMap.readLine();
			tokens = line.split(RegExp.TAB);
			val1List = val1map.get(tokens[KEY]);
			val2List = val2map.get(tokens[VAL]);
			if (val1List == null) {
				System.err.println("No matching key available for " + tokens[KEY] + "\t" + val1mapFileName);
				continue;
			} else if (val2List == null) {
				System.err.println("No matching key available for " + tokens[VAL] + "\t" + val2mapFileName);
				continue;
			}
			for (int i = 0; i < val1List.size(); i++) {
				for (int j = 0; j < val2List.size(); j++) {
					System.out.println(val1List.get(i) + "\t" + val2List.get(j));
				}
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtReplaceToMultipleValues.jar <in.map> <val1.map> <val2.map>");
		System.out.println("\t<in.map>: key1\tkey2");
		System.out.println("\t<val1.map>:");
		System.out.println("\t\tkey1\tval1-1");
		System.out.println("\t\tkey1\tval1-2");
		System.out.println("\t<val2.map>:");
		System.out.println("\t\tkey2\tval2-1");
		System.out.println("\t\tkey2\tval2-2");
		System.out.println("\tOutput will be written in standard output.");
	}

	private static String val1mapFileName;
	private static String val2mapFileName;
	
	public static void main(String[] args) {
		if (args.length == 3) {
			val1mapFileName = args[1];
			val2mapFileName = args[2];
			new ReplaceToMultipleValues().go(args[0]);
		} else {
			new ReplaceToMultipleValues().printHelp();
		}
		
	}

}
