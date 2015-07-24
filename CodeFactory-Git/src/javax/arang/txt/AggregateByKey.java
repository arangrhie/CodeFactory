package javax.arang.txt;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.matrix.Matrix;

public class AggregateByKey extends INOwrapper {

	private static boolean hasHeader = false;
	

	@Override
	public void printHelp() {
		System.out.println("java -jar txtAggregateByKey.txt <keyIdx> <output> <hasHeader> <*.infiles>");
		System.out.println("\t<keyIdx>: 1-base, column index containing the key to join");
		System.out.println("\t<output>: keyIdx\t<1th_infile>\t<2th_infile>...<Nth_infile> <Num_O>");
		System.out.println("\t<hasHeader>: TRUE if header exists in the first line, FALSE otherwise.");
		System.out.println("\t\tkeyIdx will be a unique one, and the data will be presented as \"O\" or \"X\" for existance.");
		System.out.println("Arang Rhie, 2015-04-09. arrhie@gmail.com");
	}

	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		HashMap<String, String> keyDataMap = new HashMap<String, String>();	// key, O/X data
		HashMap<String, Integer> keyLastIdx = new HashMap<String, Integer>();	// key, last number of file that had "O"
		ArrayList<String>	keyList = new ArrayList<String>();
		ArrayList<String>	sampleList = new ArrayList<String>();
		ArrayList<String>	keyListPerFile = new ArrayList<String>();
		
		String line;
		String[] tokens;
		String key;
		int nthFile = 0;
		
		for (FileReader fr : frs) {
			sampleList.add(fr.getFileName());
			if (hasHeader)	fr.readLine();	// skip header line
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				tokens = line.split(RegExp.TAB);
				key = tokens[keyIdx];
				if (!keyDataMap.containsKey(key)) {
					keyDataMap.put(key, Matrix.paddX(nthFile) + "\tO");
					keyList.add(key);
					keyListPerFile.add(key);
					keyLastIdx.put(key, nthFile + 1);
				} else if (!keyListPerFile.contains(key)){
					keyListPerFile.add(key);
					if (nthFile == keyLastIdx.get(key)) {
						keyDataMap.put(key, keyDataMap.get(key) + "\tO");
					} else {
						keyDataMap.put(key, keyDataMap.get(key) + Matrix.paddX(nthFile - keyLastIdx.get(key)) + "\tO");
					}
					keyLastIdx.put(key, nthFile + 1);
				}
			}
			nthFile++;
			keyListPerFile.clear();
		}
		for (int i = 0; i < keyList.size(); i++) {
			key = keyList.get(i);
			if (nthFile > keyLastIdx.get(key)) {
				keyDataMap.put(key, keyDataMap.get(key) + Matrix.paddX(nthFile - keyLastIdx.get(key)));
			}
		}
		
		fm.write("Key");
		for (int i = 0; i < sampleList.size(); i++) {
			fm.write("\t" + sampleList.get(i));
		}
		fm.writeLine("\tNum_O");
		
		for (int i = 0; i < keyList.size(); i++) {
			key = keyList.get(i);
			fm.writeLine(key + keyDataMap.get(key) + "\t" + Matrix.getNumOs(keyDataMap.get(key)));
		}
	
	}
	
	private static int keyIdx = 0;

	public static void main(String[] args) {
		if (args.length < 4) {
			new AggregateByKey().printHelp();
		} else {
			keyIdx = Integer.parseInt(args[0]) - 1;
			hasHeader = Boolean.parseBoolean(args[2]);
			String[] files = new String[args.length - 3];
			for (int i = 0; i < files.length; i++) {
				files[i] = args[i + 3];
			}
			new AggregateByKey().go(files, args[1]);
		}
	}

}
