package javax.arang.genome.fasta;

import javax.arang.IO.basic.FileMaker;

public class Writer {

	FileMaker fmFa = null;
	StringBuffer buffer = null;
	
	public Writer(FileMaker fmFa) {
		this.fmFa = fmFa;
		buffer = new StringBuffer();
	}
	
	
	public void write(String bases) {
		buffer.append(bases);
		if (buffer.length() > 80) {
			clearBuffer();
		}
	}
	
	public void clearBuffer() {
		fmFa.writeLine(buffer.toString());
	}
}
