package javax.arang.genome.snp;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class VarInRange extends IOwrapper {

	final int POS_1 = 1;
	final int POS_2 = 2;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			int pos = 0;
			try {
				pos = Integer.parseInt(tokens[POS_1]);
			} catch (NumberFormatException e) {
				System.out.println("\"" + tokens[POS_1] + "\" is not an integer value.skip this line.");
				fm.writeLine(line);
			}
			if (pos < FROM || pos > TO)	continue;
			fm.writeLine(line);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("java -jar varInRange.jar <annovar.in> <from> <to>");
		System.out.println("\t<output>: variation list with chr position <from> ~ <to>, inclusive.");
	}

	static int FROM;
	static int TO;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 3) {
			FROM = Integer.parseInt(args[1]);
			TO = Integer.parseInt(args[2]);
			new VarInRange().go(args[0], args[0] + "." + args[1] + "-" + args[2]);
		} else {
			new VarInRange().printHelp();
		}
	}

}
