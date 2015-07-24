/**
 * 
 */
package javax.arang.R.input.cox;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class QTtoCovariat extends IOwrapper {

	static float cutVal = 2;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		fm.writeLine(fr.readLine());
		String[] tokens;
		StringBuffer converted = new StringBuffer();
		while (fr.hasMoreLines()) {
			tokens = fr.readLine().split("\t");
			converted.append(tokens[0] + "\t" + tokens[1] + "\t" + tokens[2] + "\t" + tokens[3]);
			for (int i = 4; i < tokens.length; i++) {
				if (tokens[i].equals("NA")) {
					converted.append("\tNA");
					continue;
				}
				int convertedVal = getInterval(Float.parseFloat(tokens[i]));
				converted.append("\t" + convertedVal);
			}
			fm.writeLine(converted.toString());
			converted = new StringBuffer();
		}
	}

	/**
	 * @param parseFloat
	 * @return
	 */
	private static int getInterval(float floatVal) {
		boolean isNegative = (floatVal<0);
		if (isNegative) {
			return (int) ((floatVal - cutVal/2) / cutVal);
		} else {
			return (int) ((floatVal + cutVal/2) / cutVal);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: qt2covariat.jar <input> [cut-off]");
		System.out.println("\t<input>: sample_id\tsurv_time\tcensoring_evt\tgroup\tsamples..");
		System.out.println("\t<cut-off>: if 2, {-1 ~ +1} will be the middle cut-off value");
		System.out.println("\t\twith no cut-off value specified, program will automatically increment 20 times.");

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			//new QTtoCovariat().go(args[0], args[0].replace(".", "_" + 2 + "."));
			cutVal = 0.9500000f;
			float incr = 0.050000f;
			QTtoCovariat obj = new QTtoCovariat();
			for (int i = 1; i < 20; i++) {
				System.out.println(cutVal + incr);
				cutVal = cutVal + incr;
				obj.go(args[0], args[0].replace(".txt", "_" + String.format("%.2f", cutVal) + ".txt"));	
			}
		} else if (args.length == 2) {
			cutVal = Float.parseFloat(args[1]);
			new QTtoCovariat().go(args[0], args[0].replace(".", "_" + cutVal + "."));
		} else {
			new QTtoCovariat().printHelp();
		}

	}

}
