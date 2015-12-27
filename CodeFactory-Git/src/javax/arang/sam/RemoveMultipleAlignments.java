package javax.arang.sam;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class RemoveMultipleAlignments extends IOwrapper {
	static int CODE = 1;
	int uniqueReadNum = 0;
	int unpairedReadNum = 0;
	int notAlignedNum = 0;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		int multipleReadNum = 0;
		int mmReadNum = 0;
		int totalReadNum = 0;
		
		int mmScore = 0;
		boolean discard = false;
		String prevQname = "";
		String[] candidate = new String[2];
		candidate[0] = ""; 
		candidate[1] = "";
		
		String line;
		String[] tokens;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@"))	 {
				fm.writeLine(line);
				continue;
			}
			if (line.equals(""))	continue;
			tokens = line.split("\t");
			
			if (prevQname.equals(tokens[Sam.QNAME])) {
				if (discard) {
					continue;
				}
				
				int flag = Integer.parseInt(tokens[Sam.FLAG]);
				if (SAMUtil.isSecondaryAlignment(flag)) {
					if (CODE == 2) {
						continue;
					}
					multipleReadNum++;
					discard = true;
					candidate[0] = "";
					candidate[1] = "";
					continue;
				}
				
				if (tokens[Sam.RNAME].equals("*")) {
					discard = true;
					candidate[1] = "";
					continue;
				}
				
				try {
					mmScore += SAMUtil.getMismatchScore(tokens[Sam.CIGAR], Sam.getMDTAG(tokens));
				} catch (IndexOutOfBoundsException e) {
					System.out.println(line);
					throw e;
				}
				if (mmScore > (tokens[Sam.SEQ].length()*2*5)/100) {
					mmReadNum++;
					discard = true;
					candidate[0] = "";
					candidate[1] = "";
					continue;
				}
				candidate[1] = line;
			} else {
				totalReadNum++;
				
				// write prev candidates
				if (!candidate[0].equals("") && candidate[1].equals("")) {
					if (mmScore > (tokens[Sam.SEQ].length()*5)/100) {
						mmReadNum++;
						candidate[0] = "";
					}
				}
				writeCandidates(fm, candidate);

				// initialize candidates
				candidate[0] = "";
				candidate[1] = "";
				discard = false;
				prevQname = tokens[Sam.QNAME];
				mmScore = 0;
				
				if (tokens[Sam.RNAME].equals("*") && tokens[Sam.RNEXT].equals("*")) {
					discard = true;
					notAlignedNum++;
					continue;
				}
				
				int flag = Integer.parseInt(tokens[Sam.FLAG]);
				if (SAMUtil.isSecondaryAlignment(flag)) {
					System.out.println(":: WARNING :: Secondary alignment found in new sequence id " + tokens[Sam.QNAME]);
					System.out.println(":: WARNING :: Check if the sam is already filtered elsewhere.");
					System.out.println(":: WARNING :: This read will be treated as multiple aligned read.");
					multipleReadNum++;
					discard = true;
					continue;
				}

				// unpaired
				if (tokens[Sam.RNAME].equals("*")) {
					candidate[0] = "";
					// do nothing
				} else {
					mmScore = SAMUtil.getMismatchScore(tokens[Sam.CIGAR], Sam.getMDTAG(tokens));
					candidate[0] = line;
				}
			}
		}

		writeCandidates(fm, candidate);
		
		System.out.println("Total num read pairs\t" + totalReadNum);
		System.out.println("Total uniuqe aligned read pairs\t" + uniqueReadNum);
		System.out.println("Total unpaired read pairs (in out file, only one read appears)\t" + unpairedReadNum);
		System.out.println("Total multiple aligned read pairs\t" + multipleReadNum);
		System.out.println("Total read pairs discarded under mm score (5%)\t" + mmReadNum);
		System.out.println("Total read pairs not aligned\t" + notAlignedNum);
	}

	private void writeCandidates(FileMaker fm, String[] candidate) {
		if (candidate[0].length() > 0 && candidate[1].length() > 0) {
			fm.writeLine(candidate[0]);
			fm.writeLine(candidate[1]);
			uniqueReadNum++;
		} else if (candidate[0].length() > 0) {
			fm.writeLine(candidate[0]);
			unpairedReadNum++;
		} else if (candidate[1].length() > 0) {
			fm.writeLine(candidate[1]);
			unpairedReadNum++;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("java -jar samRemoveMultipleAlignments.jar <in.sam> [CODE=1]");
		System.out.println("\t[CODE]: 1=Unique only [DEFAULT]");
		System.out.println("\t\t2=Unique + Random select (on multiple aligned reads)");
		System.out.println("\t<out>: <in.sam.u> or <in.sam.r>, u for unique and r for random, respectively.");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new RemoveMultipleAlignments().go(args[0], args[0] + ".u");
		} else if (args.length == 2) {
			CODE = Integer.parseInt(args[1]);
			if (CODE == 2) {
				new RemoveMultipleAlignments().go(args[0], args[0] + ".r");
			} else {
				new RemoveMultipleAlignments().printHelp();	
			}
		} else {
			new RemoveMultipleAlignments().printHelp();
		}
	}

}
