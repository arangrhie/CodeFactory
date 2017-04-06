package javax.arang.genome.fasta;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ExtractFromList extends I2Owrapper {

	@Override
	public void hooker(FileReader frFasta, FileReader frScaffoldList, FileMaker fm) {
		String line;
		ArrayList<String> scaffoldList = new ArrayList<String>();
		HashMap<String, String> newScaffoldList = new HashMap<String, String>();	
		
		String scaffoldName;
		while (frScaffoldList.hasMoreLines()) {
			line = frScaffoldList.readLine();
			scaffoldName = ">" + line.trim();
			scaffoldList.add(scaffoldName);
			if (hasNewScaffoldNames) {
				line = frNewScaffoldReader.readLine();
				newScaffoldList.put(scaffoldName, ">" + line.trim());
			}
		}
		
		System.out.println("[DEBUG] :: Scaffolds listed: " + scaffoldList.size());
		if (hasNewScaffoldNames) {
			System.out.println("[DEBUG] :: New scaffold names listed: " + newScaffoldList.size());
			readFaExtractSeq(frFasta, scaffoldList, fm, newScaffoldList);
		} else {
			readFaExtractSeq(frFasta, scaffoldList, fm);
		}
	}
	
	public static ArrayList<String> readFaExtractSeq(FileReader frFasta, ArrayList<String> scaffoldList,
											FileMaker fm, HashMap<String, String> newScaffoldList) {
		String line;
		String[] tokens;
		boolean toInclude = false;
		ArrayList<String> scaffoldsWritten = new ArrayList<String>();
		String faName;
		while (frFasta.hasMoreLines()) {
			line = frFasta.readLine();
			if (line.startsWith(">")) {
				tokens = line.split(RegExp.WHITESPACE);
				faName = tokens[0].substring(1);
				if (scaffoldList.contains(tokens[0]) || scaffoldList.contains(faName)) {
					//System.out.println("[DEBUG] :: Adding " + newScaffoldList.get(tokens[0]));
					fm.writeLine(newScaffoldList.get(tokens[0]));
					scaffoldsWritten.add(faName);
					toInclude = true;
				} else {
					toInclude = false;
				}
			} else if (toInclude) {
				fm.writeLine(line);
			}
		}
		return scaffoldsWritten;
	}
	
	public static ArrayList<String> readFaExtractSeq(FileReader frFasta, ArrayList<String> scaffoldList, FileMaker fm) {
		String line;
		String[] tokens;
		boolean toInclude = false;
		String faName;
		ArrayList<String> scaffoldsWritten = new ArrayList<String>();
		while (frFasta.hasMoreLines()) {
			line = frFasta.readLine();
			if (line.startsWith(">")) {
				tokens = line.split(RegExp.WHITESPACE);
				faName = tokens[0].substring(1);
				if (scaffoldList.contains(tokens[0]) || scaffoldList.contains(faName)) {
					//System.out.println("[DEBUG] :: Adding " + line);
					fm.writeLine(line);
					toInclude = true;
					scaffoldsWritten.add(faName);
				} else {
					toInclude = false;
				}
			} else if (toInclude) {
				fm.writeLine(line);
			}
		}
		return scaffoldsWritten;
	}
	

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaExtractFromList.jar <in.fasta> <in_fasta.list> <out.fasta> [new_scaffold_name.list]");
		System.out.println("\t<in.fasta>: Total list of fasta file. Matches only the first token of lines starting with >.");
		System.out.println("\t<in_scaffold.list>: List of scaffold (contig) names, with no \">\"");
		System.out.println("\t<out.fasta>: Fasta file containing only listed scaffolds");
		System.out.println("\t[new_scaffold_name.list]: Optional. Instead of names used in <in_scaffold.list>, use these names");
		System.out.println("\t\tThe order must be the same as in <in_scaffold.list>");
		System.out.println("\t*Recommended -Xmx option slightly higher than <in_scaffold.list> +  [new_scaffold_name.list] size");
		System.out.println("Arang Rhie, 2015-06-04. arrhie@gmail.com");
	}
	
	static boolean hasNewScaffoldNames = false;
	static FileReader frNewScaffoldReader = null;
	
	public static void main(String[] args) {
		if (args.length == 3) {
			new ExtractFromList().go(args[0], args[1], args[2]);
		} else if (args.length == 4) {
			hasNewScaffoldNames = true;
			frNewScaffoldReader = new FileReader(args[3]);
			new ExtractFromList().go(args[0], args[1], args[2]);
		} else {
			new ExtractFromList().printHelp();
		}
	}

}
