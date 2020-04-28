package main;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Client implements java.io.Serializable{
	
	String cname;
	int balance;
	String log_of_transaction;
	String new_msg = "";
	boolean isBlocked;

	public Client(String cname,int balance,boolean isBlocked){
		this.cname = cname;
		this.balance = balance;
		this.isBlocked = isBlocked;
	}
	
	String getCname(){
		return cname;
	} 

	int getBalance(){
		return balance;
	}
	
	boolean getIsBlocked(){
		return isBlocked;
	}

	void blockUser(){

		isBlocked = true;
	}
	boolean debit(int amount,String user){

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

		String log;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		if (amount > this.balance){
			log = "Transaction unsuccessful insufficient funds"+" \n TimeStamp : "+ sdf.format(timestamp); 
			this.new_msg = this.new_msg + '-'+ log;
			this.log_of_transaction = log;
			return false;
		}
		else{
			this.balance = this.balance - amount;
			log ="Transaction Successful Transfered amount:" + amount + "  to "+user+" \n TimeStamp : "+ sdf.format(timestamp);
			this.new_msg = this.new_msg + '-'+ log;
			this.log_of_transaction =  log;
			return true;
		}
		
	}


	void credit(int amount,String user){
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

		this.balance = this.balance + amount;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		
		String log ="Transaction Successful received amount:" + amount + "  from  "+user+" \n TimeStamp : "+ sdf.format(timestamp);
		this.new_msg = this.new_msg + '-'+ log;
		this.log_of_transaction = log;
		
	}
	
	String checkNewMsg(){
		if(this.new_msg.equals("")){
			return "No new Transactions";
		}
		else{
			String temp = this.new_msg;
			this.new_msg = "";
			return temp;
			
		}
	}


	String getLog(){
		return this.log_of_transaction;
		
	}

}
