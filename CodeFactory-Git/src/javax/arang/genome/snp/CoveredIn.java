package javax.arang.genome.snp;

import java.util.StringTokenizer;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;

public class CoveredIn extends R2wrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2) {
		
		int pos;
		int basePos;
		
		int hasInF2 = 0;
		
		StringTokenizer st;
		String line;
		String uncoveredList = "";
		while (fr1.hasMoreLines()) {
			pos = Integer.parseInt(fr1.readLine().toString().trim().replaceAll(",", ""));
			if (fr2.hasMoreLines()) {
				do {
					line = fr2.readLine().toString();
					st = new StringTokenizer(line);
					st.nextToken();
					basePos = Integer.parseInt(st.nextToken().toString());
					if (basePos == pos) {
						hasInF2++;
					}
				} while (basePos < pos);
				if (basePos != pos) {
					uncoveredList = uncoveredList + "\n" + pos;
				}
			}
		}
		System.out.println(fr1.getFullPath() + " has " + hasInF2 + " # of common positions in " + fr2.getFullPath());
		System.out.println("Uncovered positions:");
		System.out.println(uncoveredList);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CoveredIn cIn = new CoveredIn();
		if (args.length == 2) {
			cIn.go(args[0], args[1]);
		} else {
			cIn.printHelp();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar coveredIn.jar <inFile1> <inFile2>");
		System.out.println("\tinFile1: list of positions to compare");
		System.out.println("\tinFile2: base sort file (.bas)");
		System.out.println("Arang Rhie, Oct. 19. 2011");
		
	}

}
