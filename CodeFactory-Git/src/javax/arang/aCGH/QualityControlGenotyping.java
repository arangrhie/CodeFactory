/**
 * 
 */
package javax.arang.aCGH;

import java.util.Vector;

/**
 * @author Arang Rhie
 *
 */
public class QualityControlGenotyping extends ReadArray {


	protected void qcOnProbes(String[][] array,
			int numSamples, int numProbes, Vector<Integer> qcFailed) {
		
		int cut = (int) ((float)numSamples * 0.1);
		System.out.println("Disregard probes with probes containing > " + cut + " \'NA\'s.");

		// QC on probes
		for (int probeIdx = 0; probeIdx < numProbes; probeIdx++) {
			int sumNA = 0;
			for (int sampleIdx = 0; sampleIdx < numSamples; sampleIdx++) { 
				if (array[probeIdx][sampleIdx].equals("NA")) {
					sumNA++;
				}
			}
			if (sumNA > cut) {
				qcFailed.add(probeIdx);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Quality control over aCGH data.");
		System.out.println("Remove probes with genotype < 90%");
		System.out.println("Usage: java -jar aCGHqcGenotyping.jar <in.txt> <numStartColumn> <numSamples>");
		System.out.println("\t<in.txt>: Sample_ID\tOS\t...\tProbe1\tProbe2\t...");
		System.out.println("\t<numStartColumn>: starting column of the probe, 0-base");
		System.out.println("\t<numSamples>: number of samples");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 3) {
			numStartColumn = Integer.parseInt(args[1]);
			numSamples = Integer.parseInt(args[2]);
			new QualityControlGenotyping().go(args[0], args[0].replace(".txt", "_qc_gt90.txt"));
		} else {
			new QualityControlGenotyping().printHelp();
		}
	}
}
