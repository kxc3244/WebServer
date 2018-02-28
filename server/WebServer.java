/* Brandon Chase, 1001132518
 * 
 * Sources:
 * 		https://www.cs.bu.edu/fac/matta/Teaching/CS552/F99/proj4/ ~ Help with overall project design and implementation
 * 		https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html ~ Help with reading and writing to socket
 * 		https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html ~ Help with setting up server sockets
 * */

package server;

import java.net.Socket;
import java.net.ServerSocket;

public class WebServer {
	public static void main(String argv[]) throws Exception {		
		//Set the port number
		int portNumber = 2127;
		
		//Open the server socket
		ServerSocket serverSocket = new ServerSocket(portNumber);
		
		try {
			//Process HTTP requests indefinitely
			while (true) {
				//Connect to client
				Socket clientSocket = serverSocket.accept();
				//Generate new request using client connection
				HTTPRequest request = new HTTPRequest(clientSocket);
				//Spawn new thread to handle the request. This allows server to execute many requests in parallel
				Thread thread = new Thread(request);
				thread.start();
			}
		} catch (Exception e) { //Print any error messages
			System.out.println(e);
		} finally { //Close the server socket when the server is done listening
			serverSocket.close();
		}
	}
}
