package javax.arang.txt;

import java.util.HashMap;
import java.util.Vector;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class Vlookup extends I2Owrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		HashMap<String, String> keyMap = new HashMap<String, String>();
		
		String[] tokens;
		tokens = fr2.readLine().split("\t");

		int[] in2ColIdxs = new int[colNamesToAdd.size()];
		int in2KeyIdx = 0;
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals(in2LookupTableColName)) {
				in2KeyIdx = i;
			}
			if (colNamesToAdd.contains(tokens[i])) {
				in2ColIdxs[colNamesToAdd.indexOf(tokens[i])] = i;
			}
		}
		System.out.println("Key in " + fr2.getFileName() + " given: " + in2LookupTableColName + " found: " + tokens[in2KeyIdx] + " (" +in2KeyIdx + ")");
		System.out.print("Columns to add from " + fr2.getFileName() + " : ");
		for (int i = 0; i < in2ColIdxs.length; i++) {
			System.out.print(" " + colNamesToAdd.get(i) + " (" + in2ColIdxs[i] + ")");
		}
		System.out.println();
		
		String line;
		StringBuffer columnVals = new StringBuffer();
		while (fr2.hasMoreLines()) {
			line = fr2.readLine();
			tokens = line.split("\t");
			if (line.length() < 4) {
				System.out.println(line);
			} else {
				columnVals = new StringBuffer(tokens[in2ColIdxs[0]]);
				if (colNamesToAdd.size() > 1) {
					for (int i = 1; i < colNamesToAdd.size(); i++) {
						columnVals.append("\t" + tokens[in2ColIdxs[i]]);
					}
				}
				keyMap.put(tokens[in2KeyIdx], columnVals.toString());
			}
		}
		
		line = fr1.readLine();
		tokens = line.split("\t");
		int in1KeyIdx = 0;
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals(in1ColNameToLookup)) {
				in1KeyIdx = i;
				break;
			}
		}
		System.out.println("Key in " + fr1.getFileName() + " given: " + in1ColNameToLookup + " found: " + tokens[in1KeyIdx] + " (" + in1KeyIdx + ")");
		
		columnVals = new StringBuffer(colNamesToAdd.get(0));
		if (colNamesToAdd.size() > 1) {
			for (int i = 1; i < colNamesToAdd.size(); i++) {
				columnVals.append("\t" + colNamesToAdd.get(i));
			}
		}
		fm.writeLine(line+ "\t" + columnVals.toString());
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			tokens = line.split("\t");
			if (tokens.length < in1KeyIdx) {
				fm.writeLine(line + "\t");
				continue;
			}
			if (keyMap.containsKey(tokens[in1KeyIdx])) {
				fm.writeLine(line + "\t" + keyMap.get(tokens[in1KeyIdx]));
			} else {
				fm.write(line + "\t" + "NA");
				if (colNamesToAdd.size() > 1) {
					for (int i = 1; i < colNamesToAdd.size(); i++) {
						fm.write("\t" + "NA");
					}
				}
				fm.writeLine();
			}
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtVlookup.jar <in1.txt> <in2.txt> <out.txt> <in1_col_to_lookup> <in2_lookup_table_column> <column_name_to_add> [column_names_to_add]");
		System.out.println("\tSame method as \"VLOOKUP\" in Excel.");
		System.out.println("\tThis code stores <in2.txt> into a HashMap. Try to minimize the size of <in2.txt>");
		System.out.println("\t*Make sure you have unique column names in the first line!!*");
		System.out.println("\t<in1_col_to_lookup>: key to compare in in2.txt");
		System.out.println("\t<in2_lookup_table_column>: if key exist in column named <in2_lookup_table_column>, adds <column_name_to_add>. If not, \'NA\' will be appened");
		System.out.println("\t<column_name_to_add>: Column name containing values to add into in1.txt");
		System.out.println("Arang Rhie, 2014-01-21. arrhie@gmail.com");
	}

	private static String in1ColNameToLookup = "";
	private static String in2LookupTableColName = "";
	private static Vector<String> colNamesToAdd = null;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length >= 6) {
			in1ColNameToLookup = args[3];
			in2LookupTableColName = args[4];
			colNamesToAdd = new Vector<String>();
			for (int i = 5; i < args.length; i++) {
				colNamesToAdd.add(args[i]);
			}
			new Vlookup().go(args[0], args[1], args[2]);
		} else {
			new Vlookup().printHelp();
		}
	}

}
