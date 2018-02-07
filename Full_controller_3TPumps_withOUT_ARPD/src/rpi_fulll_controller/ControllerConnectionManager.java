/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rpi_fulll_controller;

import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.openmuc.j60870.CauseOfTransmission;
import org.openmuc.j60870.Connection;
import org.openmuc.j60870.ConnectionEventListener;
import org.openmuc.j60870.IeQualifierOfInterrogation;
import org.openmuc.j60870.IeSingleCommand;
import org.openmuc.j60870.IeTime56;
import org.openmuc.j60870.internal.cli.IntCliParameter;
import quick_logger.LockedLogger;

/**
 *
 * @author Will
 */
public abstract class ControllerConnectionManager 
	implements ConnectionEventListener {
    
    
    protected static int instance_counter=0;
    protected LockedLogger locked_logger;
    
    protected Connection connection;
    protected String name;
    protected IntCliParameter commonAddrParam;
    
    protected boolean silence;

    public boolean isSilence() {
        return silence;
    }

    public void setSilence(boolean silence) {
        this.silence = silence;
    }

    
    
    public ControllerConnectionManager(String name_,IntCliParameter commonAddrParam_)
    {
        instance_counter++;
	name=name_+"_"+instance_counter;
        locked_logger= new LockedLogger("/home/will/PFE/log",name);
	connection=null;
	commonAddrParam=commonAddrParam_;
        silence=false;
    }

    public Connection getConnection() {
	return connection;
    }

    public void setConnection(Connection connection) {
	this.connection = connection;
    }
    
    public void printNewLine()
    {
        if(! silence)
            System.out.println();
    }   
    
    public void printWithName(String msg)
    {
        if(! silence)
            System.out.println(name+"::"+msg);
    }
    
    //SEND
    public void sendSynchronizeClock()
    {
	
	if(connection==null)
	    return;
        printNewLine();
	printWithName("** Sending synchronize clocks command. ** ");
	try {
		    connection.synchronizeClocks(commonAddrParam.getValue(), new IeTime56(System.currentTimeMillis()));
	} catch (IOException e1) {
		    e1.printStackTrace();
	}	
    }
    
    public void sendInterrogationCommand()
    {
	
	if(connection==null)
	    return;
        printNewLine();
	printWithName("** Sending general interrogation command **");
	try {
		connection.interrogation(commonAddrParam.getValue(), CauseOfTransmission.ACTIVATION,
			new IeQualifierOfInterrogation(20));
	} catch (IOException e1) {
		e1.printStackTrace();
	}
    }
    
    public void sendCloseConnection()
    {
	if(connection==null)
	    return;
        printNewLine();
	printWithName("** Closing connection. **");
	connection.close();
        connection=null;
    }
    
    public void sendStopServer()
    {        
        if(connection==null)
	    return;
        printNewLine();
	printWithName("** Sending Shutdown server command **");
	try {
		connection.singleCommand(commonAddrParam.getValue(),
                        CauseOfTransmission.DEACTIVATION,
                        0, new IeSingleCommand(false,0,false));
	} catch (IOException e1) {
		e1.printStackTrace();
	}    
    }
    
    //INTERFACE 
    public void addButtonsToPannel(JPanel panel)
    {
        
	//"Interrogation" Button
    	JButton binterrogation = new JButton("Interrogation");
    	binterrogation.addActionListener(new ActionListener(){
	    public void actionPerformed(java.awt.event.ActionEvent e){
		sendInterrogationCommand();
	    }
	});
    	
    	//"Synchronization" Button 
    	JButton bclock = new JButton("Synchronization");
    	bclock.addActionListener(new ActionListener(){
	    public void actionPerformed(java.awt.event.ActionEvent e){
		sendSynchronizeClock();
	    }	
	});  
    	
	//"Disconnection" Button
    	JButton bquit = new JButton("Disconnection");
    	bquit.addActionListener(new ActionListener(){
	    public void actionPerformed(java.awt.event.ActionEvent e){
		sendCloseConnection();
	    }	
	});
        
        
        
        //"Stop server" Button
    	JButton bstop = new JButton("Stop server afterwards");
    	bstop.addActionListener(new ActionListener(){
	    public void actionPerformed(java.awt.event.ActionEvent e){
                sendStopServer();
	    }	
	}); 
    	
    	//Add buttons on the dashboard
        
    	panel.add(binterrogation);
    	panel.add(bclock);
	panel.add(bquit);
        panel.add(bstop);
    }

    public String getName() {
        return name;
    }
    
    
    
}
