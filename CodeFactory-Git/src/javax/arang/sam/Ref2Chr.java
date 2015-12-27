package javax.arang.sam;

import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class Ref2Chr extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		HashMap<String, String> refMap = new HashMap<String, String>();
		refMap.put("*", "*");
		
		String line;
		StringTokenizer st;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine().toString();
			if (line.startsWith("@")) {
				if (line.startsWith("@SQ")) {
					st = new StringTokenizer(line, ":\t");
					fm.write(st.nextToken() + "\t");
					fm.write(st.nextToken() + ":");
					String ref = st.nextToken();
					String chr = "chr" + getChrN();
					refMap.put(ref, chr);
					fm.writeLine(chr + "\t" + line.substring(line.indexOf("LN:")));
				} else {
					fm.writeLine(line);
					continue;
				}
			} else {
				st = new StringTokenizer(line);
				fm.write(st.nextToken() + "\t" + st.nextToken() + "\t");	// readId + flag
				String ref = st.nextToken();
				String chr = refMap.get(ref);
				fm.write(chr + "\t");
				fm.write(st.nextToken() + "\t" + st.nextToken() + "\t" + st.nextToken() + "\t");	// readId + flag
				ref = st.nextToken();
				chr = refMap.get(ref);
				fm.write(chr + "\t");
				while (st.hasMoreTokens()) {
					fm.write(st.nextToken() + "\t");
				}
				fm.writeLine("");
			}
			
		}
		
		Set<String> keys = refMap.keySet();
		for (String key : keys) {
			System.out.println(key + "\t" + refMap.get(key));
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar ref2Chr.jar <samInFile> <samOutFile>");
		System.out.println("Convert the reference to a chr number, 1~22, 26~.");
	}
	
	static int chrN = 0;
	private int getChrN() {
		if (chrN == 22) {
			chrN += 3;
		}
		return ++chrN;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new Ref2Chr().go(args[0], args[1]);
		} else {
			new Ref2Chr().printHelp();
		}
	}

}
