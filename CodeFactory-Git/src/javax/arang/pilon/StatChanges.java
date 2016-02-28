package javax.arang.pilon;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class StatChanges extends IOwrapper {

//	private static final short CONTIG_BEFORE = 0;
//	private static final short CONTIG_AFTER = 1;
	private static final short CHANGE_BEFORE = 2;
	private static final short CHANGE_AFTER = 3;
	
	private static final short TYPE_INS = 0;
	private static final short TYPE_DEL = 1;
	private static final short TYPE_SUB = 2;
	
	private static final short BP_LEN_1 = 0;
	private static final short BP_LEN_2 = 1;
	private static final short BP_LEN_MORE_2 = 2;
	
	private static final short BASE_A = 0;
	private static final short BASE_T = 1;
	private static final short BASE_G = 2;
	private static final short BASE_C = 3;
	private static final short BASE_N = 4;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		int[][] typeLenArr = new int[3][3];
		// row = 0: ins 1: del 2: substitution
		// col = 0: 1bp 1: 2bp 2: >2bp
		
		// Arrays for 1bp a/t/g/c counts
		int[][] baseCntArr = new int[3][5];
		// row = 0: ins 1: del 2: substitution
		// col = 0: A, 1: T, 2: G, 3: C, 4: N
		
		typeLenArr = initArray(typeLenArr, 3, 3);
		baseCntArr = initArray(baseCntArr, 3, 5);
		
		int type;
		int bpLen;
		int base;
		
		String changed = "";
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			
			type = getType(tokens[CHANGE_BEFORE], tokens[CHANGE_AFTER]);
			changed = getChanged(tokens[CHANGE_BEFORE], tokens[CHANGE_AFTER]);
			bpLen = getBpLen(changed);
			typeLenArr[type][bpLen]++;
			if (bpLen == BP_LEN_1) {
				base = getBase(changed);
				baseCntArr[type][base]++;
			}
		}
		
		fm.writeLine("Type\t1bp\t2bp\t>2bp\tA\tT\tG\tC\tN");
		fm.write("Insertion");
		for (int i = 0; i < 3; i++) {
			fm.write("\t" + typeLenArr[TYPE_INS][i]);
		}
		for (int i = 0; i < 5; i++) {
			fm.write("\t" + baseCntArr[TYPE_INS][i]);
		}
		fm.writeLine();
		fm.write("Deletion");
		for (int i = 0; i < 3; i++) {
			fm.write("\t" + typeLenArr[TYPE_DEL][i]);
		}
		for (int i = 0; i < 5; i++) {
			fm.write("\t" + baseCntArr[TYPE_DEL][i]);
		}
		fm.writeLine();
		fm.write("Substitution");
		for (int i = 0; i < 3; i++) {
			fm.write("\t" + typeLenArr[TYPE_SUB][i]);
		}
		for (int i = 0; i < 5; i++) {
			fm.write("\t" + baseCntArr[TYPE_SUB][i]);
		}
		fm.writeLine();
	}
	
	private int[][] initArray(int[][] array, int numRow, int numCol) {
		for (int i = 0; i < numRow; i++) {
			for (int j = 0; j < numCol; j++) {
				array[i][j] = 0;
			}
		}
		return array;
	}

	private int getBase(String changed) {
		if (changed.equalsIgnoreCase("A")) {
			return BASE_A;
		} else if (changed.equalsIgnoreCase("T")) {
			return BASE_T;
		} else if (changed.equalsIgnoreCase("G")) {
			return BASE_G;
		} else if (changed.equalsIgnoreCase("C")) {
			return BASE_C;
		}
		System.out.println("[DEBUG] :: Not A/T/G/C base: " + changed);
		return BASE_N;
	}

	private int getBpLen(String changed) {
		if (changed.length() == 1) {
			return BP_LEN_1;
		} // 2bp ins
		else if (changed.length() == 2) {
			return BP_LEN_2;
		} // >2 bp ins
		else {
			return BP_LEN_MORE_2;
		}
	}

	private short getType(String changedBefore, String changedAfter) {
		// insertion
		if (changedBefore.equals(".")) {
			return TYPE_INS;
		}
		// deletion
		else if (changedAfter.equals(".")) {
			return TYPE_DEL;
		}
		// substitution
		else {
			return TYPE_SUB;
		}
	}
	
	private String getChanged(String changedBefore, String changedAfter) {
		// insertion
		if (changedBefore.equals(".")) {
			return changedAfter;
		}
		// deletion
		else if (changedAfter.equals(".")) {
			return changedBefore;
		}
		// substitution
		else {
			return ((changedBefore.length() > changedAfter.length()) ? changedBefore : changedAfter);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar pilonStatChanges.jar <in.fasta.bases.changes> <out.stat.txt>");
		System.out.println("\t<in.fasta.bases.changes>: output file of pilon with --fix base --changes option");
		System.out.println("\t<out.stat.txt>: Text file containing simple stats");
		System.out.println("\t\t\t1bp\t2bp\t>2bp\t1BP A\tT\tG\tC\tN");
		System.out.println("\tIns");
		System.out.println("\tDel");
		System.out.println("\tSubstitution");
		System.out.println("Arang Rhie, 2015-11-07. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new StatChanges().go(args[0], args[1]);
		} else {
			new StatChanges().printHelp();
		}
	}

}
