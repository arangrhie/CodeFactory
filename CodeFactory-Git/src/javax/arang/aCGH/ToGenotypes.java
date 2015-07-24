/**
 * 
 */
package javax.arang.aCGH;

import java.util.Vector;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class ToGenotypes extends Rwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.Rwrapper#hooker(javax.arang.IO.FileReader)
	 */
	@Override
	public void hooker(FileReader fr) {
		String header = fr.readLine();
		int numColumns = header.split("\t").length;
		Vector<String[]> array = new Vector<String[]>();
		while (fr.hasMoreLines()) {
			array.add(fr.readLine().split("\t"));
		}
		System.out.println("Input file contains " + array.size() + " lines");
		String newFileExt;
		for (Float cut : cuts) {
			newFileExt = IS_BINARY ? "_" + String.format("%,.2f", cut) + ".cnv" : "_" + String.format("%,.2f", cut) + "_num.cnv";
			FileMaker fm = new FileMaker(fr.getDirectory(), fr.getFileName().replace(".txt", newFileExt));
			System.out.println("Into " + fm.getFileName());
			fm.writeLine(header);
			for (String[] tokens : array) {
				fm.write(tokens[0]);
				for (int i = 1; i < probeIdx; i++) {
					fm.write("\t" + tokens[i]);
				}
				for (int i = probeIdx; i < numColumns; i++) {
					if (tokens[i].equals("NA")) {
						fm.write("\tNA");
					} else {
						float val = Float.parseFloat(tokens[i]);
						if (val < cut * -1) {
							fm.write(IS_BINARY ? "\t-1" : "\t" + (Math.floor(val/cut)));
						}
						else if (val >= cut) {
							fm.write(IS_BINARY ? "\t1" : "\t" + (Math.floor(val/cut)));
						}
						else fm.write("\t0");
					}
				}
				fm.writeLine();
			}
			fm.closeMaker();
		}
		
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.Rwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar aCGHtoGenotypes.jar <in_array.txt> <probe_begin> [cut] [IS_BINARY]");
		System.out.println("\t<in_array.txt>: Sample_ID\tOS\t...\tProbe1\tProbe2\t...");
		System.out.println("\t<probe_begin>: 1-based, DEFAULT=1");
		System.out.println("\t[cut]: if not specified, cut is from 0.05 to 2.00 by increase of 0.05.");
		System.out.println("\t[IS_BINARY]: DEFAULT=TRUE. If set to false, CNVs will be reported with log2R/cut.");
		System.out.println("\t\t+cut, -cut is the interval line for +1, 0, -1, or +2 +1 0 -1 -2 ... if IS_BINARY=FALSE.");
		System.out.println("2014-11-18");
	}

	private static Vector<Float> cuts = new Vector<Float>();
	private static int probeIdx = 0;
	private static boolean IS_BINARY = true;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		if (args.length == 4) {
			cuts.add(Float.parseFloat(args[2]));
			probeIdx = Integer.parseInt(args[1]) - 1;
			IS_BINARY = Boolean.parseBoolean(args[3]);
			new ToGenotypes().go(args[0]);
		} else if (args.length == 3) {
			cuts.add(Float.parseFloat(args[2]));
			probeIdx = Integer.parseInt(args[1]) - 1;
			new ToGenotypes().go(args[0]);
		}else if (args.length >= 2) {
			int cutVal = 0;
			for (int i = 0; i < 41; i++) {
				cuts.add(cutVal/100.0f);
				cutVal += 5;
			}
			probeIdx = Integer.parseInt(args[1]) - 1;
			new ToGenotypes().go(args[0]);
		} else {
			new ToGenotypes().printHelp();
		}
	}

}
