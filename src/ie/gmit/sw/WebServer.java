package ie.gmit.sw;

/* This class provides a very simple implementation of a web server. As a web server
 * must be capable of handling multiple requests from web browsers at the same time,
 * it is essential that the server is threaded, i.e. that the web server can perform
 * tasks in parallel and serially (one request at a time, after another).
 * 
 * In programming languages, all network communication is handled using sockets. A 
 * socket is a software abstraction of a connection between one computer on a network
 * and another. A server-socket is a process that listens on a port number for 
 * incoming client requests. For example, the standard port number for a HTTP server (a
 * web server) is port 80. Most of the commonly used Java networking classes are 
 * available in the java.net package. The java.io package contains a set of classes
 * designed to handle Input/Output (I/O) activity. We will use both packages in the web
 * server class below.  
 */

import java.io.*; //Contains classes for all kinds of I/O activity
import java.net.*; //Contains basic networking classes
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

public class WebServer {
	private ServerSocket ss; //A server socket listens on a port number for incoming requests
	
	private BlockingQueue<String> queue;
	
	private static final int SERVER_PORT = 8080;  
	
	private volatile boolean keepRunning = true;
	
	private WebServer(){
		try { //Try the following. If anything goes wrong, the error will be passed to the catch block
			
			ss = new ServerSocket(SERVER_PORT); //Start the server socket listening on port 8080
			System.out.println(ss.getLocalSocketAddress()+" "+ss.getInetAddress());
			Thread server = new Thread(new Listener(), "Web Server Listener"); //We can also name threads
			server.setPriority(Thread.MAX_PRIORITY); //Ask the Thread Scheduler to run this thread as a priority
			server.start(); //The Hollywood Principle - Don't call us, we'll call you
			
			System.out.println("Server started and listening on port " + SERVER_PORT);
			
		} catch (IOException e) { //Something nasty happened. We should handle error gracefully, i.e. not like this...
			System.out.println("Yikes! Something bad happened..." + e.getMessage());
		}
	}
	
	//A main method is required to start a standard Java application
	public static void main(String[] args) {
		new WebServer();
	}
	
	
	
	/* The inner class Listener is a Runnable, i.e. a job that can be given to a Thread. The job that
	 * the class has been given is to intercept incoming client requests and farm them out to other
	 * threads. Each client request is in the form of a socket and will be handled by a separate new thread.
	 */
	private class Listener implements Runnable{ //A Listener IS-A Runnable
		
		//The interface Runnable declare the method "public void run();" that must be implemented
		public void run() {
			int counter = 0; //A counter to track the number of requests
			while (keepRunning){ //Loop will keepRunning is true. Note that keepRunning is "volatile"
				try { //Try the following. If anything goes wrong, the error will be passed to the catch block
					
					Socket s = ss.accept(); //This is a blocking method, causing this thread to stop and wait here for an incoming request
					new Thread(new HTTPRequest(s), "T-" + counter).start(); 
					counter++; 
				} catch (IOException e) { 
					System.out.println("Error handling incoming request..." + e.getMessage());
				}
			}
		}
	}
	
	private class HTTPRequest implements Runnable
	{
		private Socket sock; 
		
		private HTTPRequest(Socket request) {
			this.sock = request; 
		}

		public void run() 
        {
        	 
            try{             	
            	ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
                int command=0;
                
                while(true)
                {
	                command = (int)in.readObject();
	                
	                switch (command)
	                {
		                case 2:
		                {
		                	File folder = new File("your/path");
		                	File[] listFiles = folder.listFiles();
		                	String aux="";
		                	for(int i=0;i<listFiles.length;i++)
		                		if(listFiles[i].isFile())
		                			aux+=listFiles[i].getName()+"\n";
		                	
		                	out.writeObject(aux);
		                	break;
		                }
		                case 3:
		                {
		                	String name="";
		                	File folder = new File("your/path");
		                	File[] listFiles = folder.listFiles();
		                	String aux="";
		                	for(int i=0;i<listFiles.length;i++)
		                		if(listFiles[i].getName().matches("")) //Checking if the name of the file exists in the foler
		                			{ 
		                				folder=listFiles[i];
		                				break;
		                			}
		                	
		                	out.writeObject(folder);
		                	out.flush();
		                	break;
		                }
		                case 5:
		                {
		                	System.out.println("teste");
		                	//in.close();
		                	out.writeObject("idiota");
		                	out.flush();
		                	//out.close();
		                	//this.finalize();
		                }
	                }
                } 
            } catch (Exception e) { 
            	System.out.println("Error processing request from " + sock.getRemoteSocketAddress());
            	e.printStackTrace();
            }
        }
	}//End of inner class HTTPRequest

	static class Queue implements Runnable
	{
		private static BlockingQueue<String> q;
		
		public Queue(BlockingQueue q)
		{
			this.q=q;
		}
		
		public void WriteLog(String m) 
		{
			try {
				q.put(m);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void run()
		{
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
			Date date = new Date();
			String msg="";
            try 
            {
    			BufferedWriter bFile=new BufferedWriter(new FileWriter("log"+dateFormat.format(date)+".txt"));

				while((msg = q.take())!="exit")
				{ 
					bFile.append(msg);
				}
				bFile.close();
			} 
            catch (InterruptedException | IOException e) 
            {
				e.printStackTrace();
			}
          
		}
	}
}