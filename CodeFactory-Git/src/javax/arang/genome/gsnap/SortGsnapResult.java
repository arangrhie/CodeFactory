package javax.arang.genome.gsnap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class SortGsnapResult {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			new SortGsnapResult().go(args[0], args[1]);
		} else {
			String inFile = "C://Documents and Settings/아랑/바탕 화면/ak6_chr20.indel.result";
			String outFile = "ak6_chr20.indel.result.sort";
			new SortGsnapResult().go(inFile, outFile);
		}
	}
	
	public void go(String inFile, String outFile) {
		FileReader fr = new FileReader(inFile);
		
		String line = "";
		
		ArrayList<String> keys = new ArrayList<String>();
		HashMap<String, String> alignResult = new HashMap<String, String>();
		
		String prevKey = "";
		String key = "";
		String prevValue = "";
		String value = "";
		while (fr.hasMoreLines()) {
			line = fr.readLine().toString();
			
			if (line.startsWith(">")) {
				prevKey = key;
				prevValue = value;
				if (!prevValue.equals("")) {
					// System.out.println("prevKey: " + prevKey);
					alignResult.put(prevKey, prevValue + "\n\n");
				}
				value = line;
				StringTokenizer st = new StringTokenizer(line.trim());
				st.nextToken();
				st.nextToken();
				st.nextToken();
				key = st.nextToken();
				keys.add(key);
			} else {
				value = value + "\n" + line;
			}
		}
		
		if (!key.equals("")) {
			// System.out.println("put last key: " + key);
			alignResult.put(key, value);
		}
		
		Collections.sort(keys);
		System.out.println("# of read pairs: " + keys.size());
		
		fr.closeReader();
		
		String path = inFile.substring(0, inFile.lastIndexOf("/"));
		FileMaker fm = new FileMaker(path, outFile);
		
		for (int i = 0; i < keys.size(); i++) {
			try {
				String results = alignResult.get(keys.get(i));
				fm.write(results);
				//	System.out.println((keys.get(i)));
			} catch (NullPointerException e) {
				System.out.println("null pointer exception at i=" + i + " " + keys.get(i));
				System.out.println(alignResult.get(keys.get(i)));
			}
		}
		fm.closeMaker();
		
	}

}
