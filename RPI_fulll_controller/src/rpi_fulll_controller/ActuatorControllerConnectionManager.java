/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rpi_fulll_controller;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.openmuc.j60870.ASdu;
import org.openmuc.j60870.CauseOfTransmission;
import org.openmuc.j60870.IeQualifierOfSetPointCommand;
import org.openmuc.j60870.IeScaledValue;
import org.openmuc.j60870.internal.cli.IntCliParameter;
import pumps_control.Pump;
import pumps_control.ThreePumpsMessageBuilder;
import pumps_control.ThreePumpsMessageInterface;
import pumps_control.ThreePumpsMessageInterpreter;


public class ActuatorControllerConnectionManager 
	extends ControllerConnectionManager{
    
    //bulder for the messages that have to be sent
    protected ThreePumpsMessageBuilder builder;    
    //states we will use to store values
    protected ThreePumpsMessageInterface.ThreePumpsStates states;

    public ActuatorControllerConnectionManager(String name_,IntCliParameter commonAddrParam_)
    {
	super( name_,commonAddrParam_);
	builder= new ThreePumpsMessageBuilder();    
	states=new ThreePumpsMessageInterface.ThreePumpsStates();
    }
    
    
//LISTENING
     /*
	  * ASDU's informations received by the server 
	  */            
     @Override
     public void newASdu(ASdu aSdu) {
	 try {
	     boolean do_default=false;
	     switch (aSdu.getTypeIdentification()) {

		 // received the answer to interrogation command (scaled value)
		 case M_ME_NB_1:			
		    //value in which the activation flags are stored
		    IeScaledValue scaledValue = (IeScaledValue) aSdu.getInformationObjects()[0].getInformationElements()[0][0];
		    //let's convert the received bytes into the states that our pumps should have now
		    states=ThreePumpsMessageInterpreter.getStatesFromMsg((short)scaledValue.getUnnormalizedValue());
		    printWithName("Received pump states: " + states.toString());
		    break;

	     // interrogation command 
		case C_IC_NA_1:
		    if (aSdu.getCauseOfTransmission()==CauseOfTransmission.ACTIVATION_CON)
			printWithName("Received confirmation of activation order");			
		    else
			do_default=true;
		    break;


		// Action command
		case C_SE_NB_1://scaled command
		    if (aSdu.getCauseOfTransmission()==CauseOfTransmission.ACTIVATION_CON)
			printWithName("Received confirmation of activation order");			
		    else
			do_default=true;
		    break;

		 case C_CS_NA_1:
		    if (aSdu.getCauseOfTransmission()==CauseOfTransmission.ACTIVATION_CON)
			printWithName("Received confirmation of activation order");			
		    else
			do_default=true;
		    break;

		 default:
		    do_default=true;
		    break;
	     }
	     if (do_default)
		 printWithName("Got unknown request: " + aSdu + "\n");
	 } catch (Exception e) {
	     printWithName("Will quit listening for commands on connection because of error: \"" + e.getMessage() + "\".");
	 } 

     }

    @Override
    public void connectionClosed(IOException e) {
	printWithName("Received connection closed signal. Reason: ");
	if (!e.getMessage().isEmpty()) {
	    printWithName(e.getMessage());
	}
	else {
	    printWithName("unknown");
	}
	connection.close();
    }
    
    
//SENDING
    public void sendStopAll()
    {
	if(connection!=null)
	    return;
	printWithName("** Sending 'PUMPS OFF' command. **");	
        try {
		states=new ThreePumpsMessageInterface.ThreePumpsStates();
		states.state_pump_1=Pump.PUMPSTATE.OFF;
		states.state_pump_2=Pump.PUMPSTATE.OFF;
		states.state_pump_3=Pump.PUMPSTATE.OFF;
		sendPumpStatesCommand(states);
	} catch (IOException e1) 
	{
	    printWithName(e1.getMessage());
	}
    }
    public void sendForwardAll()
    {
	if(connection!=null)
	    return;
	printWithName("** Sending 'PUMPS OFF' command. **");	
        try {
		states=new ThreePumpsMessageInterface.ThreePumpsStates();
		states.state_pump_1=Pump.PUMPSTATE.FORWARD;
		states.state_pump_2=Pump.PUMPSTATE.FORWARD;
		states.state_pump_3=Pump.PUMPSTATE.FORWARD;
		sendPumpStatesCommand(states);
	} catch (IOException e1) 
	{
	    printWithName(e1.getMessage());
	}
    }
    public void sendBackwardsAll()
    {
	if(connection!=null)
	    return;
	printWithName("** Sending 'PUMPS OFF' command. **");	
        try {
		states=new ThreePumpsMessageInterface.ThreePumpsStates();
		states.state_pump_1=Pump.PUMPSTATE.BACKWARDS;
		states.state_pump_2=Pump.PUMPSTATE.BACKWARDS;
		states.state_pump_3=Pump.PUMPSTATE.BACKWARDS;
		sendPumpStatesCommand(states);
	} catch (IOException e1) 
	{
	    printWithName(e1.getMessage());
	}
    }
    
    @Override
    public void sendCloseConnection()
    {
	sendStopAll();
	super.sendCloseConnection();	
    }

    
    protected  void sendPumpStatesCommand(ThreePumpsMessageInterface.ThreePumpsStates states) throws IOException
    {
	if(connection!=null)
	    return;
	builder.setMessagePumpState(states);
	short command=builder.getMessage();
	connection.setScaledValueCommand(commonAddrParam.getValue(), 
	    CauseOfTransmission.ACTIVATION, 2, new IeScaledValue(command), 
	    new IeQualifierOfSetPointCommand(0,false)
	);
	printWithName("** Sending Motor command: "+states.toString()+" **\n");

    }
    
//INTERFACE HANDLING
    @Override
    public void addButtonsToPannel(JPanel panel)
    {	
	super.addButtonsToPannel(panel);
	
    	
	//"FORWARD_ALL" Button
    	JButton f_all = new JButton("Forward_ALL");
    	f_all.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(java.awt.event.ActionEvent e){
            	try {			
			states=new ThreePumpsMessageInterface.ThreePumpsStates();
			states.state_pump_1=Pump.PUMPSTATE.FORWARD;
			states.state_pump_2=Pump.PUMPSTATE.FORWARD;
			states.state_pump_3=Pump.PUMPSTATE.FORWARD;
			sendPumpStatesCommand(states);
		    } catch (IOException e1) 
		    { printWithName(e1.getMessage());}
	    }
	});
    		
    	//"BACKWARDS ALL" Button
    	JButton b_all = new JButton("BACKWARDS ALL");
    	b_all.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(java.awt.event.ActionEvent e){
            	try {
			states=new ThreePumpsMessageInterface.ThreePumpsStates();
			states.state_pump_1=Pump.PUMPSTATE.BACKWARDS;
			states.state_pump_2=Pump.PUMPSTATE.BACKWARDS;
			states.state_pump_3=Pump.PUMPSTATE.BACKWARDS;
			sendPumpStatesCommand(states);
		    } catch (IOException e1) 
		    { printWithName(e1.getMessage());}
	    }
	});
	
	//"STOP ALL" Button
    	JButton s_all = new JButton("STOP ALL");
    	s_all.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(java.awt.event.ActionEvent e){
		sendStopAll();
	    }
	});
	
	//"FORWARD__P1 only" Button
    	JButton f_1 = new JButton("Forward_P1 only");
    	f_1.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(java.awt.event.ActionEvent e){
            	try {
			states=new ThreePumpsMessageInterface.ThreePumpsStates();
			states.state_pump_1=Pump.PUMPSTATE.FORWARD;
			states.state_pump_2=Pump.PUMPSTATE.OFF;
			states.state_pump_3=Pump.PUMPSTATE.OFF;
			sendPumpStatesCommand(states);
		    } catch (IOException e1) 
		    { printWithName(e1.getMessage());}
	    }
	});
    		
    	//"BACKWARDS P1 only" Button
    	JButton b_1 = new JButton("BACKWARDS P1 only");
    	b_1.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(java.awt.event.ActionEvent e){
            	try {
			states=new ThreePumpsMessageInterface.ThreePumpsStates();
			states.state_pump_1=Pump.PUMPSTATE.BACKWARDS;
			states.state_pump_2=Pump.PUMPSTATE.OFF;
			states.state_pump_3=Pump.PUMPSTATE.OFF;
			sendPumpStatesCommand(states);
		    } catch (IOException e1) 
		    { printWithName(e1.getMessage());}
	    }
	});
	
	
    	//Add buttons on the dashboard
	
    	panel.add(f_all);
	panel.add(b_all);
	panel.add(s_all);
	panel.add(f_1);
	panel.add(b_1);
    }
    
}

