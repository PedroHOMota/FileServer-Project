package ie.gmit.sw;


import java.io.*; 
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class WebServer {
	private ServerSocket ss; //A server socket listens on a port number for incoming requests
	
	private volatile boolean keepRunning = true;
	
	private static final int SERVER_PORT = 7777;
	
	private static String downPath="";
	
	private Calendar c = Calendar.getInstance();
	
	private WebServer(String port,String path){
		try {
			downPath=path;
			ss = new ServerSocket(Integer.parseInt(port)); //Start the server socket listening on port 8080
			Thread server = new Thread(new Listener(), "Web Server Listener");
			server.setPriority(Thread.MAX_PRIORITY); //Ask the Thread Scheduler to run this thread as a priority
			server.start();
			
			System.out.println("Server started and listening on port " + SERVER_PORT);
			
			Thread logger=new Thread(new Logger());
			logger.start();
			
		} catch (IOException e) { 
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		new WebServer(args[0],args[1]);
	}
	
	
	private class Listener implements Runnable{
		private int counter=0;
		private Logger log=new Logger();
		private Socket s=null;
		public void run() {
			while (keepRunning){
				try {
					
					s = ss.accept(); //This is a blocking method, causing this thread to stop and wait here for an incoming request
					
					String m="[INFO] Listing request by "+s.getInetAddress()+" at "+ c.getTime().getHours()+":"+ c.getTime().getMinutes()+
							" on "+c.get(Calendar.DAY_OF_MONTH)+"/"+c.get(Calendar.MONTH)+1+"/"+c.get(Calendar.YEAR);
					log.WriteLog(m);
					new Thread(new Request(s), "T-"+counter).start(); 
					
					counter++;
				} catch (IOException e) 
				{
					String m="[Error] Error handling request by "+s.getInetAddress()+" at "+ c.getTime().getHours()+":"+ c.getTime().getMinutes()+
							" on "+c.get(Calendar.DAY_OF_MONTH)+"/"+c.get(Calendar.MONTH)+1+"/"+c.get(Calendar.YEAR);
					log.WriteLog(m);
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
		                	String m="[INFO] list of available files requested by "+sock.getInetAddress()+" at "+ c.getTime().getHours()+":"+ c.getTime().getMinutes()+
									" on "+c.get(Calendar.DAY_OF_MONTH)+"/"+c.get(Calendar.MONTH)+1+"/"+c.get(Calendar.YEAR);
							log.WriteLog(m);
							String aux="";
		                	try{
			                	File folder = new File(downPath);
			                	File[] listFiles = folder.listFiles();
			                	for(int i=0;i<listFiles.length;i++)
			                		if(listFiles[i].isFile())
			                			aux+=listFiles[i].getName()+"\n";
								}catch(Exception e)
								{
									m="[ERROR] folder doesn't exist or is inaccessible. request by "+sock.getInetAddress()+" at "+ c.getTime().getHours()+":"+ c.getTime().getMinutes()+
											" on "+c.get(Calendar.DAY_OF_MONTH)+"/"+c.get(Calendar.MONTH)+1+"/"+c.get(Calendar.YEAR);
									aux="ERROR, the server could find the files";
									log.WriteLog(m);
								}
		                			                	
		                	out.writeObject(aux);
		                	break;
		                }
		                case 3: //return the requested file
		                {
		                	Path path = null;
      	
		                	String fileName=(String) in.readObject();
		                	
		                	String m="[INFO] "+fileName+" requested by "+sock.getInetAddress()+" at "+ c.getTime().getHours()+":"+ c.getTime().getMinutes()+
									" on "+c.get(Calendar.DAY_OF_MONTH)+"/"+c.get(Calendar.MONTH)+1+"/"+c.get(Calendar.YEAR);
							log.WriteLog(m);
							try
							{
		                	File folder = new File(downPath);
		                	File[] listFiles = folder.listFiles();

		                	folder=null; //setting the variable to null
		                	for(int i=0;i<listFiles.length;i++)
		                		if(listFiles[i].getName().matches(fileName)) //Checking if the name of the file exists in the foler
		                			{ 
		                				path=Paths.get(listFiles[i].getPath());
		                				break;
		                			}
							}catch(Exception e)
							{
								m="[ERROR] folder doesn't exist or is inaccessible. request by "+sock.getInetAddress()+" at "+ c.getTime().getHours()+":"+ c.getTime().getMinutes()+
										" on "+c.get(Calendar.DAY_OF_MONTH)+"/"+c.get(Calendar.MONTH)+1+"/"+c.get(Calendar.YEAR);
								log.WriteLog(m);
							}

		                	
		                	if(path==null) //if the file is not found, send 0 to the client and exit 
		                	{ 
		                		out.writeInt(0); //send the size of the array
		                		out.flush(); 
		                		break;
		                	}
		                	byte[] data = Files.readAllBytes(path); //convert the file to byte array
		                	
		                	out.writeInt(data.length); //send the size of the array
		                	out.flush();
		                	out.write(data); //send the actual array
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
		    			
				while((msg = q.take())!="exit") //keeps running until receives the exit message
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