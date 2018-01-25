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
import org.openmuc.j60870.IeNormalizedValue;
import org.openmuc.j60870.IeQualifierOfSetPointCommand;
import org.openmuc.j60870.IeShortFloat;
import org.openmuc.j60870.internal.cli.IntCliParameter;


public class SensorControllerConnectionManager 
	extends ControllerConnectionManager
{

    protected int id_sensor_data_callback;
    protected SensorToActuatorAutomaticControllerInterface saac_for_callback;    
    protected JLabel current_level ;
    
    //CALLBACK MANAGEMENT
    
    public void registerSensorDataCallback(SensorToActuatorAutomaticControllerInterface new_saac,int new_id)
    {
	if(new_id <0 || new_saac ==null)
	    return;
	//if a callback was already present, we resign it
	resetAndResignSensorDataCallback();
	id_sensor_data_callback=new_id;
	saac_for_callback=new_saac;
    }
    
    public boolean hasValidCallback()
    {
	return saac_for_callback!=null && id_sensor_data_callback >=0;
    }
    
    public void resetAndResignSensorDataCallback()
    {
	//if a callback was already present, we resign it
	if(hasValidCallback())
	    saac_for_callback.resetSCCMPair(id_sensor_data_callback);
	saac_for_callback=null;
	id_sensor_data_callback=-1;	
    }
    
    public void callbackSaac(float value)
    {
	if (hasValidCallback())
	    saac_for_callback.setLatestSensorLevel(value, id_sensor_data_callback);		
    }
    
    
    public int getIdSensorDataCallback()
    {
        return id_sensor_data_callback;
    }
    
//CONSTRUCTOR
    public SensorControllerConnectionManager(IntCliParameter commonAddrParam_)
    {
	super("Sensor",commonAddrParam_);
	id_sensor_data_callback=-1;
	saac_for_callback=null;
        current_level= new JLabel("Current_level");
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

		 // received the answer to interrogation command (short float value)
		 case M_ME_NC_1:			
		    //value in which the activation flags are stored
		    IeShortFloat distance_received = (IeShortFloat) aSdu.getInformationObjects()[0].getInformationElements()[0][0];
		    //printWithName("Received measured distance:"+Float.toString(distance_received.getValue()));
		    callbackSaac(distance_received.getValue());
                    locked_logger.log(Float.toString(distance_received.getValue()));
                    current_level.setText("Current level: "+distance_received.getValue());
		    break;
                    
                case C_SC_NA_1:
                    if (aSdu.getCauseOfTransmission()==CauseOfTransmission.ACTIVATION_CON)
                        printWithName("Got end server command confirmation. Server will stop gracefully after next disconnection \n");	
		    else
			do_default=true;
                    break;

	     // interrogation command 
		case C_IC_NA_1:
		    if (aSdu.getCauseOfTransmission()==CauseOfTransmission.ACTIVATION_CON)
			//printWithName("Received confirmation of activation order:interrogation command ");
                        break;
		    else
			do_default=true;
		    break;


		// Action command
		case C_SE_NA_1://activation floating point command
		    if (aSdu.getCauseOfTransmission()==CauseOfTransmission.ACTIVATION_CON)
			//printWithName("Received confirmation of activation order:Action command");
                        break;			
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
    public void sendLedOff()
    {
	if(connection==null)
	    return;
        printNewLine();
	printWithName("** Sending 'LED OFF' command. **");
	try {
	connection.setNormalizedValueCommand(commonAddrParam.getValue(), 
		CauseOfTransmission.ACTIVATION, 2, new IeNormalizedValue(-1), 
		new IeQualifierOfSetPointCommand(0,false));
	} catch (IOException e1) {
		e1.printStackTrace();
	}
    }
    
    public void sendLedOn()
    {
	if(connection==null)
	    return;
        printNewLine();
	printWithName("** Sending 'LED ON' command. **");
	try {
	connection.setNormalizedValueCommand(commonAddrParam.getValue(), 
		CauseOfTransmission.ACTIVATION, 2, new IeNormalizedValue(1), 
		new IeQualifierOfSetPointCommand(0,false));
	} catch (IOException e1) {
		e1.printStackTrace();
	}
    }

    @Override
    public void sendCloseConnection()
    {
	sendLedOff();
	super.sendCloseConnection();	
    }
//INTERFACE HANDLING
    @Override
    public void addButtonsToPannel(JPanel panel)
    {	
    	panel.add(current_level);
        panel.add(Box.createHorizontalGlue());
	super.addButtonsToPannel(panel);
	
    	//"ON" Button
    	JButton b_on = new JButton("LED ON");
    	b_on.addActionListener(new ActionListener(){
	    public void actionPerformed(java.awt.event.ActionEvent e){
		sendLedOn();
	    }	
	});
    		
    	//"OFF" Button
    	JButton b_off = new JButton("LED OFF");
    	b_off.addActionListener(new ActionListener(){
	    public void actionPerformed(java.awt.event.ActionEvent e){
		sendLedOff();
	    }	
	});
	
    	
    	//Add buttons on the dashboard
	
    	panel.add(b_on);
	panel.add(b_off);
    }
    
    
}

