package javax.arang.bed;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class FindContigSpanning extends IOwrapper {

	private static short CHR_1 = 0;
	private static short CHR_2 = 3;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			if (!tokens[CHR_2].equals("") && !tokens[CHR_1].equals(tokens[CHR_2])) {
				fm.writeLine(line);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedFindContigSpanning.jar <in.bed> <out.bed>");
		System.out.println("\t<in.bed>: pair1 bed (tab) pair2 bed (tab) ...");
		System.out.println("\t<out.beD>: only lines with discordant regions");
		System.out.println("Arang Rhie, 2015-09-24. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new FindContigSpanning().go(args[0], args[1]);
		} else {
			new FindContigSpanning().printHelp();
		}
	}

}
