package javax.arang.sam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class SortByFlag extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {

		String line;
		String[] tokens;
		
		String rid = "";
		Short  flag;
		
		HashMap<Short, String> flagToSam = new HashMap<Short, String>();
		ArrayList<Short> flags = new ArrayList<Short>();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			
			tokens = line.split(RegExp.WHITESPACE);
			if (!rid.equals("") && !rid.equals(tokens[Sam.QNAME])) {
				//  this is NOT the first read
				//  and is a new read.
				//
				//  output what we had in the flagToSam map.
				
				Collections.sort(flags);
				for (int i = 0; i < flags.size(); i++) {
					System.out.println(flagToSam.get(flags.get(i)));
				}
				
				//  initialize
				flagToSam.clear();
				flags.clear();
			}
			rid = tokens[Sam.QNAME];
			flag = Short.parseShort(tokens[Sam.FLAG]);

			if (flags.contains(flag)) {
				//  Check if the flag exists, if yes append the line
				flagToSam.put(flag, flagToSam.get(flag) + "\n" + line);
			} else {
				//  If no add the new flag and line
				flags.add(flag);
				flagToSam.put(flag, line);
			}
		}
		
		if (!rid.equals("")) {
			//  this is NOT the first read
			//  and is a new read.
			//
			//  output what we had in the flagToSam map.
			
			Collections.sort(flags);
			for (int i = 0; i < flags.size(); i++) {
				System.out.println(flagToSam.get(flags.get(i)));
			}
		}
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar samSortByFlag.jar <in.sam>");
		System.err.println("  in.sam  input sam file, sorted by read id. - for stdin");
		System.err.println("  output  sam file, sorted by flag");
		System.err.println("Arang Rhie, 2021-09-28. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new SortByFlag().go(args[0]);
		} else {
			new SortByFlag().printHelp();
		}
	}

}
