/**
 * 
 */
package javax.arang.aCGH;

import java.util.Vector;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public abstract class ReadArray extends IOwrapper {

	protected static int numStartColumn = 0;
	protected static int numSamples = 0;
	protected String[][] array;
	protected String[] header;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line = fr.readLine();
		String tokens[] = line.split("\t");
		int numProbes = tokens.length - numStartColumn;
		header = tokens;
		String[] sampleAnnotation = new String[numSamples];
		array = new String[numProbes][numSamples];
		StringBuffer newLine = null;
		int sampleIdx = 0;
		// Copy original array matrix
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			newLine = new StringBuffer();
			
			// write columns before the first probe
			for (int i = 0; i < numStartColumn; i++) {
				newLine.append(tokens[i] + "\t");
			}
			sampleAnnotation[sampleIdx] = newLine.toString().trim();
			
			// copy probe info
			for (int i = numStartColumn; i < tokens.length; i++) {
				int probeIdx = i - numStartColumn;
				array[probeIdx][sampleIdx] = tokens[i];
			}
			sampleIdx++;
		}
		
		Vector<Integer> qcFailed = new Vector<Integer>();
		
		// QC on probes
		qcOnProbes(array, numSamples, numProbes, qcFailed);
				System.out.println("Total number of QC failed probes: " + qcFailed.size());

		System.out.println("Writing header..");
		newLine = new StringBuffer();
		for (int i = 0; i < numStartColumn; i++) {
			newLine.append(header[i] + "\t");
		}
		fm.write(newLine.toString().trim());
		newLine = new StringBuffer();
		for (int probeIdx = 0; probeIdx < numProbes; probeIdx++) {
			if (!qcFailed.contains(probeIdx)) {
				fm.write("\t" + header[numStartColumn + probeIdx]);
			}
		}
		fm.writeLine("");
		
		System.out.println("Writing probes..");
		// Write on file
		for (sampleIdx = 0; sampleIdx < numSamples; sampleIdx++) {
			fm.write(sampleAnnotation[sampleIdx]);
			for (int probeIdx = 0; probeIdx < numProbes; probeIdx++) {
				if (!qcFailed.contains(probeIdx)) {
					fm.write("\t" + array[probeIdx][sampleIdx]);
				}
			}
			fm.writeLine("");
		}
		System.out.println("Done!");
	}
	
	protected abstract void qcOnProbes(String[][] array,
			int numSamples, int numProbes, Vector<Integer> qcFailed) ;

	@Override
	public void printHelp() {
		// TODO Auto-generated method stub

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
