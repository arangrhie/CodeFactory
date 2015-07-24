package javax.arang.test;

import java.util.ArrayList;
import java.util.Random;

public class ThreadTest {

	int seq;
	public int d = 0;
	static Random randomGenerator = new Random();
	
	public ThreadTest(int seq) {
		this.seq = seq;
		d = randomGenerator.nextInt(1000);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ArrayList<MyThread> threads = new ArrayList<MyThread>();
        for(int i=0; i<10; i++) {
            MyThread t = new MyThread(i);
            t.start();
            threads.add(t);
        }

        for(int i=0; i<threads.size(); i++) {
        	MyThread t = threads.get(i);
            t.join();
        }
        System.out.println("main end.");
        
        for(int i=0; i<threads.size(); i++) {
        	MyThread t = threads.get(i);
            System.out.println(t.getD());
        }
	}
	
	

}
