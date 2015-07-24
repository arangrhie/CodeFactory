package javax.arang.txt.tab;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class FilterByColumn extends IOwrapper {

	private static String COL_NAME = "";
	private static String op;
	private static String opr;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line = fr.readLine();
		String[] tokens = line.split("\t");
		
		// write header
		fm.writeLine(line);
		
		// find COL_NAME
		int col_idx = -1;
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals(COL_NAME)) {
				col_idx = i;
			}
		}
		
		if (col_idx < 0) {
			System.out.println("No column found named \'" + COL_NAME + "\'");
			System.exit(-9);
		}
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			if (isFiltered(tokens[col_idx], op, opr))	continue;
			fm.writeLine(line);
		}
	}
	
	private static boolean isFiltered(String opr1, String op, String opr2) {
		if (op.equals("==")) {
			if (opr1.equals(opr2))	return false;
		} else if (op.equals("!=")) {
			if (!opr1.equals(opr2))	return false;
		} else {
			if (opr1.equals("NA") || opr1.equals(".") || opr1.equals("")) {
				return false;
			}
			if (op.equals("<")) {
				return !(Double.parseDouble(opr1) < Double.parseDouble(opr2)); 
			} else if (op.equals(">")) {
				return !(Double.parseDouble(opr1) > Double.parseDouble(opr2)); 
			} else if (op.equals("<=")) {
				return !(Double.parseDouble(opr1) <= Double.parseDouble(opr2)); 
			} else if (op.equals(">=")) {
				return !(Double.parseDouble(opr1) >= Double.parseDouble(opr2)); 
			}
		}
		return true;
	}

	@Override
	public void printHelp() {
		System.out.println("usage: java -jar tabFilterByColumn.jar <in.txt> <COL_NAME> <operator> <operand>");
		System.out.println("\t<COL_NAME>: column to apply filter condition");
		System.out.println("\t<operator>: ==, !=, <, >, <=, >=");
		System.out.println("\t<operand>: number or string.");
		System.out.println("\t\tNA will be counted as missing values, and will not be applied to numeric operands.");
		System.out.println("2014.11.20, Arang Rhie. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 4) {
			COL_NAME = args[1];
			op = args[2];
			opr = args[3];
			new FilterByColumn().go(args[0], args[0].replace(".txt", "_" + args[1] + args[2] + args[3] + ".txt"));
		} else {
			new FilterByColumn().printHelp();
		}
	}

}
