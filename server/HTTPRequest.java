/* Kevin Chawla
   1001543244
 * */

package server;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
public class HTTPRequest implements Runnable {
	final static String CRLF = "\r\n"; //This is abbreviation for end of line character for HTTP response
	Socket socket; //This is the client socket from which we will pull and push data
	
	//Constructor
	public HTTPRequest(Socket socket) throws Exception {
		this.socket = socket;
	}
	
	//Implements the run() method of the Runnable interface, which allows this class to be used in a m1ultithreaded approach.
	//It tries to run processRequest() and reports any errors.
	public void run() {
		try {
			processRequest();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	//Parses HTTP request from the socket and handles it. Is able to handle responses for 200, 301, and 404 status codes.
	//Also generates response with appropriate status line, header lines, and the requested file.
	private void processRequest() throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //to read data from socket
		DataOutputStream out = new DataOutputStream(socket.getOutputStream()); //to write data to socket
		
		//Get request line of the HTTP request message
		String requestLine = in.readLine();
		
		//Extract the filename from the request line
		String filename = "." + requestLine.split(" ")[1]; //add . in front of filename so that file request is within current directory
		
		FileInputStream fis = null; //to check if file exists and if so, used to push requested file data to client socket
		
		//Logic checking for 301 response. If a html file is requested other than index.html that contains "index", forward traffic to index.html
		if(!filename.equals("./index.html") && filename.contains("index") && filename.endsWith(".html")) {
			out.writeBytes("HTTP/1.0 301 Moved Permanently" + CRLF); //write 301 status code
			out.writeBytes("Location: /index.html" + CRLF); //write where to redirect to
			
			//this sets the file that would be displayed and that the header lines will report info about
			filename = "./301.html";
			fis = new FileInputStream(filename);
		} else {
			//Open requested file
			boolean fileExists = true; //assume file exists
			try {
				fis = new FileInputStream(filename);
			} catch (Exception e) {
				fileExists = false; //if there is error, that means file doesn't exist
			}
			
			//Send status line
			if (fileExists) {
				out.writeBytes("HTTP/1.0 200 OK" + CRLF);
			} else {
				out.writeBytes("HTTP/1.0 404 Not Found" + CRLF);
				
				//this sets the file that will be displayed and that the header lines will report info about
				filename = "./404.html";
				fis = new FileInputStream(filename);
			}
		}
		
		//Send header lines
		out.writeBytes("Connection: close" + CRLF);
		out.writeBytes("Date: " + LocalDateTime.now().toString() + CRLF); //DATE
		out.writeBytes("Server: Brandon Chase's Local Server" + CRLF); //SERVER NAME
		File f = new File(filename); //for getting file info
		out.writeBytes("Last-Modified: " + Long.toString(f.lastModified()) + CRLF); //LAST MODIFIED
		out.writeBytes("Content-Length: " + Long.toString(f.length()) + CRLF); //CONTENT LENGTH
		out.writeBytes("Content-Type: " + getContentType(filename) + CRLF); //CONTENT TYPE
		
		//Send blank line to indicate end of header lines
		out.writeBytes(CRLF);
		
		//Send data
		sendBytes(fis, out);
		
		//Close streams and client socket
		fis.close();
		in.close();
		socket.close();
	}
	
	//Sends requested file in 1KB chunks to client through socket
	//Parameters: fis - input stream for reading desired file
	//Parameters: out - output stream for sending desired chunks to client
	private void sendBytes(FileInputStream fis, OutputStream out) throws Exception {
		//Construct a 1KB buffer to hold bytes on way to socket
		byte[] buffer = new byte[1024];
		int bytes = 0; //used for detecting when no more bytes have been read from file
		
		//Copy requested file into socket's output stream
		while((bytes = fis.read(buffer)) != -1) { //continuously read chunks of file and send it to client
			out.write(buffer, 0, bytes);
		}
	}
	
	//Takes filename and extracts its MIME type; used for a header line in the HTTP response
	//Parameters: name of the file to extract the MIME type from
	private String getContentType(String filename) {
		//get file extension by reading everything after the last period
		String extension = filename.substring(filename.lastIndexOf('.') + 1);
		switch(extension) { //only the file types used for this project were included
			case "htm":
			case "html":
				return "text/html";
			case "png":
				return "image/png";
			default:
				return "application/octet-stream"; //return this type if not a known type
		}
	}
}
