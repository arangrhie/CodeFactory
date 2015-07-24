/**
 * 
 */
package javax.arang.igv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author Arang Rhie
 *
 */
public class Screenshot {

	public void go() throws UnknownHostException, IOException {
		Socket socket = new Socket("127.0.0.1", 60151);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

//        out.println("load na12788.bam,n12788.tdf");
//        String response = in.readLine();
//        System.out.println(response);

        out.println("genome mm9");
        String response = in.readLine();
        System.out.println(response);

        out.println("goto chr1:65,827,301");
        //out.println("goto chr1:65,839,697");
        response = in.readLine();
        System.out.println(response);

        out.println("snapshotDirectory /screenshots");
        response = in.readLine();
        System.out.println(response);

        out.println("snapshot");
        response = in.readLine();
        System.out.println(response);
        
        socket.close();
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws UnknownHostException, IOException {
		new Screenshot().go();
	}

}
