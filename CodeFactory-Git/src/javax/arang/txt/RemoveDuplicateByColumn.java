package javax.arang.txt;

import java.util.HashSet;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class RemoveDuplicateByColumn extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		HashSet<String> col = new HashSet<String>();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			if (!col.contains(tokens[columnIdx])) {
				System.out.println(line);
				col.add(tokens[columnIdx]);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtRemoveDuplicateByColumn.jar <in.txt> [column_idx]");
		System.out.println("\tRemove duplicated data, and remain only the unique values contained in selected column.");
		System.out.println("\tsimilar to sort -u, but preserves the original order.");
		System.out.println("\t<in.txt>: tab(or whitespace) delimited txt file");
		System.out.println("\t<column_idx>: column index to look for duplicates. 1-based. DEFAULT=1");
		System.out.println("Arang Rhie, 2017-12-15. arrhie@gmail.com");
	}

	private static int columnIdx = 0; 
	public static void main(String[] args) {
		if (args.length == 1) {
			new RemoveDuplicateByColumn().go(args[0]);
		} else if (args.length == 2) {
			columnIdx = Integer.parseInt(args[1]) - 1;
			new RemoveDuplicateByColumn().go(args[0]);
		} else {
			new RemoveDuplicateByColumn().printHelp();
		}
	}

}
