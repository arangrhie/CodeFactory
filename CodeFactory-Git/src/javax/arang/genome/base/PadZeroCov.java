package javax.arang.genome.base;

import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.arang.IO.INwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class PadZeroCov extends INwrapper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			new PadZeroCov().printHelp();
			System.exit(-1);
		} else {
			new PadZeroCov().go(args);
		}
		
	}


	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar padZeroCov.jar <inFile1> <inFile2> ... <inFileN>");
		System.out.println("\t<inFile>: chr pos ref A C G T cov QA");
	}


	@Override
	public void hooker(ArrayList<FileReader> frs) {
		String line;
		StringTokenizer st;
		for (FileReader fr : frs) {
			int prevPos = 31728063;
			FileMaker fm = new FileMaker(getPath(fr.getFullPath()), fr.getFileName().replace(".bas", "_0padded.bas"));
			System.out.println("In file " + fm.getFileName());
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				st = new StringTokenizer(line);
				String chr = st.nextToken();
				int pos = Integer.parseInt(st.nextToken());
				if (pos <= prevPos)	continue;
				while (prevPos + 1 < pos) {
					prevPos++;
					fm.writeLine(chr + "\t" + prevPos + "\tN\t0\t0\t0\t0\t0\t0");
					System.out.println(prevPos);
				}
				fm.writeLine(line);
				prevPos = pos;
			}
		}
		
	}

}
