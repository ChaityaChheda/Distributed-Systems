package main;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import static main.SocketServer.clients_map;
import main.Client;
import main.Util;


public class ClientHandler extends Thread {
	final DataInputStream dis; 
    final DataOutputStream dos; 
    final Socket s; 
    Util util;
    String sent,received;
    String username;
    Client client;
    Timestamp timestamp;
    SimpleDateFormat sdf;
    
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos)  
    { 
        this.s = s; 
        this.dis = dis; 
        this.dos = dos;
        util = new Util();
        sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
    } 
    
    @Override
    public void run()  
    {

    	try {
    		
    		dos.writeUTF("Enter Your username");
			received = dis.readUTF();
			username = received;

			try {
				File file = new File(username+"-server");
				if (file.createNewFile()) {
					System.out.println("File created: " + username);
				} 
				else {
					System.out.println("File for " + username + " already exists.");
				}
			} catch (IOException e) {
				System.out.println("An error occurred."+e.toString());
				e.printStackTrace();
			}

			
			timestamp = new Timestamp(System.currentTimeMillis());

			if(clients_map.containsKey(username))
			{
				client = clients_map.get(username);
				sent = "Hello " + username + "\nWelcome back.\n";
				BufferedWriter writer1 = new BufferedWriter(
                	new FileWriter(username+"-server", true)  //Set true for append mode
		        );
				writer1.newLine();   //Add new line
				writer1.write("User logged in at " + sdf.format(timestamp));
				writer1.close();
			}
			else
			{
				client = new Client(username,1000, false);
				clients_map.put(username, client);	
				sent = "Hello " + username + "\nInitiated your Bank Account.\n";
				//File file = new File(username+"-server");
				BufferedWriter writer1 = new BufferedWriter(
                	new FileWriter(username+"-server", true)  //Set true for append mode
		        );
				writer1.newLine();   //Add new line
				writer1.write("User signed up at " + sdf.format(timestamp));
				writer1.close();

				writer1 = new BufferedWriter(
                	new FileWriter(username+"-checkpoint", true)  //Set true for append mode
		        );
				writer1.newLine();   //Add new line
				writer1.write("User signed up at " + sdf.format(timestamp));
				writer1.close();	
			}
				
			
			
			sent = sent + getuserPrompt();
			dos.writeUTF(sent);
			
			while(true){
				
				received = dis.readUTF();
				if(client.getIsBlocked() && !received.equals("5"))
				{
					sent = "Sorry " + username + " you have been blocked.\n";
					dos.writeUTF(sent);
				}
				else if(received.equals("1"))
				{

					sent = "Choose from the list of clients:(Type the client name and amount as space separated string)\n";
					sent = sent + getClients();
					dos.writeUTF(sent);

					received = dis.readUTF();
					String[] queries = received.split("\\s+");

					executeTransaction(queries[0],Integer.parseInt(queries[1]));
					
					sent = sent + getuserPrompt();
					dos.writeUTF(sent);
					
				}
				else if(received.equals("2"))
				{

					// sent = Integer.toString(clients_map.get(user).getBalance());
					//SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
					timestamp = new Timestamp(System.currentTimeMillis());

					String cur_balance = "Current Balance is: " +Integer.toString(clients_map.get(username).getBalance());
					sent  = cur_balance + "\n\n";
					
					BufferedWriter writer1 = new BufferedWriter(
							new FileWriter(username+"-server", true)  //Set true for append mode
						);
					writer1.newLine();   //Add new line
					writer1.write(cur_balance + " \n TimeStamp : "+ sdf.format(timestamp));
					writer1.close();

					sent = sent + getuserPrompt();
					dos.writeUTF(sent);

				}
				else if(received.equals("3"))
				{

					sent = getMsgs();
					sent  = sent +"\nCurrent Balance is: " +Integer.toString(clients_map.get(username).getBalance())+"\n\n";
					
					sent = sent + getuserPrompt();
					dos.writeUTF(sent);

				}
				else if(received.equals("4"))
				{

					File f2 = new File(username+"-server");
					System.out.print(f2.length());
					
					
					dos.writeUTF(Long.toString(f2.length()));
					received = dis.readUTF();
					System.out.print(received);
					
					util.sendLogFile(username+"-server",dos);
					received =  dis.readUTF();
					System.out.println(received);
					sent = "View "+ username +"-client File in the src directory. \nType send to transfer back the LogFile \nWarning: Any changes detected in the File will cause you to be blocked by the System !";
					dos.writeUTF(sent);
					
					
					received =  dis.readUTF();
					dos.writeUTF("received file size"+received);
					
	
					util.saveFile(username+"-temp", dis, Integer.parseInt(received));
					
					if(!util.Compare2Files(username +"-server", username +"-temp"))
					{
						System.out.println("Files are unequal");
						//block user 
						client.blockUser();
						clients_map.put(username, client);
						dos.writeUTF("\nDetected Corruption in Log Files, Dear " + username +", you have now been blocked!!");

						//roll back all actions since last check point
						//<<


						//RITWIK / KAPS FILL THIS

						//>>

					}
					else
					{
						//erase checkpoint 
						BufferedWriter writer1 = new BufferedWriter(
				                new FileWriter(username+"-checkpoint")
				        );
						writer1.newLine();   //Add new line
						writer1.write("");
						writer1.close();
						sent = getuserPrompt();
						dos.writeUTF(sent);
					}
					
					
				}
				else if(received.equals("5"))
				{
					this.dis.close(); 
			        this.dos.close(); 
			        break;
		         
				}
			}
			
		}
		catch(IOException e)
		{ 
			boolean isWritten = util.writeMaptoFile(clients_map);
	        System.out.println("exception occured here" + isWritten); 
	    } 
    }


    private synchronized void executeTransaction(String receiver,int amount) throws IOException{
    	
    	if(client.debit(amount, receiver))
    	{
    		clients_map.get(receiver).credit(amount, username);
    		clients_map.put(username, client);
    	}
    	
    	
    	
    	BufferedWriter writer1 = new BufferedWriter(
                new FileWriter(username+"-server", true)  //Set true for append mode
        );
		writer1.newLine();   //Add new line
		writer1.write(clients_map.get(username).getLog());
		writer1.close();

		writer1 = new BufferedWriter(
                new FileWriter(username+"-checkpoint", true)  //Set true for append mode
        );
		writer1.newLine();   //Add new line
		writer1.write(clients_map.get(username).getLog());
		writer1.close();
		
		System.out.println("Logs"+clients_map.get(username).getLog());
		
		System.out.println("Logs"+clients_map.get(receiver).getLog());
		
		BufferedWriter  writer = new BufferedWriter(
                new FileWriter(receiver+"-server", true)  //Set true for append mode
            );  
		writer.newLine();   //Add new line
		writer.write(clients_map.get(receiver).getLog());
		
		writer.close();

		writer = new BufferedWriter(
                new FileWriter(receiver+"-checkpoint", true)  //Set true for append mode
            );  
		writer.newLine();   //Add new line
		writer.write(clients_map.get(receiver).getLog());
		
		writer.close();
    	
    	
    }


    String getMsgs(){

    	String[] msgs = clients_map.get(username).checkNewMsg().split("-");
    	System.out.println(clients_map.get(username).checkNewMsg());
    	String retval="";
    	int i;
    	for(i =0;i<msgs.length;i++){
    		retval = retval + msgs[i] +"\n";
    	}
    	return retval;
    }
    
    
    
    
    public String getuserPrompt()
    {
    	return "\n Choose from the options below(Type the option ID shown below) \n" +
				"1 Send Money\n" +
				"2 View Balance\n" +
				"3 View Notifications \n" +
				"4 View All Transaction Log File \n" +
				"5 Exit Session";
    }

    public String getClients(){
    	String retval = "";
    	for(Map.Entry m:clients_map.entrySet())
    	{   
    		 if (!m.getKey().equals(username))
    		 	retval = retval + m.getKey()+"\n";
    	}  
    	 
    	 return retval;
    	
    }

}
