package javax.arang.stat;

import java.util.PriorityQueue;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class NumSort extends IOwrapper{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new NumSort().go(args[0], args[0] + ".sorted");
		} else {
			new NumSort().printHelp();
		}
	}

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line = "";
		PriorityQueue<String> sortedQ = new PriorityQueue<String>();
		while(fr.hasMoreLines()) {
			line = fr.readLine().trim();
			if (line.equals(""))	continue;
			sortedQ.add(line);
		}
		
		while (!sortedQ.isEmpty()) {
//			fm.writeLine(String.format("%21.0d", sortedQ.remove()));
			fm.writeLine(sortedQ.remove());
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar statNumSort.jar <in.list>");
		System.out.println("\t<in>: a list of numbers, one number in each line");
		System.out.println("\t<out>: sorted list");
	}
	
	

}
