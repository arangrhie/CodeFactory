package javax.arang.ref.util;

import java.util.HashMap;
import java.util.StringTokenizer;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class CountHomopolymerLength extends IOwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar refCountHomopolymerLength.jar <ref.fa>");
		System.out.println("\t<ref.fa>: contains the reference fasta file");
		System.out.println("\t<ref.fa>.[BASE]report: contents length analysis per chromosomes will be reported.");

	}
	
	static char base = 'A';

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			CountHomopolymerLength countObj = new CountHomopolymerLength();
			base = 'A';
			countObj.go(args[0], args[0] + "." + base + "report");
			base = 'T';
			countObj.go(args[0], args[0] + "." + base + "report");
			base = 'G';
			countObj.go(args[0], args[0] + "." + base + "report");
			base = 'C';
			countObj.go(args[0], args[0] + "." + base + "report");
		} else {
			new CountHomopolymerLength().printHelp();
		}

	}

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		fr.reset();
		String line;
		String chr = "";
		
		HashMap<Integer, Integer> gcTable = new HashMap<Integer, Integer>();
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
				if (gcTable.size() > 0) {
					writeGCTable(fm, gcTable, max);
				}
				fm.writeLine(chr);
				// initialize other vars
				gcTable.clear();
				isNewRef = true;
				max = 0;
				continue;
			}
			SEQ_COMP_LOOP: while (pointer < line.length()) {
				leftSeq = rightSeq;
				rightSeq = line.charAt(pointer);
				int len = 1;
				while ((leftSeq == base && rightSeq == base)) {
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
								if (gcTable.containsKey(len)) {
									gcTable.put(len, gcTable.get(len) + 1);
								} else {
									gcTable.put(len, 1);
								}
							}
							StringTokenizer st = new StringTokenizer(line);
							chr = st.nextToken();
							if (gcTable.size() > 0) {
								writeGCTable(fm, gcTable, max);
							}
							fm.writeLine(chr);
							// initialize other vars
							gcTable.clear();
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
					if (gcTable.containsKey(len)) {
						gcTable.put(len, gcTable.get(len) + 1);
					} else {
						gcTable.put(len, 1);
					}
				}
				pointer++;
			}
		}
		
		writeGCTable(fm, gcTable, max);
		
	}
	
	private void writeGCTable(FileMaker fm, HashMap<Integer, Integer> gcTable, int max) {
		int length = 0;
		int lenth7 = 0;
		for (int i = 2; i <= max; i++) {
			if (gcTable.containsKey(i)) {
				fm.writeLine(i + "\t" + gcTable.get(i));
				length += (i * gcTable.get(i));
				if (i > 6) {
					lenth7 += (i * gcTable.get(i));
				}
			} else {
				fm.writeLine(i + "\t" + 0);
			}
		}
		fm.writeLine(base + " region length in total:\t" + length);
		fm.writeLine(base + " region length in total (>7bp):\t" + lenth7);
	}
}
