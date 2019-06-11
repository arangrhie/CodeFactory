package javax.arang.fastq;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class SplitByRE extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		short lineCounter = 0;
		String name = "";
		int cutNum = 0;
		int leftIdx;
		int rightIdx;
		int i;
		int j;
		int k;
		int l;
		int reLeftLen = reLeft.length();
		int reRightLen = reRight.length();
		
		int cutLeftIdx = 0;		// assign i if reLeft found
		int cutRightIdx = 0;	// assign j if reRight found
		
		while (fr.hasMoreLines()) {
			lineCounter++;
			line = fr.readLine();
			if (lineCounter == 4) {
				lineCounter = 0;
			} else if (lineCounter == 1) {
				name = line.split(RegExp.WHITESPACE)[0].substring(1);
			} else if (lineCounter == 2) {
				leftIdx = 0;
				rightIdx = line.length();
				cutNum = 0;
				// Look for cut sites on each bases
				for (i = 0; i < line.length() - reLeftLen; i++) {
					RE_LEFT_LOOP: for ( j = i; j < i + reLeftLen; j++) {
						k = j - i;
						if (line.charAt(j) == reLeft.charAt(k)) {
							// System.err.println("[ DEBUG ] :: line.charAt(j) = " + line.charAt(j) + " " + j);
							// System.err.println("[ DEBUG ] :: reLeft.charAt(k) = " + reLeft.charAt(k) + " " + k);
							// if matching the reLeft base at site k
							if (k == reLeftLen - 1) {
								// reLeft match found at i. Let's find if j is matching the reRight.
								// System.err.println("[ DEBUG ] :: reLeft match found at i. Let's find if j is matching the reRight.");
								j++;	// proceed to the next base
								cutLeftIdx = j;
								PADD_LOOP : for (int numPadds = 0; numPadds <= numNs; numPadds++) {
									j += numPadds;
									RE_RIGHT_LOOP : for (l = j; l < j + reRightLen && l < rightIdx; l++ ) {
										k = l - j;
										// System.err.println("[ DEBUG ] :: line.charAt(l) = " + line.charAt(l) + " " + l);
										// System.err.println("[ DEBUG ] :: reRight.charAt(k) = " + reRight.charAt(k) + " " + k);
										if (line.charAt(l) == reRight.charAt(k)) {
											// if matching the reRight base at site k
											if (k == reRightLen - 1) {
												// Bingo. Let's cut.
												cutRightIdx = j;
												cutNum++;
												System.out.println(">" + name + "_" + cutNum);
												System.out.println(line.substring(leftIdx, cutLeftIdx));
												leftIdx = cutRightIdx;
												break PADD_LOOP;
											}
										} else {
											break RE_RIGHT_LOOP;
										}
									}
								}
							}
						} else {
							// Continue reading the line(i)th char
							break RE_LEFT_LOOP;
						}
					}
				}
				cutNum++;
				System.out.println(">" + name + "_" + cutNum);
				System.out.println(line.substring(leftIdx, rightIdx));
			}
			
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastqSplitByRE.jar <in.fastq> <re_site>");
		System.out.println("\t<in.fastq>: regular fastq file");
		System.out.println("\t<re_site>: Hi-C dna cut sites. Bases to split around.");
		System.out.println("\t\tex. GA/TC for GA / TC");
		System.out.println("\t\tor  GA/N/TC for GA /N/ TC");
		System.out.println("\t<stdout>: fasta file with sequences split by re_sites.");
		System.out.println("Arang Rhie, 2019-09-20. arrhie@gmail.com");
	}

	private static String re_site;
	private static String reLeft;
	private static String reRight;
	private static int numNs = 0;
	private static String[] tokens;
	
	public static void main(String[] args) {
		if (args.length == 2) {
			re_site = args[1];
			re_site = re_site.toUpperCase();
			tokens = re_site.split(RegExp.SLASH);
			reLeft = tokens[0];
			reRight = tokens[tokens.length - 1];
			if (re_site.contains("N")) {
				numNs = tokens[1].length();
				System.err.println("[ DEBUG ] :: numNs : " + numNs);
			}
			System.err.println("[ DEBUG ] :: reLeft : " + reLeft);
			System.err.println("[ DEBUG ] :: reRight : " + reRight);
			new SplitByRE().go(args[0]);
		} else {
			new SplitByRE().printHelp();
		}
	}

}
