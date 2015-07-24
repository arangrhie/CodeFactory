package javax.arang.txt;

import java.util.Vector;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class TransposeColRaw extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line = fr.readLine();
		String[] tokens = line.split("\t");
		Vector<StringBuffer> newRaws = new Vector<StringBuffer>();
		for (int i = 0; i < tokens.length; i++) {
			newRaws.add(new StringBuffer(tokens[i]));
		}
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			if (tokens.length == 0)	break;
			for (int i = 0; i < tokens.length; i++) {
				newRaws.set(i, newRaws.get(i).append("\t" + tokens[i]));
			}
		}
		
		int size = newRaws.size();
		System.out.println("num raws: " + size);
		
		for (int i = 0; i < size; i++) {
			fm.writeLine(newRaws.get(i).toString());
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtTransposeColRaw.jar <in.txt> <out.txt>");
		System.out.println("\tTranspose tab-delemited columns and raws.");
		System.out.println("Arang Rhie, 2014-10-21. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new TransposeColRaw().go(args[0], args[1]);
		} else {
			new TransposeColRaw().printHelp();
		}
	}

}
