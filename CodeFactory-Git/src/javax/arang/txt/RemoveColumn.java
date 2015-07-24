package javax.arang.txt;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class RemoveColumn extends IOwrapper {

	static int n;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@") || line.equals(""))	continue;
			tokens = line.split("\t");
			String newLine = "";
			for (int i = 0; i < tokens.length; i++) {
				if (i == n) continue;
				newLine = newLine + tokens[i] + "\t";
			}
			fm.writeLine(newLine.trim());
		}
	}

	@Override
	public void printHelp() {
		System.out.println("java -jar txtRemoveColumn.jar <in.txt> <N>");
		System.out.println("\t<N>: Nth column to remove. 0-base.");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			n = Integer.parseInt(args[1]);
			new RemoveColumn().go(args[0], args[0].replace(".", "_" + n + "."));
		} else {
			new RemoveColumn().printHelp();
		}

	}

}
