package ie.gmit.sw;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Client 
{
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException 
	{
		boolean run=true;
		int option=0;
		Parser p=new Parser();
		Scanner scan=new Scanner(System.in);
		Socket s = null; //Connect to the server
		ObjectOutputStream out=null;
		ObjectInputStream in=null;
		//System.out.println(p.folder);
		while(run)
		{
			System.out.println("\tMenu");
			System.out.println("1-Connect to Server\n2-Print File Listing\n3-Download file\n4-Quit");
			System.out.println("Enter option: ");
			option=scan.nextInt();
			
			try{
				if(option==1) //Open the connection with the server 
				{
					s = new Socket(p.getIp(), p.getPort());
					out = new ObjectOutputStream(s.getOutputStream());
					in = new ObjectInputStream(s.getInputStream());
					System.out.println("Connection established");
				}
				else if(option==2) //Retrieve the list of files available to download
				{
					out.writeObject(option); //send the option to the server 
					out.flush(); 	
					
					String response = (String) in.readObject(); //Receive the response and deserialize 
					System.out.println(response+"\n");
	
				}
				else if(option==3) //Download the file from the server
				{

					out.writeObject(option); //send the option to the server 
					out.flush();
					
					System.out.println("Enter the file name: ");
					String aux=scan.next();
					out.writeObject(aux);
					out.flush();
					
					int size=in.readInt();
					if(size>0) //check if the server returns that the file exists
					{ 				
						byte[] response =  new byte[size];
						in.readFully(response,0,response.length);
						FileOutputStream fop=null;
						try{
						fop = new FileOutputStream(new File(p.getFolder()+aux));
						}catch(Exception e)
						{
							new File(p.getFolder()).mkdir();
							fop = new FileOutputStream(new File(p.getFolder()+aux));
						}
						fop.write(response);
						System.out.println("File successfully downloaded");
						fop.close();
					}
					else
					{
						System.out.println("File not found");
					}
					
				}
				else //Close the connection with the server and finish the program
				{
					scan.close();
					run=false;
					try{
					out.writeObject(4); //tell the server to close the connection
					in.close();
					out.close();
					s.close();
					}catch(Exception e){}
					
				}
			}catch(Exception e) //If the user try to communicate with the server before connection this catch will ask the user to connect first
			{
				System.out.println("Please, connect to the server");
			}

		}
		
	}
}

class Parser
{
	String ip;
	int port;
	String folder;
	
	public Parser() throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(new File("Connection.xml"));
		Element root = doc.getDocumentElement();
		NodeList children=root.getChildNodes();

		ip=children.item(1).getTextContent();
		port=Integer.parseInt(children.item(3).getTextContent());
		folder=children.item(5).getTextContent();
	}
	public String getIp()
	{
		return ip;
	}
	public int getPort()
	{
		return port;
	}
	public String getFolder()
	{
		return folder;
	}
}