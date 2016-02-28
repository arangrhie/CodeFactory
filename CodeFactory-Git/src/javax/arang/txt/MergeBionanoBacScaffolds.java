package javax.arang.txt;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class MergeBionanoBacScaffolds extends IOwrapper {

	//private static final short CONTIG = 0;
	private static final short SCAFFOLD_1 = 1;
	private static final short TOTAL_LEN = 2;
	private static final short SCAFFOLD_2 = 3;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		HashMap<String, ArrayList<String>> scaffoldToBionanos = new HashMap<String, ArrayList<String>>();
		HashMap<String, String> contigToBionano = new HashMap<String, String>();
		HashMap<String, String> contigToScaffold = new HashMap<String, String>();
		HashMap<String, ArrayList<String>> bionanoToContigs = new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<String>> bacToBionano = new HashMap<String, ArrayList<String>>();
		
		
		String prevScaffold1 = ""; 
		String prevScaffold2 = "";
		boolean isFirst = true;
		String scaffold1;
		String scaffold2;
		int scaffoldId = 1;
		int len;
		int lenSum = 0;
		String scaffold1s = "";
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			scaffold1 = tokens[SCAFFOLD_1];
			scaffold2 = tokens[SCAFFOLD_2];
			len = Integer.parseInt(tokens[TOTAL_LEN]);
			if (scaffold2.equals("NA")) continue;
			if (!prevScaffold2.equals(scaffold2)) {
				if (isFirst) {
					isFirst = false;
				} else {
					fm.writeLine("Bionano-Bac-" + scaffoldId + "\t" + lenSum + "\t" + scaffold1s);
					scaffoldId++;
				}
				lenSum = len;
				scaffold1s = tokens[SCAFFOLD_1];
			} else {
				// bac id is identical
				if (!prevScaffold1.equals(scaffold1)) {
					lenSum += len;
					scaffold1s += "," + tokens[SCAFFOLD_1];
				}
			}
			
			prevScaffold1 = scaffold1;
			prevScaffold2 = scaffold2;
		}
		fm.writeLine("Bionano-Bac-" + scaffoldId + "\t" + lenSum + "\t" + scaffold1s);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: txtMergeBionanoBacScaffolds.jar <in.scaffold.sort> <out.scaffold>");
		System.out.println("\t<in.scaffold.sort>: CONTIG\tSCAFFOLD1\tLEN\tSCAFFOLD2");
		System.out.println("\t<out.scaffold>: NEW_SCAFFOLD\tLEN\tMERGED_SCAFFOLD1s");
		System.out.println("Arang Rhie, 2015-09-25. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new MergeBionanoBacScaffolds().go(args[0], args[1]);
		} else {
			new MergeBionanoBacScaffolds().printHelp();
		}
	}

}
