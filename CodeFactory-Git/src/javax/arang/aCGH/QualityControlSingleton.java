/**
 * 
 */
package javax.arang.aCGH;

import java.util.Vector;

/**
 * @author Arang Rhie
 *
 */
public class QualityControlSingleton extends ReadArray {

	static float cutline = 0.00f;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 4) {
			ReadArray.numStartColumn = Integer.parseInt(args[1]);
			ReadArray.numSamples = Integer.parseInt(args[2]);
			cutline = Float.parseFloat(args[3]);
			new QualityControlSingleton().go(args[0], args[0].replace(".txt", "_" + String.format("%,.2f",cutline) + ".txt"));
		} else if (args.length == 3) {
			ReadArray.numStartColumn = Integer.parseInt(args[1]);
			ReadArray.numSamples = Integer.parseInt(args[2]);
			int cutline100 = 0;
			cutline = 0.00f;
			QualityControlSingleton obj = new QualityControlSingleton();
			for (int i = 0; i < 41; i++) {
				System.out.println("Cutline: " + cutline);
				obj.go(args[0], args[0].replace(".txt", "_" + String.format("%,.2f",cutline) + ".txt"));
				//cutline += 0.0500000000000000000000000000f;
				cutline100 += 5;
				cutline = (float) cutline100 / 100;
			}
			
		}
	}

	@Override
	protected void qcOnProbes(String[][] array,
			int numSamples,
			int numProbes,
			Vector<Integer> qcFailed) {
		
		for (int probeIdx = 0; probeIdx < numProbes; probeIdx++) {
			int count_less = 0;
			int count_more = 0;
			
			for (int sampleIdx = 0; sampleIdx < numSamples; sampleIdx++) {
				if (array[probeIdx][sampleIdx].equals("NA")) continue;
				float arrayValue = Float.parseFloat(array[probeIdx][sampleIdx]);
				if (arrayValue <= cutline * (-1)) {
					count_less++;
				} else if (arrayValue >= cutline) {
					count_more++;
				}
			}
			
			//System.out.println((count_more + count_less) <= 1 ? "1" : "0" + "\t" + count_more + "\t" + count_less);
			if (count_less + count_more <= 1) {
				qcFailed.add(probeIdx);
			}
			if (probeIdx == 1672) {
				System.out.println("DEBUG :: " + header[numStartColumn + probeIdx] + " " + count_more + " " + count_less);
			}
		} 
		
	}
	
	@Override
	public void printHelp() {
		System.out.println("Quality control over aCGH data.");
		System.out.println("Remove probes with singleton peaks");
		System.out.println("Usage: java -jar aCGHqcSingleton.jar <in.txt> <numStartColumn> <numSamples>");
		System.out.println("\t<in.txt>: Sample_ID\tOS\t...\tProbe1\tProbe2\t...");
		System.out.println("\t<numStartColumn>: starting column of the probe, 0-base");
		System.out.println("\t<numSamples>: number of samples");
		
	}

}
