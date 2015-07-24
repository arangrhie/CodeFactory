package javax.arang.ref.util;

import java.util.StringTokenizer;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class MergeRegions extends IOwrapper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new MergeRegions().go(args[0], args[1]);
		} else {
			new MergeRegions().printHelp();
		}
	}

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		StringTokenizer st;
		line = fr.readLine().toString();
		st = new StringTokenizer(line);
		String chrP = st.nextToken();
		int posP1 = Integer.parseInt(st.nextToken());
		int posP2 = Integer.parseInt(st.nextToken());
		
		boolean merge = false;
		while (fr.hasMoreLines()) {
			line = fr.readLine().toString();
			st = new StringTokenizer(line);
			String chrN = st.nextToken();
			int posN1 = Integer.parseInt(st.nextToken());
			int posN2 = Integer.parseInt(st.nextToken());
			
			if (chrP.equals(chrN)) {
				if (posN1 - posP2 < 500) {
					merge = true;
					posN1 = posP1;
					System.out.println("merge " + chrP + "\t" + posP1 + " ~ " + posN2);
				} else {
					merge = false;
					fm.writeLine(chrP + "\t" + posP1 + "\t" + posP2);
				}
			} else {
				if (merge) {
					fm.writeLine(chrP + "\t" + posP1 + "\t" + posP2);
				}
			}
			
			chrP = chrN;
			posP1 = posN1;
			posP2 = posN2;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar mergeRegions.jar <inFile> <outFile>");
		System.out.println("<inFile>: list of file, each line has chrN\tpos1\tpos2");
		System.out.println("<outFile>: list of file, with positions not overlapping within 500 bases");
	}

}
