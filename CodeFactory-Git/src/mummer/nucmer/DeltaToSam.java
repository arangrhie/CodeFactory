package mummer.nucmer;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.genome.fasta.Seeker;
import javax.arang.sam.SAMUtil;

public class DeltaToSam extends Rwrapper {
	
	private static String qereyFa = null;

	@Override
	public void hooker(FileReader frDelta) {
		String line;
		String[] tokens;
		
		String ref = null;
		String query = null;
		String refPos = null;
		int targetLen = 0;
		int queryLen = 0;
		
		Seeker faSeeker;
		
		int flag = 0;
		int mq = 60;	// arbitrary set to 60
		String cigar = "";
		String seq = null;
		
		int start = 0;	// QuereyStart
		int end = 0;	// QuereyEnd
		int tmp;
		
		line = frDelta.readLine();
		tokens = line.split(RegExp.WHITESPACE);
		
		if (qereyFa == null) {
			faSeeker = new Seeker(new FileReader(tokens[1]));
		} else {
			faSeeker = new Seeker(new FileReader(qereyFa));
		}
		
		line = frDelta.readLine();
		if (!line.equals("NUCMER")) {
			System.err.println("No NUCMER string found. Assuming this is not a NUCMER file. Exit.");
			System.exit(-1);
		}
		
		int indel;
		short prevTypeID = Delta.TYPE_UNSET;
		short typeID;
		int matched;
		int prevMatched = 0;
		int mismatch = 1;
		int sumM = 0;
		int sumI = 0;
		
		// SAM Header
		FileMaker fmHeader = new FileMaker(frDelta.getDirectory(), frDelta.getFileName() + ".samheader");
		
		while (frDelta.hasMoreLines()) {
			line = frDelta.readLine();
			if (line.trim().equals(""))	continue;
			
			tokens = line.split(RegExp.WHITESPACE);
			
			if (Delta.isHeader(line)) {
				System.err.println("[DEBUG] :: " + line);
				ref = Delta.getRefName(tokens);
				targetLen = Delta.getTargetLen(tokens);
				query = Delta.getQueryName(tokens);
				queryLen = Delta.getQueryLen(tokens);
				seq = null;
				sumM = 0;
				sumI = 0;
				prevMatched = 0;
				mismatch = 0;
				prevTypeID = Delta.TYPE_UNSET;
				
				fmHeader.writeLine("@SQ\tSN:" + ref + "\tLN:" + targetLen);	//	@SQ	SN:chr6	LN:170805979
				continue;
			}
			
			// Same fa, different alignment
			if (tokens.length == 7) {
				System.err.println("[DEBUG] :: " + line);
				refPos = tokens[Delta.TARGET_START];
				
				flag = SAMUtil.setFirstSegmentInTemplate(0);

				start = Delta.getQueryStart(tokens);
				end = Delta.getQueryEnd(tokens);
				if (Delta.isReverse(tokens)) {
					flag = SAMUtil.setReverseComplemented(flag);
					tmp = start;
					start = end;
					end = tmp;
				}
				
				if (start > 1) {
					cigar = (start - 1) + "S";	// soft clipped bases
				}
				
				if (!faSeeker.isAt(query) || seq == null) {
					System.err.println("[DEBUG] :: " + "Start seeking...");
					faSeeker.goToContig(query);
					seq = faSeeker.getBases(0, queryLen);
					System.err.println("[DEBUG] :: " + faSeeker.getFaName() + " seq loaded.");
				}
				
				sumM = 0;
				sumI = 0;
				prevMatched = 0;
				mismatch = 0;
				prevTypeID = Delta.TYPE_UNSET;
				continue;
			}
			
			//System.err.println("[DEBUG] :: " + line);
			indel = Integer.parseInt(line.trim());
			
			if (indel == Delta.EOA) {	// End of Alignment
				
				// add the last match & mismatched indel
				//System.err.println("[DEBUG] :: prevMatched : " + prevMatched);
				sumM += prevMatched;
				sumI += (prevTypeID == Delta.TYPE_I) ? mismatch : 0;
				
				if (prevTypeID != Delta.TYPE_UNSET) {
					if (prevMatched == 0) {
						cigar = cigar + mismatch + Delta.getStringValueOfTypeID(prevTypeID);
					} else { 
						cigar = cigar + prevMatched + "M" + mismatch + Delta.getStringValueOfTypeID(prevTypeID);
					}
				} 
				cigar = cigar + (end - start + 1 - (sumM + sumI)) + "M";
				sumM += (end - start + 1 - (sumM + sumI));
				if (queryLen - end > 0) {
					cigar += (queryLen - end) + "S";
				}
				// write the final output
				// For DEBUGging..
				System.out.println(query + "\t" + flag + "\t" + ref + "\t" + refPos + "\t" + mq + "\t" + cigar + "\t*\t0\t0\t" + seq + "\t*");
//				System.err.println("S: " + (queryLen - end));
//				System.err.println("M: " + sumM);
//				System.err.println("I: " + sumI);
//				System.err.println("Sum: S + M + I = " 
//						+ (start - 1 + queryLen - end) + " + " + sumM + " + "+ sumI 
//						+ " = " + ((start - 1 + queryLen - end) + sumM + sumI));
//				System.err.println("QueryLen: " + queryLen);
				prevTypeID = Delta.TYPE_UNSET;
				
				// Initialize sumM and sumI
				sumM = 0;
				sumI = 0;
				continue;
			}
			
			// Save the previous matches / mismatches, type if ID (indel).
			typeID = Delta.getIDType(indel);
			matched = Delta.getMatches(indel);
			
			if (matched == 0 && prevTypeID == typeID) {
				mismatch++;
			} else if (prevTypeID == Delta.TYPE_UNSET) {
				mismatch = 1;
				prevMatched = matched;
			} else {
				// add the previous matched & mismatched indel
				//System.err.println("[DEBUG] :: prevMatched : " + prevMatched);
				if (prevMatched == 0) {
					cigar = cigar + mismatch + Delta.getStringValueOfTypeID(prevTypeID);
				} else { 
					cigar = cigar + prevMatched + "M" + mismatch + Delta.getStringValueOfTypeID(prevTypeID);
				}
				sumM += prevMatched;
				sumI += (prevTypeID == Delta.TYPE_I) ? mismatch : 0;
				
				mismatch = 1;
				prevMatched = matched;
			}
			
			prevTypeID = typeID;
		}
		
		fmHeader.closeMaker();
		faSeeker.close();
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar nucmerDeltaToSam.jar <in.delta> [querey.fasta] > out.sam");
		System.out.println("\t<in.delta>: delta file given by nucmer alignment");
		System.out.println("\t[querey.fasta]: query fasta file");
		System.out.println("*sam headers provided as <in.delta.samheader>. MQ are set to 60.");
		System.out.println("Arang Rhie, 2017-07-25. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new DeltaToSam().go(args[0]);
		} else if (args.length == 2) {
			qereyFa = args[1];
			new DeltaToSam().go(args[0]);
		} else {
			new DeltaToSam().printHelp();
		}
	}

}
