package javax.arang.txt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ListAuthors extends Rwrapper {

	private static final int FIRST = 0;
	private static final int MIDDLE = 1;
	private static final int LAST = 2;
	private static final int AFF = 3;
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String name;
		String aff;
		String[] affs;
		
		// names
		ArrayList<String>		names = new ArrayList<String>();
		// affiliations
		ArrayList<String>		affiliations = new ArrayList<String>();
		
		// names and aff. numbering
		HashMap<String, String> nameToAffiliation  = new HashMap<String, String>();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			// collect name
			name = tokens[FIRST] + " ";
			
			// has middle name
			if (!tokens[MIDDLE].equals("")) {
				String middle = tokens[MIDDLE];
				for (int i = 0; i < middle.length(); i++) {
					name += middle.charAt(i) + ".";
				}
				name += " ";
			}
			
			name += tokens[LAST];
			names.add(name);

			// initialize
			aff = "";

			// collect affiliations
			affs = tokens[AFF].split(";");

			for (int i = 0; i < affs.length; i++) {
				String affT = affs[i].trim();
				
				// skip if "retired"
				if (affT.contains("retired") || affT.contains("Retired")) {
					// do nothing
					continue;
				}
				
				// never seen aff?
				if (!affiliations.contains(affT)) {
					// add to the bag
					affiliations.add(affT);
				}
				
				// collect aff #
				if (i == 0) {
					aff = "" + (affiliations.indexOf(affT) + 1);
				} else {
					// aff += '\u2019' + superscript_html(affiliations.indexOf(affT));
					aff += "," + (affiliations.indexOf(affT) + 1);
				}
			}
			
			nameToAffiliation.put(name, superscript_html(aff));
		}
		
		// print author names
		// First author
		System.out.print(names.get(0) + nameToAffiliation.get(names.get(0)));
		for (int i = 1; i < names.size(); i++) {
			System.out.print(", " + names.get(i)+ nameToAffiliation.get(names.get(i)));
		}
		
		System.out.println();
		System.out.println();
		
		// print affiliations
		for (int i = 0; i < affiliations.size(); i++) {
			System.out.println((i + 1) + ". " + affiliations.get(i));
		}
		
		// Let's see if we need to further clean up the affiliations
		System.out.println();
		Collections.sort(affiliations);
		for (int i = 0; i < affiliations.size(); i++) {
			System.out.println((i + 1) + ". " + affiliations.get(i));
		}
		
	}
	
	private static String superscript_html(String in) {
		return "<sup>" + in + "</sup>";
	}
	
	private static String superscript(int in) {
		String str = String.valueOf(in + 1);
	    str = str.replaceAll("0", "⁰");
	    str = str.replaceAll("1", "¹");
	    str = str.replaceAll("2", "²");
	    str = str.replaceAll("3", "³");
	    str = str.replaceAll("4", "⁴");
	    str = str.replaceAll("5", "⁵");
	    str = str.replaceAll("6", "⁶");
	    str = str.replaceAll("7", "⁷");
	    str = str.replaceAll("8", "⁸");
	    str = str.replaceAll("9", "⁹");         
	    return str;
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar txtListAuthors.jar authorlist.tsv");
		System.err.println("Convert list of authors to Authors and Affiliations");
		System.err.println("  authorlist.tsv    tab-separated table, with columns First Middle Last Affiliations");
		System.err.println("                    separate multiple affiliations with ;)");
		System.err.println("  output     list of authors, affiliations, affiliations-sorted.");
		System.err.println("             save the list of authors as .html to get the supersription correctly.");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ListAuthors().go(args[0]);
		} else {
			new ListAuthors().printHelp();
		}

	}

}
