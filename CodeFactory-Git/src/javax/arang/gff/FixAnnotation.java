package javax.arang.gff;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class FixAnnotation extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String attributes;
		String[] tags;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			
			if (line.startsWith("#")) {
				System.out.println(line);
				continue;
			}
			
			tokens = line.split(RegExp.TAB);
			for (int i = 0; i < GFF.NOTE; i++) {
				System.out.print(tokens[i] + "\t");
			}
			
			// TODO: Validate tab-separated columns
			
			// Get "note" in the attributes
			attributes = tokens[GFF.NOTE];
			tags = attributes.split(RegExp.SEMICOLON);
			
			for (int i = 0; i < tags.length; i++) {
				System.out.print(tags[i]);
				if (tags[i].startsWith("note")) {
					for (int j = i+1; j < tags.length; j++) {
						if (   tags[j].startsWith("isoform=")
							|| tags[j].startsWith("non-AUG=")
							|| tags[j].startsWith("similar=")
							|| tags[j].startsWith("source=")) {
							System.out.print("%3B " + tags[j].split("=")[0] + " " + tags[j].split("=")[1]);
							System.err.println(line); // keep record of the original line
							i=j;
						} else {
							break;
						}
					}
				}
				if (i < tags.length - 1) {
					System.out.print(";");
				}
			}
			
			// print the rest of the tags if any
			if (tokens.length - 1 > GFF.NOTE) {
				for (int i = (GFF.NOTE + 1); i < tokens.length; i++) {
					System.out.print("\t" + tokens[i]);
				}
			}
			System.out.println();
		}
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar gff3FixAnnotation.jar in.gff");
		System.err.println("Fix annotation formatting issues in attributes field");
		System.err.println("2023-07-05");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new FixAnnotation().go(args[0]);
		} else {
			new FixAnnotation().printHelp();
		}
	}

}
