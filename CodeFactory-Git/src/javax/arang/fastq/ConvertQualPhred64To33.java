package javax.arang.fastq;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;


public class ConvertQualPhred64To33 {

	public void printHelp() {
		System.out.println("Usage: java -jar fastqConvertQualPhred64To33.jar <in.fastq64.gz> | gzip > <out.fastq33.gz>");
		System.out.println("Arang Rhie, 2014-11-07. arrhie@gmail.com");
	}

	public static void main(String[] args) throws Exception {
		
		if (args.length == 1) {
			BufferedReader bufferReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(args[0]))));
			String line;
			int lineCount = 0;
			while(bufferReader.ready()){
				line = bufferReader.readLine();
				if (lineCount % 4 == 3) {
					lineCount = 0;
					for (int i = 0; i < line.length(); i++) {
						System.out.print((char) ((byte) line.charAt(i) - 31));
					}
					System.out.println();
				} else {
					System.out.println(line);
					lineCount++;
				}
			}

			bufferReader.close();
		} else {
			new ConvertQualPhred64To33().printHelp();
		}
	}

}
