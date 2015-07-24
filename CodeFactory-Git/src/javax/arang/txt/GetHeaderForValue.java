package javax.arang.txt;

import java.util.ArrayList;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class GetHeaderForValue extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line = fr.readLine();
		String[] tokens = line.split(RegExp.TAB);
		ArrayList<String> header = new ArrayList<String>();
		
		for (int i = 0; i < tokens.length; i++) {
			header.add(tokens[i]);
		}
		
		boolean isFirst = true;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			fm.write(tokens[0]);
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].equals(value)) {
					if (isFirst) {
						fm.write("\t" + header.get(i));
						isFirst = false;
					} else {
						fm.write("," + header.get(i));
					}
				}
			}
			fm.writeLine();
			isFirst = true;
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtGetHeaderForValue.jar <value> <in.tab> <out>");
		System.out.println("\tOutputs the header column name containing value");
	}

	private static String value = "";
	public static void main(String[] args) {
		if (args.length == 3) {
			value = args[0];
			new GetHeaderForValue().go(args[1], args[2]);
		} else {
			new GetHeaderForValue().printHelp();
		}
		
	}

}
