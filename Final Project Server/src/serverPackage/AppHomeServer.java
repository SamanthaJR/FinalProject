package serverPackage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class AppHomeServer{
	
	public static String decision;
	
    public void startServer(){

    	    try{
    	      ServerSocket listener = new ServerSocket(4444);
    	      System.out.println("AHS: Started, Listening to port 4444");
    	      Socket server;

//    	        doComms connection;

    	        server = listener.accept();
    	        System.out.println("AHS: AppClient accepted.");
    	        doComms conn_c= new doComms(server);
    	        Thread t = new Thread(conn_c);
    	        t.start();
    	   
    	    } catch (IOException ioe) {
    	      System.out.println("AHS: Listen error " + ioe);
    	      ioe.printStackTrace();
    	    }
    	  }

    }

class doComms implements Runnable {
    private Socket server;
    private String line,input;
    doComms(Socket server) {
      this.server=server;
    }

    public void run () {

      input="";

      try {
        // Get input from the client
    	  InputStreamReader inputStreamReader = new InputStreamReader(server.getInputStream());
    	  BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
    	  PrintStream out = new PrintStream(server.getOutputStream());
    	  
    	  	out.println("Hello Client");
    	  	
    	  	System.out.println("AHS: Handshake sent.");
    	  	
			line = bufferedReader.readLine();
			if (checkMessage(line, "Hello Server")){
				System.out.println("Message received:" + line);
			} else throw new UnexpectedClientMessageException("Handshake incorrect.");
			while (true){
				
				line = bufferedReader.readLine();
				
				System.out.println("Waiting for response");
			
				if (checkMessage(line, "Accepted")) {
					AppHomeServer.decision = "true";
					System.out.println("Accepted login!");
					break;
				} else if (checkMessage(line, "Declined")) {
					AppHomeServer.decision = "false";
					System.out.println("Accepted login!");
					break;
				} 			
			}
			
			out.println("Goodbye Client");

        server.close();
      } catch (IOException ioe) {
        System.out.println("IOException on socket listen: " + ioe);
        ioe.printStackTrace();
      } catch (UnexpectedClientMessageException e) {
    	  System.out.println("Unexpected Client response");
		e.printStackTrace();
	}
    }
    
    /**
     * Method assesses both parameter Strings are equal.
     * @param string The expected message from the Client.
     * @param string1 The actual message from the Client.
     * @return boolean true if they match, false if not.
     */
	private boolean checkMessage(String string, String string1){
		if (string.equals(string1)){
			return true;
		} else {
			return false;
		}	
	}
}




//
//public class AppHomeServer{
//	 
//	    private  ServerSocket serverSocket;
//	    private  Socket clientSocket;
//	    private  InputStreamReader inputStreamReader;
//	    private  BufferedReader bufferedReader;
//	    private  PrintWriter printWriter;
//	    private  String message;
//	    public boolean decision;
//	 
//	    public void startServer() throws UnexpectedClientMessageException {
//	 
//	        try {
//	            serverSocket = new ServerSocket(4444);
//	 
//	        } catch (IOException e) {
//	            System.out.println("Could not listen on port: 4444");
//	        }
//	 
//	        System.out.println("AppHomeServer started. Listening to the port 4444");
//	 
//	        while (true) {
//	            try {
//	                clientSocket = serverSocket.accept();
//	                System.out.println("AppClient accepted.");
//	                inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
//	                bufferedReader = new BufferedReader(inputStreamReader);
//	                printWriter= new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
//
//	                sendServerMessage("Hello Client");
//	                System.out.println("Handshake sent.");
//	                
//	                message = bufferedReader.readLine();
//	                
//	                System.out.println(message);
//	               
//						if (checkMessage("Hello Server", message)){
//							System.out.println(message);
//						} else {
//							throw new UnexpectedClientMessageException("Unexpected message from Client." + message);
//						}
//						
//						decision = loginAcceptedByUser(bufferedReader.readLine());
//							
//						sendServerMessage("Goodbye Client//
//public class AppHomeServer{
//
//   private  ServerSocket serverSocket;
//   private  Socket clientSocket;
//   private  InputStreamReader inputStreamReader;
//   private  BufferedReader bufferedReader;
//   private  PrintWriter printWriter;
//   private  String message;
//   public boolean decision;
//
//   public void startServer() throws UnexpectedClientMessageException {
//
//       try {
//           serverSocket = new ServerSocket(4444);
//
//       } catch (IOException e) {
//           System.out.println("Could not listen on port: 4444");
//       }
//
//       System.out.println("AppHomeServer started. Listening to the port 4444");
//
//       while (true) {
//           try {
//               clientSocket = serverSocket.accept();
//               System.out.println("AppClient accepted.");
//               inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
//               bufferedReader = new BufferedReader(inputStreamReader);
//               printWriter= new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
//
//               sendServerMessage("Hello Client");
//               System.out.println("Handshake sent.");
//               
//               message = bufferedReader.readLine();
//               
//               System.out.println(message);
//              
//					if (checkMessage("Hello Server", message)){
//						System.out.println(message);
//					} else {
//						throw new UnexpectedClientMessageException("Unexpected message from Client." + message);
//					}
//					
//					decision = loginAcceptedByUser(bufferedReader.readLine());
//						
//					sendServerMessage("Goodbye Client");
//               
//               inputStreamReader.close();
//               clientSocket.close();
//
//           } catch (IOException ex) {
//               System.out.println("Problem in message reading");
//           }
//       }
//
//   }
//   
//   /**
//    * Method assesses both parameter Strings are equal.
//    * @param string The expected message from the Client.
//    * @param string1 The actual message from the Client.
//    * @return boolean true if they match, false if not.
//    */
//	private boolean checkMessage(String string, String string1) throws UnexpectedClientMessageException {
//		if (string.equals(string1)){
//			return true;
//		} else {");
//	                
//	                inputStreamReader.close();
//	                clientSocket.close();
//	 
//	            } catch (IOException ex) {
//	                System.out.println("Problem in message reading");
//	            }
//	        }
//	 
//	    }
//	    
//	    /**
//	     * Method assesses both parameter Strings are equal.
//	     * @param string The expected message from the Client.
//	     * @param string1 The actual message from the Client.
//	     * @return boolean true if they match, false if not.
//	     */
//		private boolean checkMessage(String string, String string1) throws UnexpectedClientMessageException {
//			if (string.equals(string1)){
//				return true;
//			} else {
//				return false;
//			}	
//		}
//		
//		/**
//		 * Sends a message to the client
//		 * @param message - the message we wish to send
//		 */
//		public void sendServerMessage(String message){
//				printWriter.print(message);
//		}
//
//		public boolean loginAcceptedByUser(String message) {
//			try {
//				if(checkMessage("Accepted", message)){
//					return true;
//				} else {
//					return false;
//				}
//			} catch (Exception e) {
//				System.out.println("AHS: ");
//				e.printStackTrace();
//				return false;
//			}
//		}
//		
//		
//		
////		public static void main(String[] args){
////			AppHomeServer ahs = new AppHomeServer();
////			try {
////				ahs.startServer();
////			} catch (UnexpectedClientMessageException e) {
////				System.out.println("AHS Main: ");
////				e.printStackTrace();
////			}
////		}
////		
//		
//		
//	}