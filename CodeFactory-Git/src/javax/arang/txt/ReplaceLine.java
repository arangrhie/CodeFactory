package javax.arang.txt;

import java.io.File;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ReplaceLine extends IOwrapper {

	private static int lineNum = 1;
	private static String lineToReplace = "";
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		int count = 0;
		while (fr.hasMoreLines()) {
			count++;
			if (count == lineNum) {
				fr.readLine();
				fm.writeLine(lineToReplace);
			} else {
				fm.writeLine(fr.readLine());
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: txtReplaceLine.jar <in.txt> <n> <text_to_replace>");
		System.out.println("Replace n'th line to <text_to_replace> in the file.");
		System.out.println("\t<n>: 1-base.");
		System.out.println("\t<text_to_replace>: if empty spaces needs to be also added, surround with \"s.");
		System.out.println("Arang Rhie, 2015-10-01. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			lineNum = Integer.parseInt(args[1]);
			lineToReplace = args[2];
			new ReplaceLine().go(args[0], args[0] + ".tmp");
			File f1 = new File(args[0]);
			f1.delete();
			File f2 = new File(args[0] + ".tmp");
			f2.renameTo(f1);
		} else {
			new ReplaceLine().printHelp();
		}
	}

}
