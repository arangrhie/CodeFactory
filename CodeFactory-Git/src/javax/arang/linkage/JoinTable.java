package javax.arang.linkage;

import java.util.HashMap;
import java.util.Vector;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class JoinTable extends I2Owrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		// get header column idx
		String[] tokens = fr2.readLine().split("\t");
		Vector<Integer> colIdx = new Vector<Integer>();
		int SNPidx = 0;
		System.out.println("Insert colum ");
		for (int i = 0; i < tokens.length; i++) {
			if (colNames.contains(tokens[i])) {
				colIdx.add(i);
				System.out.println("\t" + tokens[i] + " " + i);
			}
			if (tokens[i].equals("SNP")) {
				SNPidx = i;
			}
		}
		
		// hold fr2 snps in HashTable
		HashMap<String, String> annoTable = new HashMap<String, String>();
		
		while (fr2.hasMoreLines()) {
			tokens = fr2.readLine().split("\t");
			String tableColumns = "";
			for (int i = 0; i < colIdx.size(); i++) {
				tableColumns = tableColumns + "\t" + tokens[colIdx.get(i)];
			}
			annoTable.put(tokens[SNPidx], tableColumns);
		}
		
		String line = fr1.readLine().trim();
		for (int i = 0; i < colIdx.size(); i++) {
			line = line + "\t" + colNames.get(i);
		}
		tokens = line.split("\t");
		int IDidx = 0;
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals("SNP")) {
				IDidx = i;
				break;
			}
		}
		
		fm.writeLine(line);
		
		while (fr1.hasMoreLines()) {
			line = fr1.readLine().trim();
			tokens = line.split("\t");
			fm.writeLine(line + annoTable.get(tokens[IDidx]));
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar linkageJoinTable.jar <plink.assoc> <plink.assoc.adjusted> <out.file> [col_header_name_to_add]");
		System.out.println("\tAdd columns with name [col_header_name_to_add] from <plink.assoc.adjusted> into <out.file>");
	}

	static Vector<String> colNames;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 3) {
			colNames = new Vector<String>();
			
			for (int i = 3; i < args.length; i++) {
				colNames.add(args[i]);
				System.out.print(args[i] + " ");
			}
			System.out.println();
			new JoinTable().go(args[0], args[1], args[2]);
		} else {
			new JoinTable().printHelp();
		}
	}

}
