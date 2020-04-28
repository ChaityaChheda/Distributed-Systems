# Distributed-Systems
Create distributed systems with socket programming. <br/>
Create multiple- client and server environment. <br/>
Create a log-file for each client, server maintains them. The log-file of respective client contains all the requests made by it along with time stamps and the status of request processing (Y/N). A client can make multiple requests to server. Client can ask it’s log file as one of the requests. But the client has to send the log file back after reading it. <br/>
Among all the processes there is a daemon process, which tries to corrupt log file. The server should be capable of tracing this information. After the request is processed the server can check immediately or at regular intervals of time for the following things and correct them:<br/>
    1. If the client corrupts the log file, the server should name the client as daemon and notify other clients. <br/>
    2. The system has to recover from this. <br/>
    3. Use any error recovery approaches to go back to the previous points. (Maintain a log for this) <br/>
    4. Daemon process should be blocked.<br/>
<br/>
# Instructions to execute from terminal/ commandline 
<br/>

 execute the following command from src directory: <br/>
	<br/>
	for starting up Server : java main.SocketServer<br/>
	for starting up Client : java main.SocketClient<br/>
