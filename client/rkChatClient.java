import java.net.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
 * The rkChatClient class contains all of the connection logic.
 */
class rkChatClient implements Runnable {
    static final String VERSION = "1.0";   
    static final int DEFAULT_PORT = 7232; //it spells rafk on a phone :P
    static int port = DEFAULT_PORT;    
    static String host = "localhost";
    
    static Boolean isConnected = false;
    
    static Socket socket;
    static PrintStream writer; //writes to socket
    static BufferedReader listener; //receivers from socket
  
    static String name = "";
    static rkChatClientGUI gui;
    
    public static void main(String args[]) {      
        gui = new rkChatClientGUI();
        gui.displayWelcomeMessage();
    }
    
    public static void joinServer() {
        gui.addToTextArea("Connecting to "+host+":"+port+"...");
        if(name.equals("")) {
            gui.showErrorMessage("You must specify a name before connecting to a server");
        }
        else if(isConnected == true) {
            gui.showErrorMessage("You are already connected to a server!");
        }
        else {
            try{
                socket = new Socket(host,port);
                writer = new PrintStream(socket.getOutputStream());
                listener = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                isConnected = true;
                writer.println(name);
                gui.addToTextArea("Successfully connected to "+host);
            }
            catch(UnknownHostException e) {
                gui.addToTextArea("Error: Can not connect to the server at <"+host+">\nUnknown host");
            }
            catch(IOException e) {
                gui.addToTextArea("Error: Can not connect to the server at <"+host+">");
            }
        
            if(isConnected == true) {
                new Thread(new rkChatClient()).start();
            }
        }
    }
    
    //call this on "/quit" or when leave server is selected from the menu
    public static void leaveServer() { 
        if(isConnected == true) {
            try{
                isConnected = false;
                writer.println("/quit");
                socket.close();
                writer.close();
                listener.close();
                gui.addToTextArea("You have left the server <"+host+">");
            }
            catch(IOException e) {
                System.err.println(e);
            }
        }
        else {
            gui.showErrorMessage("You are not currently connected to a server. Unable to quit");
        }
    }
    
    public static void exitProgram() {
        if(isConnected == true) {
            try{
                isConnected = false;
                writer.println("/quit");
                socket.close();
                writer.close();
                listener.close();
                gui.addToTextArea("You have left the server <"+host+">");
            }
            catch(IOException e) {
                System.err.println(e);
            }
        }
        gui.addToTextArea("Now exiting the program...");
        System.exit(1);
    }
    
    //This method checks if a /command was used
    //then sends the data to the appropriate handler
    public static void parseText() {
        String userInput;
        userInput = gui.userInputField.getText();
        gui.userInputField.setText("");
        
        if(userInput.startsWith("/quit")) {
            leaveServer();
        }
        else {
            if(isConnected == true) {
                userInput = userInput.replace("/me",name); //for /me commanda
                sendMessageToServer(userInput);
            }
            else {
                gui.showErrorMessage("You cannot send a message without being connected to a server!");
            }
        }
    }
    
    //just a plain text message with "You: " appended to the beginning
    public static void sendMessageToServer(String userInput) {     
        gui.addToTextArea("You: "+userInput);
        writer.println(userInput);
    }
    
    //this method is used when you don't want to display the input
    //on textArea
    //
    //This method should be used for /commands like /serverlist
    public static void sendDataToServer(String userInput) {
        writer.println(userInput); 
    }
    
    //this thread listens to the server
    //it prints the whatever it recieves
    //to JTextArea
    @Override
    public void run() {
        String input;
        try {
            while(isConnected == true) { //listens
                input = listener.readLine();
                gui.addToTextArea(input); //print to JTextArea
            }
        }
        catch(IOException e) {
            //nothing to do...
        }
    }
}

class rkChatClientGUI implements ActionListener {
    public JFrame frame = new JFrame("rk Chat" + rkChatClient.VERSION);
    public JTextArea textArea = new JTextArea(5,30);
    public JTextField userInputField = new JTextField(20);
    
    public rkChatClientGUI() {   
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setPreferredSize(new Dimension(400,500));
        
        try {
	    // Set System L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } 
        catch (UnsupportedLookAndFeelException e) {System.err.println(e);}
        catch (ClassNotFoundException e) {System.err.println(e);}
        catch (InstantiationException e) {System.err.println(e);}
        catch (IllegalAccessException e) {System.err.println(e);}
        
        this.createMenu();
        
        this.textArea.setEditable(false);//uneditable text
        JScrollPane scrollPane = new JScrollPane(this.textArea);//text area is in a scroll pane
        
        this.userInputField.addActionListener(this);
        
        JButton sendButton = new JButton("Send");
        sendButton.setActionCommand("/sendbutton");
        sendButton.addActionListener(this);
        
        JPanel outputPanel = new JPanel();
        outputPanel.add(scrollPane);
        outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.PAGE_AXIS));
        outputPanel.add(Box.createRigidArea(new Dimension(0,5)));
        
        JPanel inputPanel = new JPanel();
        inputPanel.add(userInputField);
        inputPanel.add(sendButton);
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.LINE_AXIS));
        
        //add everything to JFrame
        Container contentPane = this.frame.getContentPane();
        contentPane.add(outputPanel,BorderLayout.CENTER);
        contentPane.add(inputPanel,BorderLayout.PAGE_END);
        this.frame.pack();
        this.frame.setVisible(true);
    }
    
    public void createMenu() {
        JMenuBar menuBar = new JMenuBar(); //menu bar
        
        //SERVER MENU
        JMenu serverMenu = new JMenu("Server");
        serverMenu.setMnemonic(KeyEvent.VK_S);
        menuBar.add(serverMenu);
        
        //connection
        JMenuItem hostIPMenuItem = new JMenuItem("Host info",KeyEvent.VK_H);
        hostIPMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,ActionEvent.ALT_MASK));
        serverMenu.add(hostIPMenuItem);
        hostIPMenuItem.setActionCommand("/hostinfo");
        hostIPMenuItem.addActionListener(this);
        
        //get name
        JMenuItem nameMenuItem = new JMenuItem("Set name",KeyEvent.VK_N);
        nameMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,ActionEvent.ALT_MASK));
        serverMenu.add(nameMenuItem);
        nameMenuItem.setActionCommand("/setname");
        nameMenuItem.addActionListener(this);
        
        //join server
        JMenuItem joinServerMenuItem = new JMenuItem("Join server",KeyEvent.VK_J);
        joinServerMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J,ActionEvent.ALT_MASK));
        serverMenu.add(joinServerMenuItem);
        joinServerMenuItem.setActionCommand("/join");
        joinServerMenuItem.addActionListener(this);
        
        //leave server
        JMenuItem leaveServerMenuItem = new JMenuItem("Leave server",KeyEvent.VK_L);
        leaveServerMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,ActionEvent.ALT_MASK));
        serverMenu.add(leaveServerMenuItem);
        leaveServerMenuItem.setActionCommand("/leave");
        leaveServerMenuItem.addActionListener(this);
        
        //quit program
        JMenuItem quitProgramMenuItem = new JMenuItem("Quit program",KeyEvent.VK_Q);
        quitProgramMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,ActionEvent.ALT_MASK));
        serverMenu.add(quitProgramMenuItem);
        quitProgramMenuItem.setActionCommand("/quitprogram");
        quitProgramMenuItem.addActionListener(this);
        
        //ABOUT MENU
        JMenu aboutMenu = new JMenu("About");
        aboutMenu.setMnemonic(KeyEvent.VK_A);
        menuBar.add(aboutMenu); //adds about to the menu
        
        //about rkChat
        JMenuItem aboutrkChatMenuItem = new JMenuItem("About rkChat",KeyEvent.VK_R);
        aboutrkChatMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,ActionEvent.ALT_MASK));
        aboutMenu.add(aboutrkChatMenuItem);
        aboutrkChatMenuItem.setActionCommand("/aboutrk");
        aboutrkChatMenuItem.addActionListener(this);
        
        //about devs
        JMenuItem aboutDevsMenuItem = new JMenuItem("About the developer",KeyEvent.VK_D);
        aboutDevsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,ActionEvent.ALT_MASK));
        aboutMenu.add(aboutDevsMenuItem);
        aboutDevsMenuItem.setActionCommand("/aboutdevs");
        aboutDevsMenuItem.addActionListener(this);
        this.frame.setJMenuBar(menuBar);
    }
    
    //This method is called whenever there is an event
    //the action command sent by whatever generated
    //the signal is parsed, and then the appropriate
    //action is taken.
    @Override
    public void actionPerformed(ActionEvent e) {
        if("derp".equals(e.getActionCommand())) {
            this.addToTextArea("Button pressed");
        }
                
        else if("/hostinfo".equals(e.getActionCommand())) {
            this.showServerDialogAndSetValues();
        }
                
        else if("/setname".equals(e.getActionCommand())) {
            this.showNameDialogAndSetValue();
        }     
                
        else if("/join".equals(e.getActionCommand())) {
            rkChatClient.joinServer();
        }
                
        else if("/leave".equals(e.getActionCommand())) {
            rkChatClient.leaveServer();
        }
                
        else if("/quitprogram".equals(e.getActionCommand())) {
            rkChatClient.exitProgram();
        }
                
        else if("/aboutrk".equals(e.getActionCommand())) {
            this.aboutRkChat();
        }
                
        else if("/aboutdevs".equals(e.getActionCommand())) {
            this.aboutDevs();
        }
                
        else if("/sendbutton".equals(e.getActionCommand())) {
            //finds out if the text is a command
            //if not, sends it to the server
            rkChatClient.parseText();
        }
    }
    
    public void addToTextArea(String s) {
        this.textArea.append(s + "\n");
        this.textArea.setCaretPosition(this.textArea.getDocument().getLength());
    }  
    
    public void showServerDialogAndSetValues() {
        String[] sa; //string array
        String s = (String)JOptionPane.showInputDialog(
                            this.frame,
                            "<host IP>:<port>\nYou can set <port> to DEFAULT_PORT to use the default port",
                            "rkChat Server Settings",
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            null,
                            rkChatClient.host + ":" + rkChatClient.port
                            );
        if(s != null && (s.length() > 0)) {
            sa = s.split(":");
            rkChatClient.host = sa[0];
            if(sa[1].equals("DEFAULT_PORT")) {
                rkChatClient.port = rkChatClient.DEFAULT_PORT;
            }
            else {
                rkChatClient.port = Integer.parseInt(sa[1]);
            }
        }
        this.addToTextArea("Host: "+rkChatClient.host);
        this.addToTextArea("Port: "+rkChatClient.port);
    }
    
    public void showNameDialogAndSetValue() {
        String s = (String)JOptionPane.showInputDialog(
                            this.frame,
                            "What's your name? ",
                            "rkChat name selection",
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            null,
                            "Dumb ass fat guy"
                            );
        if(s != null && (s.length() > 0)) {
            if(rkChatClient.isConnected == false) {
                rkChatClient.name = s;
                this.addToTextArea("Your new name is: "+rkChatClient.name);
            }
            else { 
                this.showErrorMessage("You cannot change your name while connected to a server!");
            }
        }
    }
    
    public void aboutRkChat() {
        String msg;
        msg = "VERSION: "+rkChatClient.VERSION+"\n"+
                "rkChat is an open source chat client, and server program. \n"+
                "This is released under the GNU GPL license. (See the README.html" +
                " that came with this program)";
        JOptionPane.showMessageDialog(this.frame,msg,"About rkChat",JOptionPane.INFORMATION_MESSAGE);
    }
    
    //This is the best method in the entire program
    public void aboutDevs() {
        String msg;
        msg = "rkChat was entirely developed by Rafael Khan\nMy contact info is included in README.html";
        JOptionPane.showMessageDialog(this.frame,msg,"About the rkChat dev",JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void showErrorMessage(String s) {
        JOptionPane.showMessageDialog(this.frame,s,"Error",JOptionPane.ERROR_MESSAGE);
    }
    
    public void displayWelcomeMessage() {
        this.addToTextArea("Welcome to rkChat "+rkChatClient.VERSION+"!");
        this.addToTextArea("To get started, click on \"Host info\" in the main menu\nThen select \"Join server\"");
    }
}