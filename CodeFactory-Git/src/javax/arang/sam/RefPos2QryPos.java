package javax.arang.sam;

import java.util.ArrayList;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class RefPos2QryPos extends Rwrapper {

	@Override
	public void hooker(FileReader frSam) {
		
		String line;
		String[] tokens;
		
		String lSeq = "", rSeq = "", qSeq = ""; // ref: REF from sam, rSeq: reference from frBed, qSeq: qry sequence name (read name)
		int rPos, qPos; // reference position seen in sam POS, qry position (0-base)
		int lStart = 0, lEnd = 0; // reference region from frList  Start: 0-base, End: 1-base
		int rStart = 0, rEnd = 0; // ref region to report.         Start: 0-base, End: 1-base
		int qStart = 0, qEnd = 0; // qry region to report.         Start: 0-base, End: 1-base
		boolean qRev = false;     // true = - , false = + direction (reverse complemented)
		
		int match = 0, del = 0, ins = 0; // CIGAR operations

		String cigar;
		
		// For each frList
		tokens = region.split(":");
		lSeq   = tokens[0];

		tokens = tokens[1].split("-");
		lStart = Integer.parseInt(tokens[0].replaceAll(",", "")) - 1; // 0-base
		lEnd   = Integer.parseInt(tokens[1].replaceAll(",", ""));     // 1-base

		// Reading SAM
		while (frSam.hasMoreLines()) {
			line = frSam.readLine();
			tokens = line.split(RegExp.TAB);

			rSeq = tokens[Sam.RNAME];

			if (!lSeq.equals(rSeq)) {
				continue;
			}

			// ref equals rSeq
			rPos = Integer.parseInt(tokens[Sam.POS]) - 1; // beginning must be 0-based to point the last bp of each CIGAR op

			// rPos already passed lEnd. Skip this alignment.
			if (rPos > lEnd) {
				continue;
			}

			// rPos <= lEnd : has some overlap, or alignment ends way before the rStart

			cigar = tokens[Sam.CIGAR];

			// initialize ref
			rStart = lStart;
			rEnd   = lEnd;

			// initialize qry
			qSeq = tokens[Sam.QNAME];
			qPos = 0;  // 0-based
			qStart = 0;
			qEnd = 0;
			qRev = SAMUtil.isReverseComplemented(Integer.parseInt(tokens[Sam.FLAG]));

			// parse CIGAR
			ArrayList<String[]> cigarArr = Sam.parseCIGAR(cigar);

			// initialize CIGAR ops
			match = 0;
			del = 0; // jump rPos
			ins = 0; // jump qPos

			reported = false;

			for (int i = 0; i < cigarArr.size(); i++) {
				if (cigarArr.get(i)[Sam.OP] != null) {
					// Match or Mismatch
					if (cigarArr.get(i)[Sam.OP].equals("M") || cigarArr.get(i)[Sam.OP].equals("=") || cigarArr.get(i)[Sam.OP].equals("X")) {
						match = Integer.parseInt(cigarArr.get(i)[Sam.COUNT]); // 1-base

						// lEnd already passed, report the last matching base positions and finish
						if (lEnd < rPos) {
							if (qEnd == 0) {
								// Nothing overlaps, hold on printing
							} else {
								// System.err.println("[[ DEBUG ]] :: M=X lEnd < rPos");
								printOut(rSeq, rStart, rEnd, qSeq, qStart, qEnd, qRev);
							}
							break;
						}

						// lStart already passed, set the first matching base position as rStart and qStart
						if (qStart == 0 && lStart < rPos) {
							rStart = rPos;
							qStart = qPos;
						}

						// lStart is between rPos and rPos + match
						if (rPos <= lStart && lStart < rPos + match) { 
							// Found the qStart!
							qStart = qPos + (lStart - rPos);
							rStart = lStart;
						}

						// rEnd is between rPos and rPos + match
						if (rPos < lEnd && lEnd <= rPos + match ) {
							// Found the qEnd!
							qEnd  = qPos + (lEnd - rPos);
							rEnd  = lEnd;

							// Print
							// System.err.println("[[ DEBUG ]] :: M=X rPos < lEnd && lEnd <= rPos + match");
							printOut(rSeq, rStart, rEnd, qSeq, qStart, qEnd, qRev);
							break;
						}
						rPos += match;
						qPos += match;
						rEnd  = rPos;
						qEnd  = qPos;
						
						/***
						System.err.println("[[ DEBUG ]] :: M=X" 
								+ " rStart=" + rStart + " rEnd=" + rEnd 
								+ " qStart=" + qStart + " qEnd=" + qEnd 
								+ " rPos=" + rPos + " qPos=" + qPos + " match=" + match);
						***/

						// Insertion
					}  else if (cigarArr.get(i)[Sam.OP].equals("I") 
							||  cigarArr.get(i)[Sam.OP].equals("H") 
							||  cigarArr.get(i)[Sam.OP].equals("S")) {
						ins   = Integer.parseInt(cigarArr.get(i)[Sam.COUNT]);
						qPos += ins;
						qEnd  = qPos;
						
						/***
						System.err.println("[[ DEBUG ]] :: IHS" 
								+ " rStart=" + rStart + " rEnd=" + rEnd 
								+ " qStart=" + qStart + " qEnd=" + qEnd 
								+ " rPos=" + rPos + " qPos=" + qPos + " ins=" + ins);
						***/

						// Deletion
					}  else if (cigarArr.get(i)[Sam.OP].equals("D")) {
						del   = Integer.parseInt(cigarArr.get(i)[Sam.COUNT]);
						if (rPos <= lStart && lStart < rPos + del || rPos <= lEnd && lEnd < rPos + del) {
							if (rPos <= lStart && lStart <= rPos + del) { 
								// lStart is between rPos and rPos + del: jump rStart to rPos + del
								// we can't report within delta
								rStart = rPos + del;
							}
							if (rPos <= lEnd && lEnd <= rPos + del) { // rEnd is between rPos and rPos + del
								// Reduce rEnd accordingly, report the last matching bp POS.
								rEnd = rPos;
								qEnd = qPos;

								// Print
								// System.err.println("[[ DEBUG ]] :: DEL rPos <= lEnd && lEnd <= rPos + del");
								printOut(rSeq, rStart, rEnd, qSeq, qStart, qEnd, qRev);
								break;
							}
						} else {
							rPos += del;
							rEnd  = rPos;
						}
						
						/***
						System.err.println("[[ DEBUG ]] :: DEL"
								+ " rStart=" + rStart + " rEnd=" + rEnd 
								+ " qStart=" + qStart + " qEnd=" + qEnd 
								+ " rPos=" + rPos + " qPos=" + qPos + " del=" + del);
						***/
					}
				}
			}

			if (rEnd < lStart) {
				// Nothing overlaps, do nothing
				// System.err.println("[[ WARNING ]] " + qSeq + " does not align to (bed chr:start-end) " + rSeq + ":" + rStart + "-" + rEnd);
			} else {
				if (! reported) {
					// All CIGAR consumed. Report the last matching base as rEnd and qEnd
					// System.err.println("[[ DEBUG ]] :: Reached the end of read");
					printOut(rSeq, rStart, rEnd, qSeq, qStart, qEnd, qRev);
				}
			}
		} // End of SAM file
	}

	private void printOut(String rSeq, int rStart, int rEnd, String qSeq, int qStart, int qEnd, boolean isRev) {
		char strand = '+';
		if (isRev) {
			strand = '-';
			if (qLen > 0) {
				System.out.println(rSeq + "\t" + rStart + "\t" + rEnd + "\t"
								 + qSeq + "\t" + (qLen - qEnd) + "\t" + (qLen - qStart) + "\t" + strand);
				reported = true;
				return;
			}
		}
		System.out.println(rSeq + "\t" + rStart + "\t" + rEnd + "\t"
						 + qSeq + "\t" + qStart + "\t" + qEnd + "\t" + strand);
		reported = true;
	}
	
	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar samRefPos2QryPos.jar in.sam region [qLen]");
		System.err.println("Print all closest pairing positions in the Ref and Qry, inferred from POS and CIGAR string.");
		System.err.println("  in.sort.sam pipe in `samtools view` (no header)");
		System.err.println("  region      reference region, in chr:start-end format. start = 1-base, end = 1-base.");
		System.err.println("  qLen        query sequence length. OPTIONAL.");
		System.err.println("  sysout      paired bed format, containing rSeq rStart rEnd qSeq qStart qEnd strand");
		System.err.println("              in the order as provided in region (bed format). start = 0-base, end = 1-base");
		System.err.println();
		System.err.println("If start and end position falls in an indel, the closest inner-matching bp position gets reported.");
		System.err.println("*If strand is -, and qLen is provided, qStart and qEnd will be adjusted.");
		System.err.println(" If not, they need to be adjusted as (qLen-qEnd) (qLen-qStart).");
		System.err.println("2024-06-14. Arang Rhie");
	}
	
	private static String  region;
	private static boolean reported = false;
	private static int     qLen = 0;

	public static void main(String[] args) {
		// Becuase we are streaming, we are not able to re-set the input file.
		if (args.length >= 2) {
			region = args[1];
			if (args.length == 3) {
				qLen = Integer.parseInt(args[2]);
			}
			new RefPos2QryPos().go(args[0]);
			
		} else {
			new RefPos2QryPos().printHelp();
		}
	}

}
