import java.net.*;
import java.io.*;

class rkChatServer extends Thread {
    public static final int MAX_USERS = 32;
    public static final int DEFAULT_PORT = 7232; //it spells rafk on a phone :P
    static ServerSocket serverSocket;
    static Socket clientSocket;
    
    //t is an array of threads
    static Client t[] = new Client[MAX_USERS];
    
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        if(args.length < 0 || args.length > 1) { //invalid args
            System.out.println("Usage: program_name <port>");
            System.exit(1);
        }
        else if(args.length == 0) { //use default port
            System.out.println("No port specified, using default port ("+DEFAULT_PORT+")");
        }
        else if(args.length == 1) { //use specified port
            port = Integer.parseInt(args[0]);
        }
        
        //create server socket
        try {
            serverSocket = new ServerSocket(port);
        }
        catch(IOException e) {
            System.err.println(e);
            System.exit(1);
        }
        
        //Accepts clients, then puts them into t[]
        //and starts a new thread with their info.
        while(true) {
            try{    
                clientSocket = serverSocket.accept();
                for(int n = 0; n < MAX_USERS; n++) {
                    if(t[n] == null) { //if t[n] is empty, start a new thread.
                        (t[n] = new Client(clientSocket,t)).start();
                        break;
                    } 
                }
            }
            catch(IOException e) {
                System.err.println(e);
                System.exit(1);
            }
        }
    }
}

class Client extends Thread {
    Socket clientSocket;       
    Client t[]; 
    public PrintStream writer; //writes to socket
    public BufferedReader listener; //receivers from socket
    public String name = "";
    
    public Client(Socket s,Client[] t) {
        this.clientSocket = s;
        this.t = t;
    }
    
    @Override
    public void run() {
        String userInput;     
        
        try {
            this.listener = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
	    this.writer = new PrintStream(clientSocket.getOutputStream());
            this.name = this.listener.readLine(); //initialize name
        }
        catch(IOException e) {
            System.err.println(e);
            System.exit(1);
        }
        
        this.writer.println("Hello "+this.name+"! Welcome to our server!");
        
                
        while(true) { //listen and echo
            try {
                userInput = this.listener.readLine();
                if(userInput.startsWith("/quit")) {
                    break;
                }   
                else if(userInput.startsWith("/serverlist")) {
                    this.writer.println(this.getServerList());
                }
                else {
                    this.sendToEveryoneButThis(name +": " + userInput);
                }
            }
            catch(IOException e) {
                break;
            }
        } //end listen and echo
        
        //person has /quit
        this.sendToEveryoneButThis(name + " is leaving the chat");
        
        //assign quitter to null
        //this allows someone to fill that spot
        for(int n = 0; n < rkChatServer.MAX_USERS;n++) {
            if(t[n] == this) {
                cleanUp();
                t[n] = null;  
            }
        }       
    }
    
    public void sendToEveryone(String s) {
        for(int n = 0; n < rkChatServer.MAX_USERS; n++) {
            if(t[n] != null) {
                this.t[n].writer.println(s);
            }
        }
    }
    
    public void sendToEveryoneButThis(String s) {
        for(int n = 0; n < rkChatServer.MAX_USERS; n++) {
            if(t[n] != null && t[n] != this) {
                this.t[n].writer.println(s);
            }
        }
    }
    
    public String getServerList() {
        String s;
        s = "***SERVER LIST***\n";
        for(int n = 0;n < rkChatServer.MAX_USERS; n++) {
            if(t[n] != null && t[n] != this) {
                s += this.t[n].name;
                s += "\n";
            }
        }
        return s;
    }
    
    public void cleanUp() {
        try {
            this.clientSocket.close();            
            this.writer.close();
            this.listener.close();
        }
        catch(IOException e) {
            System.err.println(e);
            System.exit(1);
        }
    }
}