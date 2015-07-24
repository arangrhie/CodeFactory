package javax.arang.txt;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import javax.arang.IO.basic.FileMaker;

public class Gunzip {
	
	public void printHelp() {
		System.out.println("Usage: java -jar gunzip.jar <in.gz> <out>");
		System.out.println("Arang Rhie, 2015-01-07. arrhie@gmail.com");
	}

	public static void main(String[] args) throws Exception {
		if (args.length == 2) {
			BufferedReader bufferReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(args[0]))));
			FileMaker fm = new FileMaker(args[1]);
			while(bufferReader.ready()){
				fm.writeLine(bufferReader.readLine());
			}
			bufferReader.close();
			fm.closeMaker();
		} else {
			new Gunzip().printHelp();
		}
	}

}
