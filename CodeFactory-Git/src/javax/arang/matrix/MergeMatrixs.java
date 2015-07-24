package javax.arang.matrix;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class MergeMatrixs extends INOwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar matrixMergeMatrixs.jar <out.tab> <*.tab>");
		System.out.println("\tMerge tab-delimited files, by joining the <keyIdx>.");
		System.out.println("\tIf the key in the n'th file does not exist in the previous(n-1) file,");
		System.out.println("\tthe data will be filled with \"X\"");
		System.out.println("\tThe 1st column in the input file is the key to join");
		System.out.println("\t<*.tab>: data filled with \"O\" or \"X\", with the last column containing the number of Os.");
	}

	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		String line;
		String[] tokens = null;
		
		HashMap<String, String>	keyDataMap = new HashMap<String, String>();		// key \t data columns with O/X
		HashMap<String, Integer> keyColIdx = new HashMap<String, Integer>();	// key \t last column index that has been written in keyDataMap
		ArrayList<String> keyList = new ArrayList<String>();
		
		int maxColumnIdx = 0;
		int prevNumColumns = 0;
		String key;
		for (FileReader fr : frs) {
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				tokens = line.split(RegExp.TAB);
				key = tokens[KEY];
				
				if (!keyDataMap.containsKey(key)) {
					keyList.add(key);
					keyDataMap.put(key, Matrix.paddX(maxColumnIdx) + getData(tokens));
				} else {
					if (maxColumnIdx == keyColIdx.get(key)) {
						keyDataMap.put(key, keyDataMap.get(key) + getData(tokens));
					} else {
						keyDataMap.put(key, keyDataMap.get(key) + Matrix.paddX(maxColumnIdx - keyColIdx.get(key)) + getData(tokens));
					}
				}
				keyColIdx.put(key, maxColumnIdx + tokens.length - 1);
				
				if (key.equals("000021F")) {
					System.out.println("[DEBUG] :: " + keyDataMap.get(key));
				}
			}
			prevNumColumns = tokens.length - 1;
			maxColumnIdx += prevNumColumns;
		}
		
		for (int i = 0; i < keyList.size(); i++) {
			key = keyList.get(i);
			if (maxColumnIdx > keyColIdx.get(key)) {
				keyDataMap.put(key, keyDataMap.get(key) + Matrix.paddX(maxColumnIdx - keyColIdx.get(key)));
			}
		}
		
		key = keyList.get(0);
		fm.writeLine(key + keyDataMap.get(key) + "\tNum_O");
		
		for (int i = 1; i < keyList.size(); i++) {
			key = keyList.get(i);
			fm.writeLine(key + keyDataMap.get(key) + "\t" + Matrix.getNumOs(keyDataMap.get(key)));
		}
		
	}
	
	private String getData(String[] tokens) {
		StringBuffer data = new StringBuffer();
		for (int i = 1; i < tokens.length; i++) {
			data.append("\t" + tokens[i]);
		}
		return data.toString();
	}

	private static final int KEY = 0;
	public static void main(String[] args) {
		if (args.length < 2) {
			new MergeMatrixs().printHelp();
		} else {
			String[] inFiles = new String[args.length - 1];
			for (int i = 0; i < inFiles.length; i++) {
				inFiles[i] = args[i + 1];
			}
			new MergeMatrixs().go(inFiles, args[0]);
		}
		
	}

}
