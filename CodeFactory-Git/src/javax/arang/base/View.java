/**
 * 
 */
package javax.arang.base;

import javax.arang.IO.BinaryIFileOwrapper;
import javax.arang.IO.bambasic.BinaryReader;
import javax.arang.IO.basic.FileMaker;

/**
 * @author Arang Rhie
 *
 */
public class View extends BinaryIFileOwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.BinaryIFileOwrapper#hooker(javax.arang.IO.BinaryReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(BinaryReader br, FileMaker fm) {
		int refLen = br.readByte();
		while (refLen != -1) {
			String chr = br.readChars(refLen);
			int pos = br.readInt();
			System.out.print(chr + "\t" + pos);
			for (int i = 0; i < 5; i++) {
				System.out.print("\t" + br.readInt());
			}
			System.out.println();
			refLen = br.readByte();
		}
		
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.BinaryIFileOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar baseView.jar <in.base> [region.bed]");
		System.out.println("\t<in.base>: binary file.");
		System.out.println("\t\t(char)chr\t(int)pos	Depth (A C G T D)");
		System.out.println("Arang Rhie, 2014-03-07. arrhie@gmail.com");
	}
	
	public static void main(String[] args) {
		if (args.length == 1) {
			new View().go(args[0], "test.cov");
		} else {
			new View().printHelp();
		}
	}

}
