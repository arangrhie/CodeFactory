package javax.arang.txt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class SortByColumn extends IOwrapper {

	private static int k = 0;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		HashMap<String, StringBuffer> readToLines = new HashMap<String, StringBuffer>();
		ArrayList<String> readList = new ArrayList<String>();
		String readName;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#")) {
				fm.writeLine(line);
				continue;
			}
			tokens = line.split(RegExp.TAB);
			readName = tokens[k];
			if (readList.contains(readName)) {
				readToLines.put(readName, readToLines.get(readName).append(line + "\n"));
			} else {
				StringBuffer lines = new StringBuffer(line + "\n");
				readToLines.put(readName, lines);
				readList.add(readName);
			}
		}
		Collections.sort(readList);
		System.out.println(readList.size() + " unique keys");
		
		for (int i = 0; i < readList.size(); i++) {
			fm.write(readToLines.get(readList.get(i)).toString());
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtSortByColumn.jar <in.txt> <out.txt> [k]");
		System.out.println("\t<in.txt>: any tab-delimited text file.");
		System.out.println("\t<out.txt>: sorted by <k>'th column.");
		System.out.println("\t<k>: 1-based. DEFAULT=1");
		System.out.println("\t**TIP: for inner group sorting, sort with this code the closest subset column first, and the largest at last.");
		System.out.println("\t\tEx. Sort a sorted bed file by CONTIG-START position first (may use bedSort.jar), and then sort with 4th column");
		System.out.println("\t\t\tto get a bed file sorted by READ_NAME, and than by chromosomal coordinate.");
		System.out.println("Arang Rhie, 2015-10-01. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new SortByColumn().go(args[0], args[1]);
		} else if (args.length == 3) {
			k = Integer.parseInt(args[2]) - 1;
			new SortByColumn().go(args[0], args[1]);
		} else {
			new SortByColumn().printHelp();
		}
	}

}
