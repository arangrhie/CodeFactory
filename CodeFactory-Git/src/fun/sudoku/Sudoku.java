package fun.sudoku;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class Sudoku extends Rwrapper {

	public static void main(String[] args) {
		if (args.length == 1) {
			new Sudoku().go(args[0]);
		} else {
			new Sudoku().printHelp();
		}
	}

	private int[] table = new int[81];	// Table of numbers, with each cell filled when solved
	private HashMap<Integer, ArrayList<Integer>> candidates = new HashMap<Integer, ArrayList<Integer>>();

	@Override
	public void hooker(FileReader fr) {
		
		int idx = 0;
		
		String line;
		String[] tokens;
		
		ArrayList<Integer> can;
		
		// Read the quiz
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			if (tokens.length != 9) {
				System.err.println("Input file is incorrect at line: " + line);
			}
			for (int i = 0; i < 9; i++) {
				table[idx] = Integer.parseInt(tokens[i]);
				if (table[idx] == 0) {
					// Create candidates with all numbers
					can = new ArrayList<Integer>();
					for (int j = 1; j <= 9; j++) {
						can.add(j);
					}
					candidates.put(idx, can);
				}
				idx++;
			}
		}
		
		System.out.println("Original table");
		printTable();
		//printCandidates();
		System.out.println();
		
		deterministic();
		
		int count_0 = 0;
		for (int i = 0; i < 81; i++) {
			if (table[i] == 0) count_0++;
		}
		
		if (count_0 == 0) {
			// All numbers determined. Done!
			printTable();
			System.exit(0);
		}
		
		// We have exhausted deterministic numbers.
		// Let's try all possible solutions, using min in each square

		// backup the table so we can start with a new number
		int[] bkTable = new int[81];
		for (int i = 0; i < 81; i++) bkTable[i] = table[i];
		
		HashMap<Integer, ArrayList<Integer>> bkCandidates = new HashMap<Integer, ArrayList<Integer>>();
		for (int key : candidates.keySet()) {
			ArrayList<Integer> candidate = new ArrayList<Integer>();
			for (int i = 0; i < candidates.get(key).size(); i++)	candidate.add(candidates.get(key).get(i));
			bkCandidates.put(key, candidate);
		}
		
		for (int square = 0; square < 9; square++) {
			idx = (square/3)*27 + (square%3)*3;
			int min=9;
			int ii=idx;
			int min_ii = idx;
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					ii = idx + i*9 + j;
					if (candidates.containsKey(ii)) {
						if (candidates.get(ii).size() < min) {
							min = candidates.get(ii).size();
							min_ii = ii;
						}
					}
				}
			}

			System.err.println("Trying " + candidates.get(min_ii).size() + " possible solutions from square " + square);
			ArrayList<Integer> paths = new ArrayList<Integer>();
			paths.addAll(candidates.get(min_ii));
			for (int p : paths) {

				System.err.println("== " + p + " at " + min_ii);


				// set p
				candidates.get(min_ii).clear();
				candidates.get(min_ii).add(p);

				// go!
				deterministic();

				count_0 = 0;
				for (int i = 0; i < 81; i++)	if (table[i] == 0) count_0++;
				if (count_0 == 0) {
					// All numbers determined. Done!
					System.out.println("Resolved!");
					printTable();
					System.out.println();
				}

				// reset
				for (int i = 0; i < 81; i++) table[i] = bkTable[i];
				candidates.clear();
				for (int key : bkCandidates.keySet()) {
					ArrayList<Integer> candidate = new ArrayList<Integer>();
					for (int i = 0; i < bkCandidates.get(key).size(); i++)	candidate.add(bkCandidates.get(key).get(i));
					candidates.put(key, candidate);
				}
			}
		}
	}
	
	private void deterministic() {
		boolean updated = true;
		printCandidates();
		while (updated) {
			//System.err.println("Check Col & Row ...");
			checkColRow();
			//printCandidates();

			//System.err.println("Check Square ...");
			checkSquare();
			//printCandidates();
			
			//System.err.println("Check unique candidates in each square ...");
			checkSquareCandidates();
			//printCandidates();
			
			//System.err.println("Check mutually exclusive numbers in each row / col ...");
			checkMutExclusive();
			//printCandidates();

			//System.err.println("Update table ...");
			updated = updateTable();
			//printCandidates();
			
			if (!validate()) {
				System.err.println("Dead end. Wrong path.");
				updated = false;
			}
			//System.err.println();
		}
		printCandidates();
	}
	
	private void printCandidates() {
		int num, idx;
		String str = "";
		
		System.err.println();
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				idx = i * 9 + j;
				num = table[idx];
				if (num > 0) {
					System.err.print(String.format("%16s", "[" + table[idx] + "]"));
				}
				else {
					for (int m : candidates.get(idx)) {
						str += m + ",";
					}
					System.err.print(String.format("%16s", str + ""));
					str = "";
				}
			}
			System.err.println();
		}
		System.err.println();
	}
	
	private boolean updateTable() {
		boolean updated = false;
		
		int idx;
		
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				idx = i * 9 + j;
				if (table[idx] == 0) {
					if (candidates.get(idx).size() == 1) {
						table[idx] = candidates.get(idx).get(0);
						candidates.remove(idx);
						updated = true;
					}
					
				}
			}
		}
		return updated;
	}
	
	private boolean validate() {
		boolean ok = true;
		
		for (int k = 0; k < 9; k++) {
			for (int i = 0; i < 9; i++) {
				for (int j = i + 1; j < 9; j++) {
					if (table[k*9 + i] > 0 && table[k*9 + i] == table[k*9 + j]) {
						System.err.println("Collesion at " + (k*9 + i) + "(" + table[k*9 + i] + ") and "
								+ (k*9 + j) + "(" + table[k*9 + j] + ")");
						ok = false;
					} else if (table[i*9 + k] > 0 && table[i*9 + k] == table[j*9 + k]) {
						System.err.println("Collesion at " + (i*9 + k) + "(" + table[i*9 + k] + ") and "
								+ (j*9 + k) + "(" + table[j*9 + k] + ")");
						ok = false;
					}
				}
			}
		}
		return ok;
	}

	private void printTable() {
		for (int i = 0; i < 9; i++) {
			if (i == 3 || i == 6) System.out.println("---------------------");
			for (int j = 0; j < 9; j++) {
				if (j == 3 || j == 6) System.out.print("| ");
				System.out.print(table[i * 9 + j] + " ");
			}
			System.out.println();
		}
	}
	
	private void checkColRow() {
		
		int num = 0;
		ArrayList<Integer> can;
		for (int idx = 0; idx < 81; idx++) {
			// this cell has a number
			if (table[idx] > 0)	continue;
			
			can = candidates.get(idx);
			
			//System.err.print("Col: ");
			for (int i = 0; i < 9; i++) {
				// check row
				num = table[(idx/9)*9 + i];
				//System.err.print((idx/9)*9 + i + "(" + num + ")");
				if (num > 0 && can.contains(num)) {
					can.remove(can.indexOf(num));
				}

				// check col
				num = table[idx%9 + i*9];
				//System.err.print(idx%9 + i*9 + "(" + num + ")");
				if (num > 0 && can.contains(num)) {
					can.remove(can.indexOf(num));
				}
			}
			//System.err.println();
		}
	}
	
	private void checkSquare() {
		
		int idx;
		int num;
		
		ArrayList<Integer> marked;
		ArrayList<Integer> can;
		
		for (int i = 0; i < 3; i ++) {
			for (int j = 0; j < 3; j++) {
				idx = i*27 + j*3;
				
				marked = new ArrayList<Integer>();
				
				// traverse within the square, collect 'marked' numbers
				for (int k = 0; k < 3; k++) {
					for (int l = 0; l < 3; l++) {
						num = table[idx + k*9 + l];
						if (num > 0) {
							marked.add(num);
						}
					}
				}
				// traverse again, remove 'marked' from candidates
				for (int k = 0; k < 3; k++) {
					for (int l = 0; l < 3; l++) {
						num = table[idx + k*9 + l];
						if (num == 0) {
							can = candidates.get(idx + k*9 + l);
							for (int m : marked) {
								if (can.contains(m)) can.remove(can.indexOf(m));
							}
						}
					}
				}
			}
		}
	}
	
	private void checkSquareCandidates() {
		int idx, num, ii;
		HashMap<Integer, ArrayList<Integer>> candidateNoDup = new HashMap<Integer, ArrayList<Integer>>();
		ArrayList<Integer> can;
		ArrayList<Integer> iiList = new ArrayList<Integer>();
		
		for (int i = 0; i < 3; i ++) {
			for (int j = 0; j < 3; j++) {
				idx = i*27 + j*3;
				
				// initialize
				iiList.clear();
				candidateNoDup.clear();
				
				// traverse again, this time look for candidates present only in 1 cell
				for (int k = 0; k < 3; k++) {
					for (int l = 0; l < 3; l++) {
						ii = idx + k*9 + l;
						num = table[ii];
						if (num == 0) {
							iiList.add(ii);
							can = new ArrayList<Integer>();
							can.addAll(candidates.get(ii));
							candidateNoDup.put(ii, can);
						}
					}
				}
				for (int idxII = 0; idxII < iiList.size(); idxII++) {
					ii = iiList.get(idxII);
					can = candidateNoDup.get(ii);
					for (int lii = 0; lii < iiList.size(); lii++) {
						// skip itself
						if (lii == idxII)	continue;
						
						// remove elements found in other candidates
						for (int m : candidates.get(iiList.get(lii))) {
							if (can.contains(m))	can.remove(can.indexOf(m));
						}
					}
					if (can.size() == 1) {
						candidates.put(ii, can);
					}
				}
			}
		}
	}
	
	private void checkMutExclusive() {
		
		int idx, ii;
		ArrayList<Integer> sharedR = new ArrayList<Integer>();
		ArrayList<Integer> sharedC = new ArrayList<Integer>();
		ArrayList<Integer> iiList = new ArrayList<Integer>();
		ArrayList<Integer> can;
		
		for (int i = 0; i < 3; i ++) {
			for (int j = 0; j < 3; j++) {
				// go to each square
				idx = i*27 + j*3;
				
				// initialize
				iiList.clear();
				sharedR.clear();
				sharedC.clear();
				
				// traverse, put candidate ii index
				for (int k = 0; k < 3; k++) {
					for (int l = 0; l < 3; l++) {
						ii = idx + k*9 + l;
						if (table[ii] == 0) iiList.add(ii);
					}
				}
				
				// Cst row
				for (int C = 0; C < 3; C++) {
					ROW : for (int rr = 0; rr < 2; rr++) {
						int irr = C*9 + idx;
						if (iiList.contains(irr + rr)) {
							can = candidates.get(irr + rr);
							for (int rf = rr + 1; rf < 3; rf++) {
								if (!iiList.contains(irr + rf))	continue;
								for (int m : candidates.get(irr + rf)) {
									if (can.contains(m) && !sharedR.contains(m))	sharedR.add(m);
								}
							}
							break ROW;
						}
					}
					if (sharedR.size() > 0) {
					// remove if found in the rest of the cells
						for (int rc = 0; rc < 3; rc++) {
							if (rc == C)	continue;
							for (int rr = 0; rr < 3; rr++) {
								int irc = rc*9 + idx + rr;
								if (iiList.contains(irc)) {
									for (int m : candidates.get(irc)) {
										if (sharedR.contains(m))	sharedR.remove(sharedR.indexOf(m));
									}
								}
							}
						}
					}
					if (sharedR.size() > 0) {
						// found R restricted ones!
						//System.err.print(idx + " Square row " + (C+1) + ": ");
						//for (int m : sharedR) {
							//System.err.print(m + ",");
						//}
						//System.err.println();
						// remove from the rest of the row
						for (int rr = 0; rr < 9; rr++) {
							// skip if this is the cells from sharedR
							if (rr >= idx % 9 && rr < idx % 9 + 3)	continue;
							int irr = (idx/9 + C) * 9 + rr;
							if (candidates.containsKey(irr)) {
								can = candidates.get(irr);
								for (int m : sharedR) {
									if (can.contains(m)) {
										can.remove(can.indexOf(m));
										//System.err.println("  Remove " + m + " from " + irr);
									}
								}
							}
						}
					}
					sharedR.clear();
				}
				
				// Rst col
				for (int R = 0; R < 3; R++) {
					COL : for (int cc = 0; cc < 2; cc++) {
						int icc = idx + R;
						if (iiList.contains(icc + cc*9)) {
							can = candidates.get(icc + cc*9);
							for (int cf = cc + 1; cf < 3; cf++) {
								if (!iiList.contains(icc + cf*9))	continue;
								for (int m : candidates.get(icc + cf*9)) {
									if (can.contains(m) && !sharedC.contains(m))	sharedC.add(m);
								}
							}
							break COL;
						}
					}
					if (sharedC.size() > 0) {
					// remove if found in the rest of the cells
						for (int cr = 0; cr < 3; cr++) {
							if (cr == R)	continue;
							for (int cc = 0; cc < 3; cc++) {
								int icc = idx + cr + 9*cc;
								if (iiList.contains(icc)) {
									for (int m : candidates.get(icc)) {
										if (sharedC.contains(m)) {
											sharedC.remove(sharedC.indexOf(m));
										}
									}
								}
							}
						}
					}
					if (sharedC.size() > 0) {
						// found R restricted ones!
						//System.err.print(idx + " Square col " + (R + 1) + ": ");
						//for (int m : sharedC) {
							//System.err.print(m + ",");
						//}
						//System.err.println();
						// remove from the rest of the col
						for (int cc = 0; cc < 9; cc++) {
							// skip if this is the cells from sharedR
							if (cc >= (idx / 9) && cc < (idx / 9) + 3)	continue;
							int icc = idx%9 + R + 9*cc;
							if (candidates.containsKey(icc)) {
								can = candidates.get(icc);
								for (int m : sharedC) {
									if (can.contains(m)) {
										can.remove(can.indexOf(m));
										//System.err.println("  Remove " + m + " from " +icc);
									}
								}
							}
						}
					}
					sharedC.clear();
				}
			}
		}
	}
	
	
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar Sudoku.jar in.txt");
		System.out.println("\tin.txt: e.g. 1 0 0 2 3 0 5 0 7");
		System.out.println("\tstdout: Answer printed, or give up");
	}
	
}
