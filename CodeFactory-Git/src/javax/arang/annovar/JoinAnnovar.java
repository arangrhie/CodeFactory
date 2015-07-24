/**
 * 
 */
package javax.arang.annovar;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class JoinAnnovar extends I2Owrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		String line1;
		String line2;
		String[] tokens2;
		
		int count = 0;
		fm.write("\t\t\t\t\t\t");
		while (fr1.hasMoreLines()) {
			line1 = fr1.readLine();
			line2 = fr2.readLine();
			tokens2 = line2.split("\t");
			fm.write(line1.trim());
			for (int i = ANNOVAR.NOTE + 1; i < tokens2.length; i++) {
				fm.write("\t" + tokens2[i]);
			}
			fm.writeLine("");
			count++;
		}
		
		System.out.println(count + " lines processed");
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar snpJoinAnnovar.jar <in1.snp> <in2.snp> <out.snp>");

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 3) {
			new JoinAnnovar().go(args[0], args[1], args[2]);
		} else {
			new JoinAnnovar().printHelp();
		}

	}

}
