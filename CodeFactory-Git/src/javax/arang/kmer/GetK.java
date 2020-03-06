package javax.arang.kmer;

public class GetK {

	public static void main(String[] args) {
		if (args.length >=1) {
			double genomeSize = Double.parseDouble(args[0]);
			double errRate = 0.001;
			if (args.length == 2) {
				errRate = Double.parseDouble(args[1]);
			}
			
			double p = (1-errRate)/errRate;
			
			System.out.println("k=" + Math.log(genomeSize * p) / Math.log(4));
		} else {
			System.out.println("Usage: java -jar getK.jar <genome_size> [error_rate]");
			System.out.println("\tReturns k-size for the given <genome_size> that survives [error_rate].");
			System.out.println("\t<genome_size>: ex. 1100000000 for 1.1G");
			System.out.println("\t[error_rate]: DEFAULT=0.001.Error rate of the tolerated k-mer collesion.");
			System.out.println("*Short implementation of trioCanu k-mer threshold.");
			System.out.println("Arang Rhie, 2018-03-30. arrhie@gmail.com");
		}

	}

}
