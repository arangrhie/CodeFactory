package javax.arang.txt;

import java.util.HashSet;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class RemoveDuplicateByColumn extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		HashSet<String> col = new HashSet<String>();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			if (!col.contains(tokens[columnIdx])) {
				fm.writeLine(line);
				col.add(tokens[columnIdx]);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtRemoveDuplicateByColumn.jar <in.txt> <out.txt> [column_idx]");
		System.out.println("\tRemove duplicated data, and remain only the unique values contained in selected column.");
		System.out.println("\t<in.txt>: tab(or whitespace) delimited txt file");
		System.out.println("\t<out.txt>: output containing unique values in <column_idx>");
		System.out.println("\t<column_idx>: column index to look for duplicates. 1-based. DEFAULT=1");
	}

	private static int columnIdx = 0; 
	public static void main(String[] args) {
		if (args.length == 2) {
			new RemoveDuplicateByColumn().go(args[0], args[1]);
		} else if (args.length == 3) {
			columnIdx = Integer.parseInt(args[2]) - 1;
			new RemoveDuplicateByColumn().go(args[0], args[1]);
		} else {
			new RemoveDuplicateByColumn().printHelp();
		}
	}

}
