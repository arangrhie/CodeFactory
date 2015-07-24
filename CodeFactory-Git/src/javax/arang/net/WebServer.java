/*
	Computer Network Socket Programming
	10. 19. 2007, Arang Rhie
	arrhie@gmail.com
	
	To run
	1. execute this WebServer.java
	2. open a web browser, type in: http://localhost:5001/notice.html - this shows eclipse notice
	3. open a web browser, type in: http://localhost:5001/banner_mdg.gif - this shows an gif image
*/

package javax.arang.net;

import java.io.*;
import java.net.*;
import java.util.*;

public class WebServer {

	public static void main(String args[]) throws Exception {
		//Set the port number.
		int port = 5001;

		//Establish the listen socket.
		ServerSocket serverSocket = new ServerSocket(port);

		//Process HTTP service requests in an infinite loop.
		while(true){
			//Listen for a TCP connection request.
			Socket connectionSocket = serverSocket.accept();

			//Construct an object to process the HTTP request message.
			HttpRequest request = new HttpRequest(connectionSocket);

			//Create a new thread to process the request.
			Thread thread = new Thread(request);

			//Start the thread.
			thread.start();
		}
		//After creating a new thread, execution in the main thread returns to
		//the top of the loop waiting for another TCP connection request.
	}
}

final class HttpRequest implements Runnable{
	final static String CRLF ="\r\n";
	Socket socket;

	//Constructor
	public HttpRequest(Socket socket) throws Exception {
		// TODO Auto-generated constructor stub
		this.socket=socket;
	}
	//Implement the run() method of the Runnable interface
	public void run(){
		try{
			processRequest();
		}catch (Exception e){
			System.out.println(e);
		}
	}
	private void processRequest() throws Exception{
		//Get a reference to the socket`s input and output streams.
		InputStream is = socket.getInputStream();
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());

		//Set up input stream filters.
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);

		//Get the request line of the HTTP request message.
		String requestLine = br.readLine();

		//Display the request line.
		System.out.println();
		System.out.println(requestLine);

		/*******************Part B**********************/
		//Extract the fileName from the request line.
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken(); //skip over the method,which should be "GET"
		String fileName=tokens.nextToken();

		//Prepend a "." so that file request is within the current directory.
		fileName="."+fileName;

		//Open the requested file.
		FileInputStream fis=null;
		boolean fileExists=true;
		try{
			fis = new FileInputStream(fileName);
		}catch(FileNotFoundException e){
			fileExists = false;
		}

		//Construct the response message.
		String statusLine=null;
		String contentTypeLine=null;
		String entityBody=null;
		if(fileExists){
			//If the file exists, request object later in this message.
			statusLine= "HTTP/1.0 200 OK" ;
			contentTypeLine = "Content-type: "+
				contentType(fileName) + CRLF;
		}else{
			//If the requested file is not found on this server,
			//show this message later on the browser.
			statusLine= "HTTP/1.0 404 Not Found";
			//Since the file does not exist,
			//we skip the content type line part.
			contentTypeLine= "" ;
			//Spread the error message on the browser.
			entityBody = "<HTML>"+
			"<HEAD><TITLE>Not Found</TITLE></HEAD>" +
			"<BODY>Not Found</BODY></HTML>";
		}

		//Send the status line.
		os.writeBytes(statusLine);

		//Send the content type line.
		os.writeBytes(contentTypeLine);

		//Send a blank line to indicate the end of the header lines.
		os.writeBytes(CRLF);

		//Send the entity body.
		if(fileExists){
			//If the requested file exist,
			//we call a separate method to send the file.
			sendBytes(fis, os);
			fis.close();
		}else{
			//If the requested file does not exist,
			//send the HTML-encoded error message that we have prepared.
			os.writeBytes(entityBody);
		}
		/*******************Part B************************/

		/*******************Part A**********************/
		//Get and display the header lines.
		String headerLine = null;
		while((headerLine = br.readLine()).length() != 0){
			System.out.println(headerLine);
		}
		/*******************Part A**********************/

		//Close streams and socket.
		os.close();
		br.close();
		socket.close();

	}

	private static void sendBytes(FileInputStream fis, OutputStream os)	throws Exception{
		//construct a 1K buffer to hold bytes on their way to the socket.
		byte[] buffer = new byte[1024];
		int bytes = 0;

		//Copy requested file into the socket's output stream.
		while((bytes = fis.read(buffer))!=-1){
			os.write(buffer, 0, bytes);
		}
	}

	//When the file exists,
	//determine the file`s MIME type and send the appropriate MIME type specifier.


	private static String contentType(String fileName){
		//If the file extension is .htm or .html
		if(fileName.endsWith(".htm") || fileName.endsWith(".html")){
			return "text/html";
		}
		//If the file extension is .jpeg or .jpg
		else if(fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")){
			return "image/jpeg";
		}
		//If the file extension is is .gif
		else if(fileName.endsWith(".GIF")){
			return "image/gif";
		}
		//If the file extension is unknown,
		//we return the type application/octet-stream.
		else return "application/octet-stream";
	}
}
