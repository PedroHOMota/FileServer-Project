package ie.gmit.sw;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client 
{
	public static void main(String[] args) throws Throwable
	{
		int option=0;
		Scanner scan=new Scanner(System.in);
		Socket s = null; //Connect to the server
		ObjectOutputStream out;
		ObjectInputStream in;
		
		System.out.println("\tMenu");
		System.out.println("1-Connect to Server\n2-Print File Listing\n3-Download file\n4-Quit");
		while(true)
		{
			System.out.println("Enter option: ");
			option=scan.nextInt();
			
			
			if(option==1) //Open the connection with the server 
			{
				s = new Socket("localhost", 7777);
				
			}
			else if(option==2) //Retrieve the list of files available to download
			{
				out = new ObjectOutputStream(s.getOutputStream());
				in = new ObjectInputStream(s.getInputStream());
				out.writeObject(option); //send the option to the server 
				out.flush(); 	
				
				String response = (String) in.readObject(); //Receive the response and deserialize 
				System.out.println(response+"\n");

			}
			else if(option==3) //Download the file from the server
			{
				out = new ObjectOutputStream(s.getOutputStream());
				in = new ObjectInputStream(s.getInputStream());
				System.out.println("Enter the file name: ");
				String aux=scan.next();
				out.writeObject(aux);
				out.flush(); 
				 
				if(in.readBoolean()) //check if the server returns that the file exists
				{ 
					File response =  (File) in.readObject(); //Deserialise
					System.out.println("File successfully downloaded");
				}
				else
				{
					System.out.println("File not found");
				}
				
			}
			else if(option==5)
			{
				out = new ObjectOutputStream(s.getOutputStream());
				in = new ObjectInputStream(s.getInputStream());
				out.writeObject(5);
				System.out.println(in.readObject());
			}
			else //Close the connection with the server and finish the program
			{
				s.close();
			}

		}
		
	}
}
