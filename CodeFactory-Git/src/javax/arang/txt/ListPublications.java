package javax.arang.txt;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ListPublications extends Rwrapper {

	private static int l1 = 0;
	private static int l2 = 1;
	private static int l3 = 2;
	private static int l4 = 3;
	
	@Override
	public void hooker(FileReader fr) {
		
		String line;
		String[] names;
		String[] tokens;
		
		String title = "";
		String authors = "";
		String journal = "";
		
		int line_num = 0;
		
		String lastName = "";
		String initials = "";
		int lastNameIdx = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			// System.out.println(line);
			if (line_num == l1) {
				title = line;
			} else if (line_num == l3) {
				journal = line;
			} else if (line_num == l2) {
				line = line.replaceFirst("&", ",");
				names = line.split(RegExp.COMMA);
				for (int i = 0; i < names.length; i++) {
					names[i] = names[i].trim();
					if (names[i].startsWith("View ORCID Profile")) {
						names[i] = names[i].substring(18);
					}
					
					tokens = names[i].split(RegExp.WHITESPACE);
					lastNameIdx = -1;
					for (int j = 0; j < tokens.length; j++) {
						tokens[j] = tokens[j].trim();
						if (tokens[j].toUpperCase().startsWith("HTTPS")
								|| tokens[j].matches(RegExp.NUMBERS) )	{
							continue;
						} else {
							lastNameIdx++;
						}
					}
					initials = "";
					for (int j = 0; j < lastNameIdx; j++) {
						if ( tokens[j].equalsIgnoreCase("AND")) continue;
						initials += tokens[j].charAt(0);
					}
					lastName = tokens[lastNameIdx].toLowerCase().substring(1);
					authors += tokens[lastNameIdx].charAt(0) + lastName + " " + initials + ", ";
				}
			} else if (line_num == l4) {
				System.out.println(authors + title + ". " + journal);
				authors = "";
				title = "";
				journal = "";
			}
			line_num++;
			line_num %= 4;
		}
		System.out.println(authors + title + ". " + journal);
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar txtListPublications.jar in.txt");
		System.err.println("  in.txt  line1: Title");
		System.err.println("          line2: Authors");
		System.err.println("          line3: Journal, Year");
		System.err.println("          line4: blank");
		System.err.println("Arang Rhie, 2023-05-10");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ListPublications().go(args[0]);
		} else {
			new ListPublications().printHelp();
		}
	}

}
