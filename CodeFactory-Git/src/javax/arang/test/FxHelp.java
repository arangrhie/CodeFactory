/**
 * 
 */
package javax.arang.test;

/**
 * @author Arang Rhie
 *
 */
public class FxHelp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FxHelp.usage();
	}
	
	public static void usage() {
		System.out.println("FX Local Hadoop Cluster Manual");
		System.out.println();
		System.out.println("Usage: hadoop jar fx.jar [project_dir] [OPTIONS]");
		System.out.println("		or hadoop jar fx.jar --help");
		System.out.println();
		System.out.println("Required Option");
		System.out.println("project_dir	Path to HDFS project directory that contains [rawdata] directory");
		System.out.println("		e.g., if you created my_project on HDFS, and created directory structure as:");
		System.out.println("			my_project");
		System.out.println("				\\| rawdata");
		System.out.println("					\\| s_1_1_sequence.txt");
		System.out.println("					\\| s_1_2_sequence.txt");
		System.out.println("		put my_project as the project directory.");
		System.out.println();
		System.out.println();
		System.out.println("GSNAP Option");
		System.out.println("	-g : GSNAP Path [DEFAULT : /usr/local/gmap_2011-10-16/bin/gsnap]");
		System.out.println("	-d : GSNAP DB for gene model [DEFAULT : --db=3db_forTxn.genome]");
		System.out.println("	-b : GSNAP Reference DB Path for gene model");
		System.out.println("			[DEFAULT : --dir=/usr/local/gmap_2011-03-28/reference_db]");
		System.out.println("	-n : GSNAP Reference Version [DEFAULT : hg19], you may put hg18 instead of hg19");
		System.out.println("	-A : GSNAP Reference DB Path for genome model");
		System.out.println("			[DEFAULT : --dir=/usr/local/gmap_2011-03-28/reference_db]");
		System.out.println("			Note that the coherent GSNAP reference db will be named automatically as the option given in -n + \".genome\".");
		System.out.println("			For example, genome reference db name will be \"hg19.genome\" as -n is set as hg19 by default.");
		
		System.out.println();
		System.out.println("Step Options");
		System.out.println("	FX runs in two ways; 'run at once' or 'step by step'.");
		System.out.println("	If no step options are specified, FX will run at once from preprocess to expression profiling with default options.");
		System.out.println("	Configuration parameters in each step are optional, and will run with default values if not specified.");
		System.out.println();

		System.out.println("-P, --preprocess	Preprocess FASTQ paired end reads into GSNAP oriented input format");
		System.out.println("			Use with -s.");
		System.out.println("-G, --gsnapAlign	Align the preprocessed reads from -P with GSNAP");
		System.out.println("			Use with -m, -i, -g, -d, -b, -n and -o (specify o only if needed).");
		System.out.println("-M, --gsnap		Set if the input file is in GSNAP output format");
		System.out.println("			The SAM (or GSNAP alignment) files must be located under [my_project/align_results].");
		System.out.println("			By default, SAM alignment output formats are used");
		System.out.println("			in -baseCall, -indelCall, -expProfiling.");
		System.out.println("-B, --baseCall		Call out bases properly aligned in GSNAP default output format");
		System.out.println("			This step filters bases under -q (quality threshold) out,");
		System.out.println("			and trims bases from both ends of read. Only uniquely aligned reads to the");
		System.out.println("			reference sequence are left and called.");
		System.out.println("			This step is required to be performed before running -snpCall, -indelCall.");
		System.out.println("			Use with -q, -h, and -t.");
		System.out.println("-S, --snpCall		Call SNPs out of base calling results");
		System.out.println("			Make sure you have ?baseCall result files located under [my_project/base_call].");
		System.out.println("			Use with -x, -r.");
		System.out.println("-I, --indelCall		Call INDELs out of GSNAP default alignment result");
		System.out.println("			Make sure you have ?baseCall result files located under [my_project/base_call].");
		System.out.println("			Use with -X, -R.");
		System.out.println("-E, --expProfiling	Expression profiling");
		System.out.println("			Similar to RPKM, normalize expression level in base resolution of each gene (BPKM)");
		System.out.println("			Use with -Z, -Q, set --rpkm if desired.");
		System.out.println("-U, --collectUnmapped	Collect unmapped reads, and align them against the reference genome.");
		System.out.println("			Use with -A=(dir path) to provide the reference genome location without the reference version.");
		System.out.println("			The reference version given with -n option will be automatically added on the path.");
		System.out.println("			The genome name will be assumed as (GSNAP reference version).genome by default.");
		System.out.println("			Use with -a=(other gsnap splicing options). [DEFAULT=-s splicesites -N -n 40 -t 1 -O].");
		System.out.println();
		System.out.println("Configuration Options");
		System.out.println("-s, --split=INT			Split input FASTQ sequence file into [ ] number of files");
		System.out.println("				Each split file generates one GSNAP alignment process.");
		System.out.println("				For performance enhancement, consider that maximum number of");
		System.out.println("				GSNAP alignment processes are running simultaneously as");
		System.out.println("				the # of map slot capacity.");
		System.out.println("				[DEFAULT=36], assumes 12 map slot capacity");
		System.out.println("-m, --maxMismatches=INT or FLOAT	Maximum number of mismatches allowed");
		System.out.println("				If specified between 0.0 and 1.0, then treated as a fraction");
		System.out.println("				of each read length. Otherwise, treated as an integral number");
		System.out.println("				of mismatches (including indel and splicing penalties)");
		System.out.println("				For RNA-Seq, you may need to increase this value slightly");
		System.out.println("				to align reads extending past the ends of an exon.");
		System.out.println("				[DEFAULT=10]");
		System.out.println("-i, --indelPenalty=INT		Penalty for an indel");
		System.out.println("				Counts against mismatches allowed. To find indels, make");
		System.out.println("				indel-penalty less than or equal to max-mismatches");
		System.out.println("				For 2-base reads, need to set indel-penalty somewhat high");
		System.out.println("				[DEFAULT=3]");
		System.out.println("-o, --otherGsnapOpt=STRING	Other GSNAP alignment options quoted with \" \"");
		System.out.println("				[DEFAULT=\"-n 40 -t 1 -O\"]");
		System.out.println("-a 				Other GSNAP genome alignment options quoted with \" \"");
		System.out.println("				[DEFAULT=\"-s splicesites -N 1 -n 40 -t 1 -O\"]");
		

		System.out.println("-q, --qualBase=INT		Filter out bases under Phred quality score");
		System.out.println("				e.g., 20 means -10Log10^2, i.e., 1 error possibility over 10^-20 bases.");
		System.out.println("				[DEFAULT=20]");
		System.out.println("-f, --qualOffset=INT		Quality offset used in FASTQ input quality score");
		System.out.println("				e.g., for Solexa GAII, which uses Phred+64 ASCII value, set this option as 64.");
		System.out.println("				[DEFAULT=33]");
		System.out.println("-t, --trimBase=INT		Trim [ ] bases from each read ends to avoid adapter sequences included");
		System.out.println("				[DEFAULT=4]");
		System.out.println("-x, --snpCount=INT		Consider as SNP candidate when allele observed differs more than x times");
		System.out.println("				[DEFAULT=4]");
		System.out.println("-X, --indelCount=INT		Consider as INDEL candidate when allele observed differs more than x times");
		System.out.println("				[DEFAULT=4]");
		System.out.println("-r, --snpRatio=FLOAT		Defines as SNP when SNP frequency over wild type appears more than r %");
		System.out.println("				[DEFAULT=1.0](%)");
		System.out.println("-R, --indelRatio=FLOAT		Defines as INDEL when INDEL frequency over wild type appears more than R %");
		System.out.println("				[DEFAULT=1.0](%)");
		System.out.println("-Z, --expLevel=FLOAT		Filter genes as \'xpressed\' when BPKM(or RPKM) > Z");
		System.out.println("				BPKM: Bases per Kilobase of exon model per Million mapped bases");
		System.out.println("				bpkm = 10^9 x C(g) / ( C(b) x L )");
		System.out.println("				C(x) : counted number of x");
		System.out.println("				g : Mapped bases on the gene exon region");
		System.out.println("				b : Total number of mapped bases after trimming");
		System.out.println("				L : Exon length of the gene");
		System.out.println("				[DEFAULT=1.0]");
		System.out.println("-Q, --ui		Use union intersection gene model instead of union intersection model.");
		System.out.println("				This argument takes effect only at expression profiling (--expProfiling) stage.");
		System.out.println("				[DEFAULT: Union Gene Model]");
		System.out.println("-k, --rpkm			Get expression level normalized in RPKM.");
		System.out.println("				[DEFAULT: BPKM]");
		System.out.println();
		System.out.println("Help Options");
		System.out.println("--version		Current version");
		System.out.println("--help			Print this help message");
	}

}
