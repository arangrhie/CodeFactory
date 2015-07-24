package javax.arang.txt;

import java.util.ArrayList;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class RemoveIntersectByColumn extends I2Owrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		String line;
		String[] tokens;
		ArrayList<String> keyList = new ArrayList<String>();
		
		while (fr2.hasMoreLines()) {
			line = fr2.readLine();
			tokens = line.split(RegExp.TAB);
			keyList.add(tokens[keyIdx]);
		}
		System.out.println("[DEBUG] :: Number of keys to compare in " + fr2.getFileName() + ": " + keyList.size());
		
		// write the header line
		fm.writeLine(fr1.readLine());
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			tokens = line.split(RegExp.TAB);
			if (keyList.contains(tokens[keyIdx]))	continue;
			else	fm.writeLine(line);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("usage: java -jar txtRemoveIntersectByColumn.jar <in1.tab> <in2.tab> <out.tab> <keyIdx>");
		System.out.println("\t<in1.tab>: File to retain with non-intersections");
		System.out.println("\t<in2.tab>: File to compare to see for intersections");
		System.out.println("\t<keyIdx>: 1-based, column index containing the key of comparison");
		System.out.println("Arang Rhie, 2015-04-01. arrhie@gmail.com");
	}

	private static int keyIdx = 0;
	public static void main(String[] args) {
		if (args.length == 4) {
			keyIdx = Integer.parseInt(args[3]) - 1;
			new RemoveIntersectByColumn().go(args[0], args[1], args[2]);
		} else {
			new RemoveIntersectByColumn().printHelp();
		}
	}

}
