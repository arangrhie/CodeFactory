/**
 * 
 */
package javax.arang.paf;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

/**
 * @author rhiea
 *
 */
public class ToClippedSV extends Rwrapper {

	private String qSVName = "", tSVName = "";
	private Double qStart, qEnd, tStart, tEnd, qLen, tSVStart = 0d, tSVEnd = 0d, qSVStart = 0d, qSVEnd = 0d;

	private String qName = "";
	private String tName = "";
	
	private boolean isPositive = true;
	
	private String strand = "*", svStrand = "*";
	private String prevStrand = "";
	private String prevQName = "";
	private String prevTName = "";

	@Override
	public void hooker(FileReader fr) {
		
		// Variables
		String[] tokens;
		
		boolean foundSV = false;
		
		while (fr.hasMoreLines()) {
			
			tokens = fr.readLine().split(RegExp.TAB);

			qName = tokens[PAF.Q_NAME];
			qStart = Double.parseDouble(tokens[PAF.Q_START]);
			qEnd = Double.parseDouble(tokens[PAF.Q_END]);
			qLen	= Double.parseDouble(tokens[PAF.Q_LEN]);
			
			tName	= tokens[PAF.T_NAME];
			tStart	= Double.parseDouble(tokens[PAF.T_START]);
			tEnd	= Double.parseDouble(tokens[PAF.T_END]);
			
			strand  = tokens[PAF.STRAND];
			isPositive = PAF.isPositive(tokens[PAF.STRAND]);
			
			// new query sequence starting
			if (! qName.equals(prevQName)) {
				// output if there were any SVs found
				if (foundSV) {
					printBEDPE();
					foundSV = false;
				}
				
				// is end of the first alignment truncated?
				if (   ( isPositive && (qLen - qEnd) > min_clipped)
					|| (!isPositive &&  qStart       > min_clipped)) {
					tSVName = tName; 
					tSVStart = tEnd;
					
					qSVName = qName;
					if (isPositive) {
						qSVStart = qEnd;
					} else {
						// - alignments are swapped in order for query; second part comes first
						qSVEnd = qStart;
					}
					svStrand = strand;
				}
			} else {
				// same query sequence, different alignment detected
				
				// check this alignment belongs to the same target sequence
				// TODO: keep track of all target sequences,
				// so we don't loose any true SVs. Ignore for now
				if (!tName.equals(prevTName) || !strand.equals(prevStrand)) {
					continue;
				}
				
				// same target, same strand. look for SV patterns:
				// is beginning of the second alignment truncated? (assume no weird translocation events are happening)
				
				if (   ( isPositive &&  qStart       > min_clipped )
					|| (!isPositive && (qLen - qEnd) > min_clipped )) {
					tSVEnd = tStart;
					if (isPositive) {
						qSVEnd = qStart;
					} else {
						// - alignments are swapped in order for query; first part comes next
						qSVStart = qEnd;
					}
					
					if (tSVStart < tSVEnd)	foundSV = true;
				}
			}
			prevQName = qName;
			prevTName = tName;
			prevStrand = strand;
		}
		
		if (foundSV)  printBEDPE();
	}
	
	private void printBEDPE() {
		System.out.println(
			tSVName + "\t" + String.format("%.0f", tSVStart) + "\t" + String.format("%.0f", tSVEnd) + "\t" +
			qSVName + "\t" + String.format("%.0f", qSVStart) + "\t" + String.format("%.0f", qSVEnd) + "\t" +
			qSVName + ":" + String.format("%.0f", (qSVStart + 1)) + "-" + String.format("%.0f", qSVEnd) + "\t" +
			"+\t"+ svStrand
		);
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar pafToClippedSV.jar in.paf");
		System.err.println("Find simple SVs between split 1, 2 alignments: 1--->  2---> or <---1 <---2");
		System.err.println("  in.paf  querey-sorted paf file");
		System.err.println("  stdout  clipped SV-like regions in paired-bed format");
		System.err.println("Arang Rhie, 2021-12-30. arrhie@gmail.com");

	}
	
	private static double min_clipped = 50;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new ToClippedSV().go(args[0]);
		} else {
			new ToClippedSV().printHelp();
		}
	}

}
