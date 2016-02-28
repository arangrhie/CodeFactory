package javax.arang.txt;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class AddHeader extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		if (header.contains("\\t")) {
			header = header.replace("\\t", "\t");
		}
		if (header.contains("\\n")) {
			header = header.replace("\\n", "\n");
		}
		
		System.out.println(header);
		fm.writeLine(header);
		while (fr.hasMoreLines()) {
			fm.writeLine(fr.readLine());
		}
	}

	@Override
	public void printHelp() {
		System.out.println("java -jar txtAddHeader.jar <in.txt> \"<header>\" [out.txt]");
		System.out.println("\tAdd <header> in head");
		System.out.println("\t<in.txt>: any text file");
		System.out.println("\t<header>: line(s) to insert. make sure to surround with \" \" .");
		System.out.println("\t[out.txt]: DEFAULT = <in.txt.wi_header>");
		System.out.println("Arang Rhie, 2015-10-05. arrhie@gmail.com");
	}

	private static String header;
	public static void main(String[] args) {
		if (args.length == 2) {
			header = args[1];
			new AddHeader().go(args[0], args[0] + ".wi_header");
		} else if (args.length == 3) {
			header = args[1];
			new AddHeader().go(args[0], args[2]);
		} else {
			new AddHeader().printHelp();
		}
	}

}
