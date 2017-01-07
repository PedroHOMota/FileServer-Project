package ie.gmit.sw;


import java.io.*; 
import java.net.*; 
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class WebServer {
	private ServerSocket ss; //A server socket listens on a port number for incoming requests
	
	private BlockingQueue<String> queue;
	
	private volatile boolean keepRunning = true;
	
	private static final int SERVER_PORT = 7777;
	
	private static String downPath="";
	
	private Calendar c = Calendar.getInstance();
	
	private WebServer(int port,String path){
		try { //Try the following. If anything goes wrong, the error will be passed to the catch block
			
			ss = new ServerSocket(port); //Start the server socket listening on port 8080
			System.out.println(ss.getLocalSocketAddress()+" "+ss.getInetAddress());
			Thread server = new Thread(new Listener(), "Web Server Listener"); //We can also name threads
			server.setPriority(Thread.MAX_PRIORITY); //Ask the Thread Scheduler to run this thread as a priority
			server.start();
			
			System.out.println("Server started and listening on port " + SERVER_PORT);
			
			Thread logger=new Thread(new Logger());
			logger.start();
			
		} catch (IOException e) { 
			System.out.println("Yikes! Something bad happened..." + e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		//System.out.println(args[0]+" "+args[1]);
		//downPath=args[1];
		new WebServer(7777,"Download");
	}
	
	
	private class Listener implements Runnable{
		private int counter=0;
		private Logger log=new Logger();
		public void run() {
			while (keepRunning){
				try { //Try the following. If anything goes wrong, the error will be passed to the catch block
					
					Socket s = ss.accept(); //This is a blocking method, causing this thread to stop and wait here for an incoming request
					
					String m="[INFO] Listing request by "+s.getInetAddress()+" at "+ c.getTime().getHours()+":"+ c.getTime().getMinutes()+
							" on "+c.get(Calendar.DAY_OF_MONTH)+"/"+c.get(Calendar.MONTH)+1+"/"+c.get(Calendar.YEAR);
					log.WriteLog(m);
					new Thread(new Request(s), "T-"+counter).start(); 
					
					counter++;
				} catch (IOException e) { 
					System.out.println("Error handling incoming request..." + e.getMessage());
				}
			}
		}
	}
	
	private class Request implements Runnable
	{
		private Socket sock; 
		private boolean run=true;
		private Logger log=new Logger();
		
		private Request(Socket request) {
			this.sock = request; 
		}

		public void run() 
        {
        	 
            try{             	
            	ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
                int command=0;
                
                while(run)
                {
	                command = (int)in.readObject();
	                
	                switch (command)
	                {
		                case 2: //return the list of files
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
		                case 3: //return the requested file
		                {
		                	String name="";
		                	File folder = new File("your/path");
		                	File[] listFiles = folder.listFiles();
		                	
		                	folder=null; //setting the variable to null
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
		                case 4: //close the socket and stop the loop
		                {
		                	String m="[INFO] Closing connection with "+sock.getInetAddress()+" at "+ c.getTime().getHours()+":"+ c.getTime().getMinutes()+
									" on "+c.get(Calendar.DAY_OF_MONTH)+"/"+c.get(Calendar.MONTH)+1+"/"+c.get(Calendar.YEAR);
							log.WriteLog(m);
		                	in.close();
		                	out.close();
		                	sock.close();
		                	run=false;	
		                }
		                		                	
	                }
                } 
            } catch (Exception e) { 
            	System.out.println("Error processing request from " + sock.getRemoteSocketAddress());
            	e.printStackTrace();
            }
        }
	}//End of inner class HTTPRequest

	static class Logger implements Runnable
	{
		private static BlockingQueue<String> q=new ArrayBlockingQueue<String>(50);
		
		public void WriteLog(String m) 
		{
			System.out.println("outro: "+m);
			try {
				q.put(m);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void run()
		{
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date();
			String msg="";
            try 
            {
            	FileOutputStream fileData=new FileOutputStream("log"+dateFormat.format(date)+".txt",true);
    			BufferedWriter bFile=new BufferedWriter(new FileWriter("log"+dateFormat.format(date)+".txt",true));
		    			
				while((msg = q.take())!="exit")
				{ 
					System.out.println(msg);
					bFile.append(msg);
					bFile.newLine();
					bFile.flush();
				}
				System.out.println("out");
				bFile.close();
				fileData.close();
			} 
            catch (InterruptedException | IOException e) 
            {
				e.printStackTrace();
			}
          
		}
	}
}