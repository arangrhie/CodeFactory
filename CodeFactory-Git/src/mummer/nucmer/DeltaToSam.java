package mummer.nucmer;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.genome.fasta.Seeker;
import javax.arang.sam.SAMUtil;

public class DeltaToSam extends Rwrapper {

	@Override
	public void hooker(FileReader frDelta) {
		String line;
		String[] tokens;
		
		String ref = null;
		String query = null;
		String refPos = null;
		int queryLen = 0;
		
		int flag = 0;
		int mq = 60;
		String cigar = null;
		String seq = null;
		
		int start;
		int end = 0;
		int tmp;
		
		line = frDelta.readLine();
		tokens = line.split(RegExp.WHITESPACE);
		
		Seeker faSeeker = new Seeker(new FileReader(tokens[1]));
		
		line = frDelta.readLine();
		if (!line.equals("NUCMER")) {
			System.err.println("This is not a NUCMER file. Exit.");
			System.exit(-1);
		}
		
		int indel;
		short prevTypeID = Delta.TYPE_UNSET;
		short typeID;
		int matched;
		int prevMatched = 0;
		int mismatch = 1;
		
		while (frDelta.hasMoreLines()) {
			line = frDelta.readLine();
			if (line.trim().equals(""))	continue;
			
			tokens = line.split(RegExp.WHITESPACE);
			
			if (Delta.isHeader(line)) {
				System.err.println("[DEBUG] :: " + line);
				ref = Delta.getRefName(tokens);
				query = Delta.getQueryName(tokens);
				queryLen = Delta.getQueryLen(tokens);
				continue;
			}
			
			// Same fa, different alignment
			if (tokens.length == 7) {
				System.err.println("[DEBUG] :: " + line);
				refPos = tokens[Delta.REF_START];
				
				start = Delta.getQueryStart(tokens);
				end = Delta.getQueryEnd(tokens);
				
				if (start > 1) {
					cigar = (start - 1) + "S";	// soft clipped bases
				}
				
				flag = SAMUtil.setFirstSegmentInTemplate(0);
				if (Delta.isReverse(tokens)) {
					flag = SAMUtil.setReverseComplemented(flag);
					tmp = start;
					start = end;
					end = tmp;
				}
				
				faSeeker.goToContig(query);
				seq = faSeeker.getBases(0, queryLen);
				continue;
			}
			
			//System.err.println("[DEBUG] :: " + line);
			indel = Integer.parseInt(line.trim());
			if (indel == 0) {	// End of alignment
				// add the last match & mismatched indel
				cigar = cigar + prevMatched + "M" + mismatch + Delta.getStringValueOfTypeID(prevTypeID);
				if (queryLen - end > 0) {
					cigar += (queryLen - end) + "S";
				}
				// write the final output
				System.out.println(query + "\t" + flag + "\t" + ref + "\t" + refPos + "\t" + mq + "\t" + cigar + "\t*\t0\t0\t" + seq + "\t*");
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
				cigar = cigar + prevMatched + "M" + mismatch + Delta.getStringValueOfTypeID(prevTypeID); 
				mismatch = 1;
				prevMatched = matched;
			}
			
			prevTypeID = typeID;
			
			
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar nucmerDeltaToSam.jar <in.delta> > out.sam");
		System.out.println("\t<in.delta>: delta file given by nucmer alignment");
		System.out.println("\t<query.fasta>: query fasta file");
		System.out.println("*No sam headers provided here at this time. MQ are set to 60.");
		System.out.println("Arang Rhie, 2017-01-30. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new DeltaToSam().go(args[0]);
		} else {
			new DeltaToSam().printHelp();
		}
	}

}
