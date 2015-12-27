package javax.arang.sam;

import java.util.Vector;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class RemoveRedundancy extends IOwrapper {

	private static int N = 5;

	
	int QUAL_OFFSET = 33;
	
	// read line
	Vector<String> readLines = new Vector<String>();
	Vector<Integer> qualSum = new Vector<Integer>();
	Vector<Integer> rightEnds = new Vector<Integer>();
	int readsAfter = 0;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		int currentLeft = 0;
		
		String read1tokens[];
		String read2tokens[];
		fm.writeLine("@ Redundancy Removed");
		String prevKey = "";
		int readsBefore = 0;
		int numUnpaired = 0;
		String read1 = "";
		try {
			while (fr.hasMoreLines()) {
				read1 = fr.readLine();
				if (read1.startsWith("@")) {
					fm.writeLine(read1);
					continue;
				}
				if (read1.length() < 5) 	continue;
				if (read1.equals(""))	continue;
				read1tokens = read1.split("\t");
				String key = read1tokens[Sam.QNAME];
				String read2 = fr.readLine();
				read2tokens = read2.split("\t");
				if (!key.equals(read2tokens[Sam.QNAME])) {
					System.out.println("Error: Is not a paired-end, unique matched sequence data");
					break;
				}
				if (!key.equals(prevKey)) {
					prevKey = key;
					readsBefore++;
					if (read1tokens[Sam.CIGAR].equals("*") || read2tokens[Sam.CIGAR].equals("*")) {
						numUnpaired++;
					} else {
						int pos = Integer.parseInt(read1tokens[Sam.POS]) - Sam.getStartSoftclip(read1tokens[Sam.CIGAR]);
						if (currentLeft < pos) {
							currentLeft = pos;
							// new left end added. add the reads on fm.
							writeLines(fm);
						}
						
						String reads = read1 + "\n" + read2;
						String qual = read1tokens[Sam.QUAL] + read2tokens[Sam.QUAL];
						int qSum = 0;
						for (int i = 0; i < qual.length(); i++) {
							qSum += (qual.charAt(i) - QUAL_OFFSET);
						}
						int rightEnd = Integer.parseInt(read2tokens[Sam.POS])
									+ Sam.getMatchedBasesLen(read2tokens[Sam.CIGAR])
									+ Sam.getDeletedBasesLen(read2tokens[Sam.CIGAR])
									+ Sam.getEndSoftclip(read2tokens[Sam.CIGAR]);
						
						if (readLines.size() == 0) {
							readLines.add(reads);
							qualSum.add(qSum);
							rightEnds.add(rightEnd);
							continue;
						} else {
							// sort by quality
							int count = 0;
							boolean isAdded = false;
							int readSize = readLines.size();
							SORT_LOOP : for (int i = 0; i < readSize; i++) {
								if (rightEnds.get(i) < rightEnd) {
									continue SORT_LOOP;
								}
								if (rightEnds.get(i) > rightEnd) {
									if (!isAdded) {
										readLines.add(i, reads);
										qualSum.add(i, qSum);
										rightEnds.add(i, rightEnd);
										isAdded = true;
										readSize++;
										i++;
									}
									break SORT_LOOP;
								}
								if (rightEnds.get(i) == rightEnd) {
									count++;
									if (!isAdded && count <= N) {
										if (qualSum.get(i) < qSum) {
											readLines.add(i, reads);
											qualSum.add(i, qSum);
											rightEnds.add(i, rightEnd);
											readSize++;
											i++;
											isAdded = true;
										}
									} else if (count > N) {
										int leftOverSize = readLines.size() - i;
										for (int j = 0; j < leftOverSize; ) {
											if (rightEnds.get(i + j) == rightEnd) {
												readLines.remove(i + j);
												qualSum.remove(i + j);
												rightEnds.remove(i + j);
												leftOverSize--;
											} else {
												j++;
											}
										}
										break SORT_LOOP;
									}
								}
							}
							if (!isAdded && count < N) {
								readLines.add(reads);
								qualSum.add(qSum);
								rightEnds.add(rightEnd);
							}
						}
						
					}
				}
			}
			writeLines(fm);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println(read1);
			throw e;
		}
		
		System.out.println("Reads before redundancy filtering\t" + readsBefore);
		System.out.println("Reads after redundancy filtering\t" + readsAfter);
		System.out.println("Unpaired reads\t" + numUnpaired);

	}
	
	private void writeLines(FileMaker fm) {
		for (int i = 0; i < readLines.size(); i++ ) {
			fm.writeLine(readLines.get(i)); 
			readsAfter++;
		}
		readLines.clear();
		qualSum.clear();
		rightEnds.clear();
	}

	@Override
	public void printHelp() {
		System.out.println("usage: java -jar samRemoveRedundancy.jar <in.sorted.sam> [N]");
		System.out.println("\t[N]: [OPTION] DEFAULT=5. More than N redundant reads will be removed.");
		System.out.println("\tOutput: <in_red_N_rm.sam>");
		System.out.println("\tInput file must be sorted, paired-end sequenced sam.");
		System.out.println("Redundant reads, aligned on same position, " +
				"with a sequencing depth > N will be removed.\n" +
				"Reads with \tthe best alignment scores will left.");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new RemoveRedundancy().go(args[0], args[0].replace(".sam", "_red_" + N + "_rm.sam"));
		} else if (args.length == 2) {
			N = Integer.parseInt(args[1]);
			new RemoveRedundancy().go(args[0], args[0].replace(".sam", "_red_" + N + "_rm.sam"));
		} else {
			new RemoveRedundancy().printHelp();
		}
	}

}
