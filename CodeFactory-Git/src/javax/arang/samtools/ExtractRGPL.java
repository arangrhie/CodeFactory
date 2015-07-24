/**
 * 
 */
package javax.arang.samtools;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class ExtractRGPL extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@RG")) {
				tokens = line.split("\t");
				// @RG	ID:AK10	PL:illumina	PU:SureSelectAllExonG3362	LB:AK10	SM:AK10
				String pl = "";
				String sm = "";
				for (String token : tokens) {
					if (token.startsWith("PL")) {
						pl = token.substring(token.indexOf(":") + 1);
					}
					if (token.startsWith("SM")) {
						sm = token.substring(token.indexOf(":") + 1);
					}
				}
				FileMaker fm = new FileMaker(fr.getFullPath(), sm + "_pl");
				fm.writeLine(pl);
				fm.closeMaker();
				break;
			} else if (!line.startsWith("@")) {
				System.out.println("No @RG found...");
				break;
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samtoolsExtractRGPL.jar <sam_header>");
		System.out.println("\t<output>: RGPL(Readgroup Platform) in file <sample_name.rgpl>");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new ExtractRGPL().go(args[0]);
		} else {
			new ExtractRGPL().printHelp();
		}
	}

}
