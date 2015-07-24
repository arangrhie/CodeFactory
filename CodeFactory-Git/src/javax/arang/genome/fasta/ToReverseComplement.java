package javax.arang.genome.fasta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ToReverseComplement {

	public static void main(String[] args) {
		try{
			BufferedReader br = 
	                      new BufferedReader(new InputStreamReader(System.in));
	 
			String input;
			while((input=br.readLine())!=null){
				for (int i = input.length() - 1; i >= 0; i--) {
					System.out.print(toComplement(input.charAt(i)));
				}
			}
	 
		}catch(IOException io){
			io.printStackTrace();
		}	

	}
	
	private static char toComplement(char code) {
		switch(code) {
		case 'a': return 'T';
		case 'A': return 'T';
		case 'c': return 'G';
		case 'C': return 'G';
		case 'g': return 'C';
		case 'G': return 'C';
		case 't': return 'A';
		case 'T': return 'A';
				
		}
		return 'n';
	}

}
