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
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openmuc.j60870.ASdu;
import org.openmuc.j60870.CauseOfTransmission;
import org.openmuc.j60870.IeQualifierOfSetPointCommand;
import org.openmuc.j60870.IeScaledValue;
import org.openmuc.j60870.internal.cli.IntCliParameter;
import pumps_control.TernaryPump;
import pumps_control.ThreeTernaryPumpsMessageBuilder;
import pumps_control.ThreeTernaryPumpsMessageInterface;
import pumps_control.ThreeTernaryPumpsMessageInterpreter;
import quick_logger.LockedLogger;


public class ActuatorControllerConnectionManager 
	extends ControllerConnectionManager
{
    
    //bulder for the messages that have to be sent
    protected ThreeTernaryPumpsMessageBuilder builder;    
    //states we will use to store values
    protected ThreeTernaryPumpsMessageInterface.ThreeTernaryPumpsStates states;
        
    protected JLabel text_latest_order_sent ;
    protected JLabel value_latest_order_sent ;
    
    protected JLabel text_latest_received_pumpstate ;
    protected JLabel value_latest_received_pumpstate ;

    public ActuatorControllerConnectionManager(IntCliParameter commonAddrParam_)
    {
	super("Pump",commonAddrParam_);
	builder= new ThreeTernaryPumpsMessageBuilder();    
	states=new ThreeTernaryPumpsMessageInterface.ThreeTernaryPumpsStates();
        text_latest_order_sent= new JLabel("Latest_order_sent:");
        value_latest_order_sent= new JLabel(states.toString());
        text_latest_received_pumpstate= new JLabel("Latest_received_pumpstate: ");
        value_latest_received_pumpstate= new JLabel("");
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
                 
                 
                 
                 
                    
                case C_SC_NA_1:
                    if (aSdu.getCauseOfTransmission()==CauseOfTransmission.ACTIVATION_CON)
                        printWithName("Got end server command confirmation. Server will stop gracefully after next disconnection \n");	
		    else
			do_default=true;
                    break;

		 // received the answer to interrogation command (scaled value)
		 case M_ME_NB_1:			
		    //value in which the activation flags are stored
		    IeScaledValue scaledValue = (IeScaledValue) aSdu.getInformationObjects()[0].getInformationElements()[0][0];
		    //let's convert the received bytes into the states that our pumps should have now
		    states=ThreeTernaryPumpsMessageInterpreter.getStatesFromMsg((short)scaledValue.getUnnormalizedValue());
		    printWithName("Received pump states: " + states.toString());
                    value_latest_received_pumpstate.setText(states.toString());
		    break;

	     // interrogation command 
		case C_IC_NA_1:
		    if (aSdu.getCauseOfTransmission()==CauseOfTransmission.ACTIVATION_CON)
			printWithName("Received confirmation of activation order: interrogation command ");			
		    else
			do_default=true;
		    break;


		// Action command
		case C_SE_NB_1://scaled command
		    if (aSdu.getCauseOfTransmission()==CauseOfTransmission.ACTIVATION_CON)
			printWithName("Received confirmation of activation order: Action command");			
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
	if(connection==null)
	    return;
        states.state_pump_1=TernaryPump.TERNARY_PUMP_STATE.OFF;
        states.state_pump_2=TernaryPump.TERNARY_PUMP_STATE.OFF;
        states.state_pump_3=TernaryPump.TERNARY_PUMP_STATE.OFF;	
        try {
		sendPumpStatesCommand(states);
	} catch (IOException e1) 
	{
	    printWithName(e1.getMessage());
	}
    }
    public void sendForwardAll()
    {
	if(connection==null)
	    return;
        states.state_pump_1=TernaryPump.TERNARY_PUMP_STATE.FORWARD;
        states.state_pump_2=TernaryPump.TERNARY_PUMP_STATE.FORWARD;
        states.state_pump_3=TernaryPump.TERNARY_PUMP_STATE.FORWARD;
        try {
		sendPumpStatesCommand(states);
	} catch (IOException e1) 
	{
	    printWithName(e1.getMessage());
	}
    }
    public void sendBackwardsAll()
    {
	if(connection==null)
	    return;
        states.state_pump_1=TernaryPump.TERNARY_PUMP_STATE.BACKWARDS;
        states.state_pump_2=TernaryPump.TERNARY_PUMP_STATE.BACKWARDS;
        states.state_pump_3=TernaryPump.TERNARY_PUMP_STATE.BACKWARDS;
        try {
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

    
    protected  void sendPumpStatesCommand(ThreeTernaryPumpsMessageInterface.ThreeTernaryPumpsStates states) throws IOException
    {
	if(connection==null)
	    return;
        locked_logger.log(states.toString());
	builder.setMessagePumpState(states);
	short command=builder.getMessage();
	connection.setScaledValueCommand(commonAddrParam.getValue(), 
	    CauseOfTransmission.ACTIVATION, 2, new IeScaledValue(command), 
	    new IeQualifierOfSetPointCommand(0,false)
	);
        printNewLine();
	printWithName("** Sending Motor command: "+states.toString()+" **\n");
        value_latest_order_sent.setText(states.toString());
    }
    
//INTERFACE HANDLING
    @Override
    public void addButtonsToPannel(JPanel panel)
    {	
        panel.add(text_latest_order_sent);
        panel.add(value_latest_order_sent);
        panel.add(text_latest_received_pumpstate);
        panel.add(value_latest_received_pumpstate);
        
        
        panel.add(Box.createGlue());
	super.addButtonsToPannel(panel);	
        panel.add(Box.createGlue());
    	
	//"FORWARD_ALL" Button
    	JButton f_all = new JButton("Forward_ALL");
    	f_all.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(java.awt.event.ActionEvent e){
            	try {
			states.state_pump_1=TernaryPump.TERNARY_PUMP_STATE.FORWARD;
			states.state_pump_2=TernaryPump.TERNARY_PUMP_STATE.FORWARD;
			states.state_pump_3=TernaryPump.TERNARY_PUMP_STATE.FORWARD;
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
			states.state_pump_1=TernaryPump.TERNARY_PUMP_STATE.BACKWARDS;
			states.state_pump_2=TernaryPump.TERNARY_PUMP_STATE.BACKWARDS;
			states.state_pump_3=TernaryPump.TERNARY_PUMP_STATE.BACKWARDS;
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
			states.state_pump_1=TernaryPump.TERNARY_PUMP_STATE.FORWARD;
			states.state_pump_2=TernaryPump.TERNARY_PUMP_STATE.OFF;
			states.state_pump_3=TernaryPump.TERNARY_PUMP_STATE.OFF;
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
			states=new ThreeTernaryPumpsMessageInterface.ThreeTernaryPumpsStates();
			states.state_pump_1=TernaryPump.TERNARY_PUMP_STATE.BACKWARDS;
			states.state_pump_2=TernaryPump.TERNARY_PUMP_STATE.OFF;
			states.state_pump_3=TernaryPump.TERNARY_PUMP_STATE.OFF;
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

