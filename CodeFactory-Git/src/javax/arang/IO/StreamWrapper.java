package javax.arang.IO;

import javax.arang.IO.basic.Wrapper;

public abstract class StreamWrapper extends Wrapper {

	public void go() {
		startTiming();
		
		System.err.println("Processing streamed input");
		
		hooker();
		
		printTiming();
	}
	
	public abstract void hooker();
	
	public abstract void printHelp();

}
