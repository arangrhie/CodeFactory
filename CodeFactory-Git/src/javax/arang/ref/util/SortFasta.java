package javax.arang.ref.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class SortFasta extends IOwrapper{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String inFile = "C://Documents and Settings/아랑/바탕 화면/mito/ref/mitocon_hg19_bait_gsnap.fasta";
		String outFile = "mitocon_hg19_bait_gsnap_sort.fasta";
		if (args.length > 0) {
			inFile = args[0];
			outFile = args[1];
		}
		new SortFasta().go(inFile, outFile);
	}
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		HashMap<String, String> sequences = new HashMap<String, String>();
		ArrayList<String> keys = new ArrayList<String>();
		while (fr.hasMoreLines()) {
			String key = fr.readLine().toString();
			String line = fr.readLine().toString();
			keys.add(key);
			sequences.put(key, line);
		}
		
		Collections.sort(keys);
		
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			fm.writeLine(key);
			fm.writeLine(sequences.get(key));
		}
 		
	}

	@Override
	public void printHelp() {
		// TODO Auto-generated method stub
		
	}

}
