package javax.arang.test;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

public class ReadingTest extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			System.out.println(line);
		}
	}

	@Override
	public void printHelp() {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ReadingTest().go(args[0]);
		} else {
			new ReadingTest().printHelp();
		}
	}

}
