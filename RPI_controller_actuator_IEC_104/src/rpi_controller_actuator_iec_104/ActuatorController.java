/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rpi_controller_actuator_iec_104;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.event.ActionListener;
import java.io.EOFException;

import org.openmuc.j60870.ASdu;
import org.openmuc.j60870.CauseOfTransmission;
import org.openmuc.j60870.ClientConnectionBuilder;
import org.openmuc.j60870.Connection;
import org.openmuc.j60870.ConnectionEventListener;
import org.openmuc.j60870.IeNormalizedValue;
import org.openmuc.j60870.IeQualifierOfInterrogation;
import org.openmuc.j60870.IeQualifierOfSetPointCommand;
import org.openmuc.j60870.IeScaledValue;
import org.openmuc.j60870.IeTime56;
import org.openmuc.j60870.internal.cli.CliParameter;
import org.openmuc.j60870.internal.cli.CliParameterBuilder;
import org.openmuc.j60870.internal.cli.CliParseException;
import org.openmuc.j60870.internal.cli.CliParser;
import org.openmuc.j60870.internal.cli.IntCliParameter;
import org.openmuc.j60870.internal.cli.StringCliParameter;
import pumps_control.TernaryPump;
import pumps_control.ThreeTernaryPumpsMessageBuilder;
import pumps_control.ThreeTernaryPumpsMessageInterface;
import pumps_control.ThreeTernaryPumpsMessageInterpreter;

   

/*
 * Copyright 2014-17 Fraunhofer ISE
 *
 * This file is part of j60870.
 * For more information visit http://www.openmuc.org
 *
 * j60870 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * j60870 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with j60870.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

public class ActuatorController {

	static JButton bforward, bbackward, binterrogation, bstop, bquit, bclock, bactibarrier;
	static JFrame frame;
	static JPanel panel_int, panel_motor;
	
	
    //bulder for the messages that have to be sent
    static protected ThreeTernaryPumpsMessageBuilder builder= new ThreeTernaryPumpsMessageBuilder();    
    //states we will use to store values
    static protected ThreeTernaryPumpsMessageInterface.ThreeTernaryPumpsStates states=new ThreeTernaryPumpsMessageInterface.ThreeTernaryPumpsStates();
    
    public static void sendPumpStatesCommand(ThreeTernaryPumpsMessageInterface.ThreeTernaryPumpsStates states) throws IOException
    {
	builder.setMessagePumpState(states);
	short command=builder.getMessage();
	connection.setScaledValueCommand(commonAddrParam.getValue(), 
	    CauseOfTransmission.ACTIVATION, 2, new IeScaledValue(command), 
	    new IeQualifierOfSetPointCommand(0,false)
	);

    }
	
	//Initialization of Host and Port parameters
    private static final StringCliParameter hostParam = new CliParameterBuilder("-h").buildStringParameter("host", "192.168.20.11");
    private static final IntCliParameter portParam = new CliParameterBuilder("-p")
            .setDescription("The port to connect to.").buildIntParameter("port", 2404);
    private static final IntCliParameter commonAddrParam = new CliParameterBuilder("-ca")
            .setDescription("The address of the target station or the broad cast address.")
            .buildIntParameter("common_address", 1);

    private static volatile Connection connection;

    //Listen the response given by the server
    private static class ClientEventListener implements ConnectionEventListener {	
	 
	
	
	
	
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
			states=ThreeTernaryPumpsMessageInterpreter.getStatesFromMsg((short)scaledValue.getUnnormalizedValue());
			System.out.println("Received pump states: " + states.toString());
			break;
			
		 // interrogation command 
		    case C_IC_NA_1:
			if (aSdu.getCauseOfTransmission()==CauseOfTransmission.ACTIVATION_CON)
			    System.out.println("Received confirmation of activation order");			
			else
			    do_default=true;
			break;
			

		    // Action command
		    case C_SE_NB_1://scaled command
			if (aSdu.getCauseOfTransmission()==CauseOfTransmission.ACTIVATION_CON)
			    System.out.println("Received confirmation of activation order");			
			else
			    do_default=true;
			break;

		     case C_CS_NA_1:
			if (aSdu.getCauseOfTransmission()==CauseOfTransmission.ACTIVATION_CON)
			    System.out.println("Received confirmation of activation order");			
			else
			    do_default=true;
			break;

		     default:
			do_default=true;
			break;
		 }
		 if (do_default)
		     System.out.println("Got unknown request: " + aSdu + "\n");
	     } catch (Exception e) {
		 System.out.println("Will quit listening for commands on connection because of error: \"" + e.getMessage() + "\".");
	     } 

	 }

        @Override
        public void connectionClosed(IOException e) {
            System.out.print("Received connection closed signal. Reason: ");
            if (!e.getMessage().isEmpty()) {
                System.out.println(e.getMessage());
            }
            else {
                System.out.println("unknown");
            }
            connection.close();
        }

    }

    public static void main(String[] args) {
    	
    	//IP Address initialization
        InetAddress address;
        try {
            address = InetAddress.getByName(hostParam.getValue());
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + hostParam.getValue());
            return;
        }

        ClientConnectionBuilder clientConnectionBuilder = new ClientConnectionBuilder(address)
                .setPort(portParam.getValue());
        
        //Connection established
        try {
            connection = clientConnectionBuilder.connect();
        } catch (IOException e) {
            System.out.println("Unable to connect to remote host: " + hostParam.getValue() + ".");
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                connection.close();
            }
        });

        //Starting data transfer
        try {
            connection.startDataTransfer(new ClientEventListener(), 5000);
        } catch (TimeoutException e2) {
            System.out.println("Starting data transfecommonAddrParamr timed out. Closing connection.");
            connection.close();
            return;
        } catch (IOException e) {
            System.out.println("Connection closed for the following reason: " + e.getMessage());
            return;
        }
        System.out.println("successfully connected");
        

        //Graphic Interface
    	JFrame frame = new JFrame("Dashboard");
    	frame.setSize(500, 500);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setVisible(true);	
    	
    	
    	//"Interrogation" Button (Implemented by Pr.Fraunhofer)
    	JButton binterrogation = new JButton("Interrogation");
    	binterrogation.addActionListener(new ActionListener(){
			public void actionPerformed(java.awt.event.ActionEvent e){
				System.out.println("** Sending general interrogation command **");
                try {
					connection.interrogation(commonAddrParam.getValue(), CauseOfTransmission.ACTIVATION,
					        new IeQualifierOfInterrogation(20));
					Thread.sleep(2000);
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
                
			}
		});
    	
    	//"Synchronization" Button (Implemented by Pr.Fraunhofer)
    	JButton bclock = new JButton("Synchronization");
    	bclock.addActionListener(new ActionListener(){
			public void actionPerformed(java.awt.event.ActionEvent e){
				 System.out.println("** Sending synchronize clocks command. ** ");
                    try {
						connection.synchronizeClocks(commonAddrParam.getValue(), new IeTime56(System.currentTimeMillis()));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
			}
		});
    	

    	//"FORWARD_ALL" Button
    	JButton f_all = new JButton("Forward_ALL");
    	f_all.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(java.awt.event.ActionEvent e){
            	try {			
			states=new ThreeTernaryPumpsMessageInterface.ThreeTernaryPumpsStates();
			states.state_pump_1=TernaryPump.TERNARY_PUMP_STATE.FORWARD;
			states.state_pump_2=TernaryPump.TERNARY_PUMP_STATE.FORWARD;
			states.state_pump_3=TernaryPump.TERNARY_PUMP_STATE.FORWARD;
			sendPumpStatesCommand(states);
			System.out.print("** Sending Motor command: "+states.toString()+" **\n");
		    } catch (IOException e1) 
		    { System.out.print(e1.getMessage());}
	    }
	});
    		
    	//"BACKWARDS ALL" Button
    	JButton b_all = new JButton("BACKWARDS ALL");
    	b_all.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(java.awt.event.ActionEvent e){
            	try {
			states=new ThreeTernaryPumpsMessageInterface.ThreeTernaryPumpsStates();
			states.state_pump_1=TernaryPump.TERNARY_PUMP_STATE.BACKWARDS;
			states.state_pump_2=TernaryPump.TERNARY_PUMP_STATE.BACKWARDS;
			states.state_pump_3=TernaryPump.TERNARY_PUMP_STATE.BACKWARDS;
			sendPumpStatesCommand(states);
			System.out.print("** Sending Motor command: "+states.toString()+" **\n");
		    } catch (IOException e1) 
		    { System.out.print(e1.getMessage());}
	    }
	});
	
	//"STOP ALL" Button
    	JButton s_all = new JButton("STOP ALL");
    	s_all.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(java.awt.event.ActionEvent e){
            	try {
			states=new ThreeTernaryPumpsMessageInterface.ThreeTernaryPumpsStates();
			states.state_pump_1=TernaryPump.TERNARY_PUMP_STATE.OFF;
			states.state_pump_2=TernaryPump.TERNARY_PUMP_STATE.OFF;
			states.state_pump_3=TernaryPump.TERNARY_PUMP_STATE.OFF;
			sendPumpStatesCommand(states);
			System.out.print("** Sending Motor command: "+states.toString()+" **\n");
		    } catch (IOException e1) 
		    { System.out.print(e1.getMessage());}
	    }
	});
	
	//"FORWARD__P1 only" Button
    	JButton f_1 = new JButton("Forward_P1 only");
    	f_1.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(java.awt.event.ActionEvent e){
            	try {
			states=new ThreeTernaryPumpsMessageInterface.ThreeTernaryPumpsStates();
			states.state_pump_1=TernaryPump.TERNARY_PUMP_STATE.FORWARD;
			states.state_pump_2=TernaryPump.TERNARY_PUMP_STATE.OFF;
			states.state_pump_3=TernaryPump.TERNARY_PUMP_STATE.OFF;
			sendPumpStatesCommand(states);
			System.out.print("** Sending Motor command: "+states.toString()+" **\n");
		    } catch (IOException e1) 
		    { System.out.print(e1.getMessage());}
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
			System.out.print("** Sending Motor command: "+states.toString()+" **\n");
		    } catch (IOException e1) 
		    { System.out.print(e1.getMessage());}
	    }
	});
	

    	//"Disconnection" Button
    	JButton bquit = new JButton("Disconnection");
    	bquit.addActionListener(new ActionListener(){
			public void actionPerformed(java.awt.event.ActionEvent e){
	            System.out.println("** Closing connection. **\n");
	            connection.close();
			}
		});
    	
    	
    	//Add buttons on the dashboard
    	JPanel panel_int = new JPanel();
    	frame.add(panel_int, BorderLayout.NORTH);
    	panel_int.add(binterrogation);
    	panel_int.add(bclock);
//    	panel_int.add(bactibarrier);    
    	
    	JPanel panel_motor = new JPanel();
    	frame.add(panel_motor, BorderLayout.CENTER);
    	panel_motor.add(f_all);
    	panel_motor.add(b_all);
	panel_motor.add(f_1);
    	panel_motor.add(b_1);
	panel_motor.add(s_all);
    	//panel_motor.add(bstop);
    	panel_motor.add(bquit);

    }

}


