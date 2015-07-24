package javax.arang.txt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class StarAggregateStat extends INOwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtStarAggregateStat.jar <*.stat>");
		System.out.println("\tAggregate STAR final alignment log files.");
		System.out.println("\t<*.stat>: STAR Log.final.out files");
		System.out.println("\t<out>: StarStat.summary");
		System.out.println("2015-03-19, Arang Rhie. arrhie@gmail.com");
	}

	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		String line;
		String[] tokens;
		Vector<String> categories = new Vector<String>();
		HashMap<String, String> outTable = new HashMap<String, String>();
		boolean isFirst = true;
		int lineCount = 0;
		fm.write("Category");
		for (FileReader fr : frs) {
			fm.write("\t" + fr.getFileName().replace(".stat", ""));
			System.out.println("Reading " + fr.getFileName());
			while(fr.hasMoreLines()) {
				line = fr.readLine();
				lineCount++;
				if(lineCount < 6) {
					continue;
				}
				tokens = line.trim().split("\t");
				if(tokens.length < 2)	continue;
				if(isFirst) {
					categories.add(tokens[0].replace(" |", ""));
					outTable.put(tokens[0].replace(" |", ""), "\t" + tokens[1]);
				} else {
					outTable.put(tokens[0].replace(" |", ""), outTable.get(tokens[0].replace(" |", "")) + "\t" + tokens[1]);
				}
			}
			isFirst = false;
			lineCount = 0;
		}
		fm.writeLine();
		
		int i = 0;
		for (String category : categories) {
			if (i == 2)	fm.writeLine("UNIQUE READS");
			else if (i == 16)	fm.writeLine("MULTI-MAPPING READS");
			else if (i == 20)	fm.writeLine("UNMAPPED READS");
			fm.writeLine(category + outTable.get(category));
			i++;
		}
	}

	public static void main(String[] args) {
		if (args.length > 1) {
			new StarAggregateStat().go(args, "StarStat.summary");
		} else {
			new StarAggregateStat().printHelp();
		}
	}

}
