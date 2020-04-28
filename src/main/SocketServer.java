package main;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.*;



public class SocketServer {

	static HashMap<String,Client>clients_map = new HashMap<String, Client>();

	

	static boolean readFromFile()
	{
		try 
		{
			FileInputStream fileIn = new FileInputStream("data/map.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			clients_map = (HashMap<String,Client>) in.readObject();
			in.close();
			fileIn.close();
			return true;
		}
		catch (IOException i)
		{
			i.printStackTrace();
			return false;
		} 
		catch (ClassNotFoundException c) 
		{
			System.out.println("class not found");
			c.printStackTrace();
			return false;
		}
	}

	public static void main(String[] args) throws Exception
	{
		ServerSocket ss = new ServerSocket(9999);
		String str_client, str_server = null; 
		  
		System.out.println("Server is Up and Running on port : 9999");
	    // connect it to client socket 
		Socket s = null;
		
		if(readFromFile())
		{
			System.out.println("Map read from file");
		}
		else
		{
			System.out.println("Could not read Map from file");
		}
		try
		{
	        while (true) 
	        { 
	        	
	        	s = ss.accept(); 
	            System.out.println("New Connection established");
	            System.out.println("A new client is connected : " + s.getInetAddress()); 
	      
	            // to send data to the client 
	            DataOutputStream dos  = new DataOutputStream(s.getOutputStream()); 
	            
	            DataInputStream dis = new DataInputStream(s.getInputStream());
	           
	            Thread t = new ClientHandler(s, dis, dos);
	            
	            t.start();
	
	        }
		}
		catch(Exception e)
		{ 
			System.out.println("Exception occured here ");
            e.printStackTrace(); 
        } 
	}
	


}
