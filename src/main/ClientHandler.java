package main;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
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
			username = received.toLowerCase();

			try {
				File file = new File("serverFiles/"+username+"-server");
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
				System.out.println(username + " has logged in to the system at " + sdf.format(timestamp));

				sent = "Hello " + username + "\nWelcome back.\n";
				BufferedWriter writer1 = new BufferedWriter(
                	new FileWriter("serverFiles/"+username+"-server", true)  //Set true for append mode
		        );
				writer1.newLine();   //Add new line
				writer1.write("User logged in at " + sdf.format(timestamp));
				writer1.close();
			}
			else
			{
				client = new Client(username,1000, false);
				clients_map.put(username, client);	
				System.out.println(username + " has signed up into the system at " + sdf.format(timestamp));

				sent = "Hello " + username + "\nInitiated your Bank Account.\n";
				//File file = new File(username+"-server");
				BufferedWriter writer1 = new BufferedWriter(
                	new FileWriter("serverFiles/"+username+"-server", true)  //Set true for append mode
		        );
				writer1.newLine();   //Add new line
				writer1.write("User signed up at " + sdf.format(timestamp));
				writer1.close();

				writer1 = new BufferedWriter(
                	new FileWriter("checkpoints/"+username+"-checkpoint", true)  //Set true for append mode
		        );
				writer1.newLine();   //Add new line
				writer1.write("User signed up at " + sdf.format(timestamp));
				writer1.close();	
			}
				
			
			
			sent = sent + getuserPrompt();
			dos.writeUTF(sent);
			
			while(true){
				
				received = dis.readUTF();

				System.out.println("Client Status "+client.getIsBlocked());
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

					if(!clients_map.containsKey(queries[0])){
						sent = "Sorry but Client named "+ queries[0] +"does not exist\n\n";
						sent = sent + getuserPrompt();
					}

					else if(clients_map.get(queries[0]).getIsBlocked()){
						sent = "Sorry but "+ queries[0] +" has been blocked. You cannot perform any Transaction with this Client\n\n";
						sent = sent + getuserPrompt();			

					}
					else{

						executeTransaction(username, queries[0],Integer.parseInt(queries[1]),"Normal");
						sent = "Transaction Complete.\n";
						sent = sent + getuserPrompt();
					}

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
							new FileWriter("serverFiles/"+username+"-server", true)  //Set true for append mode
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

					File f2 = new File("serverFiles/"+username+"-server");
					System.out.print(f2.length());
					
					
					dos.writeUTF(Long.toString(f2.length()));
					received = dis.readUTF();
					System.out.print(received);
					
					util.sendLogFile("serverFiles/"+username+"-server",dos);
					received =  dis.readUTF();
					System.out.println(received);
					sent = "View "+ username +"-client File in the src directory. \nType \"send\" to transfer back the LogFile \nWarning: Any changes detected in the File will cause you to be blocked by the System !";
					dos.writeUTF(sent);
					
					
					received =  dis.readUTF();
					dos.writeUTF("received file size"+received);
					
	
					util.saveFile(username+"-temp", dis, Integer.parseInt(received));

					
					
					if(!util.Compare2Files("serverFiles/"+username +"-server", username +"-temp"))
					{
						System.out.println("Files are unequal");
						//block user 
						client.blockUser();
						clients_map.put(username, client);
						dos.writeUTF("\nDetected Corruption in Log Files, Dear " + username +", you have now been blocked!!");

						//roll back all actions since last check point
						//<<

						BufferedReader reader1 = new BufferedReader(new FileReader("checkpoints/"+username+"-checkpoint"));

						List<String> lines =  new ArrayList<String>(); 
						String line1 = reader1.readLine();


						System.out.println("line "+line1);
         
						while(line1 != null){

							lines.add(line1);
							line1 = reader1.readLine();
						}


						for(int i=lines.size()-1;i>=0;i--){
							System.out.println("line "+lines.get(i));

							String[] words = lines.get(i).split("\\s+");
							if( words[0].equals("Transaction")){

								String[] amt = words[3].split(":");
								int amount = Integer.parseInt(amt[1]);
								String receiver = words[5];
								System.out.println("Reverting Transaction");
								if(words[2].equals("Transfered")){

									executeTransaction(receiver,username,amount,"ErrorRecovery");
								}
								else if( words[2].equals("received")  ){
									executeTransaction(username,receiver, amount,"ErrorRecovery");
								}

							}

						}

						System.out.println("ErrorRecovery done successfully");
						
						for(Map.Entry m:clients_map.entrySet())
				    	{   
				    		if (!m.getKey().equals(username)){
				    			clients_map.get(m.getKey()).Notify(username + " has been blocked. ");
				    		}

				    	} 
					}
					else
					{
						System.out.println("No Corruption found in File.");
						sent = "Received the File, Thank You!  \n\n";
						//erase checkpoint 
						BufferedWriter writer1 = new BufferedWriter(
				                new FileWriter("checkpoints/"+username+"-checkpoint")
				        );
						writer1.newLine();   //Add new line
						writer1.write("");
						writer1.close();
						sent = sent + getuserPrompt();
						dos.writeUTF(sent);
					}
					File f = new File(username+"-temp");
        			f.delete();
					
				}
				else if(received.equals("5"))
				{
					boolean isWritten = util.writeMaptoFile(clients_map);
					logUserOut();
					this.dis.close(); 
			        this.dos.close(); 
			        break;
		         
				}
			}
			
		}
		catch(IOException e)
		{ 
			boolean isWritten = util.writeMaptoFile(clients_map);
	        // System.out.println("exception occured here" + isWritten); 
	        logUserOut();
	    } 
    }


    private synchronized void executeTransaction(String sender,String receiver,int amount,String label) throws IOException{
    	
    	if(clients_map.get(sender).debit(amount, receiver,label))
    	{
    		clients_map.get(receiver).credit(amount, sender,label);
    		clients_map.put(sender, clients_map.get(sender));
    	}
    	
    	
    	
    	BufferedWriter writer1 = new BufferedWriter(
                new FileWriter("serverFiles/"+sender+"-server", true)  //Set true for append mode
        );
		writer1.newLine();   //Add new line
		writer1.write(clients_map.get(sender).getLog());
		writer1.close();

		writer1 = new BufferedWriter(
                new FileWriter("checkpoints/"+sender+"-checkpoint", true)  //Set true for append mode
        );
		writer1.newLine();   //Add new line
		writer1.write(clients_map.get(sender).getLog());
		writer1.close();
		
		// System.out.println("Logs"+clients_map.get(username).getLog());
		
		// System.out.println("Logs"+clients_map.get(receiver).getLog());
		
		BufferedWriter  writer = new BufferedWriter(
                new FileWriter("serverFiles/"+receiver+"-server", true)  //Set true for append mode
            );  
		writer.newLine();   //Add new line
		writer.write(clients_map.get(receiver).getLog());
		
		writer.close();

		writer = new BufferedWriter(
                new FileWriter("checkpoints/"+receiver+"-checkpoint", true)  //Set true for append mode
            );  
		writer.newLine();   //Add new line
		writer.write(clients_map.get(receiver).getLog());
		
		writer.close();
    	
    	
    }


    String getMsgs(){

    	String[] msgs = clients_map.get(username).checkNewMsg().split("-");
    	// System.out.println(clients_map.get(username).checkNewMsg());
    	System.out.println(username + " - Notifications : ");
    	String retval="\n";
    	int i;
    	// msg[0] is always blank that is why started  with 1.
    	for(i =1;i<msgs.length;i++){
    		System.out.print("--->" + msgs[i] +"\n");
    		retval = retval + msgs[i] +"\n";
    	}
    	// System.out.println("\t" + retval);
    	return retval;
    }
    
    
    
    
    public String getuserPrompt()
    {
    	return "\nChoose from the options below(Type the option ID shown below) \n" +
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
    		if (!m.getKey().equals(username)){
    		 	if(clients_map.get(m.getKey()).getIsBlocked())
    		 		retval = retval + m.getKey()+"-Blocked \n";
    		 	else
    		 		retval = retval + m.getKey()+"\n";
    		 }
    	}  
    	 
    	 return retval;
    	
    }

    public void logUserOut()
    {
    	if(username != null)
    	{
	    	timestamp = new Timestamp(System.currentTimeMillis());
	        System.out.println("\n" + username + " has logged out of the system at " + sdf.format(timestamp));

	        try{
		        BufferedWriter writer1 = new BufferedWriter(
	            	new FileWriter("serverFiles/"+username+"-server", true)  //Set true for append mode
		        );
				writer1.newLine();   //Add new line
				writer1.write("User logged out at " + sdf.format(timestamp));
				writer1.close();

			} 
			catch(IOException ioe)
			{
				System.out.println("Exception Caught in logUserOut() : " + ioe.toString());
			}
		}
    }

}