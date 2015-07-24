package javax.arang.R.input.polymer;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class MakeHomopolymerIn extends IOwrapper {

	static String base = "A";
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line = "";
		String[] tokens;
		int[] readAs = null;
		int[] numVal = null;
		int polymerLen = 3;

		try {
			fm.writeLine("Sequenced Length\tPolymer Length");
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				line = line.trim();
				if (line.length() < 5) {
					for (int i = 0; i < readAs.length; i++) {
						for (int times = 0; times < numVal[i]; times++) {
							fm.writeLine(readAs[i] + "\t" + polymerLen);
						}
					}
					continue;
				}
				tokens = line.split("\t");
				if (line.startsWith("Poly")) {
					readAs = new int[tokens.length - 1];
					numVal = new int[tokens.length - 1];
					try {
						polymerLen = Integer.parseInt(tokens[0].replace("Poly", ""));
					} catch (NumberFormatException e) {
						polymerLen = Integer.parseInt(tokens[0].replace("Poly", "").replace(" (%)", ""));
					}
					for (int i = 1; i < tokens.length; i++) {
						readAs[i - 1] = Integer.parseInt(tokens[i]);
					}
				} else if (tokens[0].equals(base)) {
					for (int i = 1; i < tokens.length; i++) {
						numVal[i - 1] = (int) (Float.parseFloat(tokens[i]) * 10);
					}
				}

			}
			
		} catch (IndexOutOfBoundsException e) {
			System.out.println(line);
			throw e;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("java -jar rMakeHomopolymerIn.jar <in.txt>");
		System.out.println("\t<output>: <r-matrix format out.txt>");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			base = "A";
			new MakeHomopolymerIn().go(args[0], args[0].replace(".txt", "_A_r.txt"));
			base = "T";
			new MakeHomopolymerIn().go(args[0], args[0].replace(".txt", "_T_r.txt"));
			base = "G";
			new MakeHomopolymerIn().go(args[0], args[0].replace(".txt", "_G_r.txt"));
			base = "C";
			new MakeHomopolymerIn().go(args[0], args[0].replace(".txt", "_C_r.txt"));
			base = "GC";
			new MakeHomopolymerIn().go(args[0], args[0].replace(".txt", "_GC_r.txt"));
		} else {
			new MakeHomopolymerIn().printHelp();
		}

	}

}
