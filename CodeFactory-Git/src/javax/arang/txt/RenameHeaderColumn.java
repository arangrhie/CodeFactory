/**
 * 
 */
package javax.arang.txt;

import java.util.Vector;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class RenameHeaderColumn extends IOwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String tokens[];
		boolean isHeader = true;
		StringBuffer newLine = new StringBuffer();
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (isHeader) {
				tokens = line.split("\t");
				for (int i = 0; i < tokens.length; i++) {
					if (columnsOld.contains(tokens[i])) {
						newLine.append(columnsNew.get(columnsOld.indexOf(tokens[i])) + "\t");
					} else {
						newLine.append(tokens[i] + "\t");
					}
				}
				fm.writeLine(newLine.toString().trim());
				isHeader = false;
			} else {
				fm.writeLine(line);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtRenameHeaderColumn.jar <in.txt> <out.txt> <column_name_old> <column_name_new> [<column_name_old> <column_name_new>]");
		System.out.println("\tRenames one or more columns in the header line.");
		System.out.println("\t\tArang Rhie, 2014-01-21. arrhie@gmail.com");
	}

	private static Vector<String> columnsOld = null;
	private static Vector<String> columnsNew = null;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length >= 4) {
			columnsOld = new Vector<String>();
			columnsNew = new Vector<String>();
			for (int i = 2; i < args.length; i++) {
				if (i%2 == 0) {
					columnsOld.add(args[i]);
				} else {
					columnsNew.add(args[i]);
				}
			}
			new RenameHeaderColumn().go(args[0], args[1]);
		} else {
			new RenameHeaderColumn().printHelp();
		}
		
	}

}
