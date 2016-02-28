package javax.arang.bed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class SortByReadName extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		HashMap<String, StringBuffer> readToBed = new HashMap<String, StringBuffer>();
		ArrayList<String> readList = new ArrayList<String>();
		String readName;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#")) {
				fm.writeLine(line);
				continue;
			}
			tokens = line.split(RegExp.TAB);
			readName = tokens[Bed.NOTE];
			if (readList.contains(readName)) {
				readToBed.put(readName, readToBed.get(readName).append(line + "\n"));
			} else {
				StringBuffer lines = new StringBuffer(line + "\n");
				readToBed.put(readName, lines);
				readList.add(readName);
			}
		}
		Collections.sort(readList);
		for (int i = 0; i < readList.size(); i++) {
			fm.write(readToBed.get(readList.get(i)).toString());
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedSortByReadName.jar <in.bed> <out.bed>");
		System.out.println("\t<in.bed>: generated with bedSort.jar");
		System.out.println("\t<out.bed>: sorted by 4th column: ReadName.");
		System.out.println("Arang Rhie, 2015-10-01. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new SortByReadName().go(args[0], args[1]);
		} else {
			new SortByReadName().printHelp();
		}
	}

}
