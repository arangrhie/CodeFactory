package javax.arang.ref.util;

import java.util.HashMap;
import java.util.StringTokenizer;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class CountHomopolymers extends IOwrapper {
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String chr = "";
		
		HashMap<Integer, Integer> homoTable = new HashMap<Integer, Integer>();
		int pointer = 1;
		char leftSeq;
		char rightSeq = '0';
		boolean isNewRef = false;
		int max = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine().trim();
			if (isNewRef) {
				rightSeq = line.charAt(0);
				isNewRef = false;
			}
			pointer = 1;
			if (line.startsWith(">")) {
				StringTokenizer st = new StringTokenizer(line);
				chr = st.nextToken();
				if (homoTable.size() > 0) {
					writeHomoTable(fm, homoTable, max);
				}
				fm.writeLine(chr);
				// initialize other vars
				homoTable.clear();
				isNewRef = true;
				max = 0;
				continue;
			}
			SEQ_COMP_LOOP: while (pointer < line.length()) {
				leftSeq = rightSeq;
				rightSeq = line.charAt(pointer);
				int len = 1;
				while (leftSeq == rightSeq) {
					len++;
					pointer++;
					if (pointer >= line.length() && fr.hasMoreLines()) {
						line = fr.readLine();
						pointer = 0;
						if (line.startsWith(">")) {
							if (len > max) {
								max = len;
							}
							if (len > 1) {
								if (homoTable.containsKey(len)) {
									homoTable.put(len, homoTable.get(len) + 1);
								} else {
									homoTable.put(len, 1);
								}
							}
							StringTokenizer st = new StringTokenizer(line);
							chr = st.nextToken();
							if (homoTable.size() > 0) {
								writeHomoTable(fm, homoTable, max);
							}
							fm.writeLine(chr);
							// initialize other vars
							homoTable.clear();
							isNewRef = true;
							max = 0;
							break SEQ_COMP_LOOP;
						}
					}
					rightSeq = line.charAt(pointer);
				}
				if (len > max) {
					max = len;
				}
				if (len > 1) {
					if (homoTable.containsKey(len)) {
						homoTable.put(len, homoTable.get(len) + 1);
					} else {
						homoTable.put(len, 1);
					}
				}
				pointer++;
			}
		}
		
		writeHomoTable(fm, homoTable, max);
		
	}
	
	private void writeHomoTable(FileMaker fm, HashMap<Integer, Integer> homoTable, int max) {
		int length = 0;
		int lenth7 = 0;
		for (int i = 2; i <= max; i++) {
			if (homoTable.containsKey(i)) {
				fm.writeLine(i + "\t" + homoTable.get(i));
				length += (i * homoTable.get(i));
				if (i > 6) {
					lenth7 += (i * homoTable.get(i));
				}
			} else {
				fm.writeLine(i + "\t" + 0);
			}
		}
		fm.writeLine("homopolymer region length in total: " + length);
		fm.writeLine("homopolymer region length in total (>7bp): " + lenth7);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar countHomopolymers.jar <ref.fa> <outFile>");
		System.out.println("\t<ref.fa>: contains the reference fasta file");
		System.out.println("\t<outFile>: homopolymer analysis per chromosomes(contigs)");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new CountHomopolymers().go(args[0], args[1]);
		} else {
			new CountHomopolymers().printHelp();
		}
	}

}
