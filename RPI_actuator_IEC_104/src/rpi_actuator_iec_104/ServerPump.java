
package rpi_actuator_iec_104;

import pumps_control.Pump;


/*
 * This file is part of j60870.
 * For more information visit http://www.openmuc.org
 *
 * You are free to use code of this sample file in any
 * way you like and without any restrictions.
 *
 */

import java.io.EOFException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.TimeoutException;


import com.pi4j.io.gpio.RaspiPin;

import org.openmuc.j60870.ASdu;
import org.openmuc.j60870.CauseOfTransmission;
import org.openmuc.j60870.Connection;
import org.openmuc.j60870.ConnectionEventListener;
import org.openmuc.j60870.IeNormalizedValue;
import org.openmuc.j60870.IeQuality;
import org.openmuc.j60870.IeScaledValue;
import org.openmuc.j60870.IeShortFloat;
import org.openmuc.j60870.InformationElement;
import org.openmuc.j60870.InformationObject;
import org.openmuc.j60870.Server;
import org.openmuc.j60870.ServerEventListener;
import org.openmuc.j60870.TypeId;
import pumps_control.ThreePumpsMessageBuilder;
import pumps_control.ThreePumpsMessageInterface;
import pumps_control.ThreePumpsMessageInterpreter;

public class ServerPump {

    static Pump pump1;
    static Pump pump2;
    static Pump pump3;
    
    //builder for the messages that have to be sent
    static protected ThreePumpsMessageBuilder builder= new ThreePumpsMessageBuilder();
    //states we will use to store values
    static protected ThreePumpsMessageInterface.ThreePumpsStates states=new ThreePumpsMessageInterface.ThreePumpsStates();
    
     public class ServerListener implements ServerEventListener  {
      
      
         public class ConnectionListener implements ConnectionEventListener {
 
             private final Connection connection;
             private final int connectionId;
 
             public ConnectionListener(Connection connection, int connectionId) {
                 this.connection = connection;
                 this.connectionId = connectionId;
             }
             
             /*
              * ASDU's informations received by the server 
              */            
             @Override
             public void newASdu(ASdu aSdu) {
                 try {
 
                     switch (aSdu.getTypeIdentification()) {
                     
                     // interrogation command
                     case C_IC_NA_1:
                        connection.sendConfirmation(aSdu);
			ThreePumpsMessageInterface.ThreePumpsStates states=new ThreePumpsMessageInterface.ThreePumpsStates();
			states.state_pump_1=pump1.getState();
			states.state_pump_2=pump2.getState();
			states.state_pump_3=pump3.getState();
			builder.setMessagePumpState(states);
			short status=builder.getMessage();
            
                         System.out.println("Got interrogation command. Will send pump states: "+ states.toString() +"\n");
 
                         connection.send(new ASdu(TypeId.M_ME_NB_1, true, CauseOfTransmission.REQUEST, false, false,
                                 0, aSdu.getCommonAddress(),
                                 new InformationObject[] { new InformationObject(1, new InformationElement[][] 
                                 {
                                         { new IeScaledValue(status), new IeQuality(false, false, false, false, false) }
                                 }) }));
                        break;
                        
                    // Action command
                    case C_SE_NB_1://scaled command
                    	connection.sendConfirmation(aSdu);
                    	
                    	//Information Object Address
                    	
                	int informationObjectAddress = aSdu.getInformationObjects()[0].getInformationObjectAddress();
                		
                        // pump_action
                        if(informationObjectAddress == 2){
                            //value in which the activation flags are stored
                            IeScaledValue scaledValue = (IeScaledValue) aSdu.getInformationObjects()[0].getInformationElements()[0][0];
                            //let's convert the received bytes into the states that our pumps should have now
                            states=ThreePumpsMessageInterpreter.getStatesFromMsg((short)scaledValue.getUnnormalizedValue());
                            System.out.println("Received pump request: " + states.toString()+"\n");
                            pump1.setState(states.state_pump_1);
			    pump2.setState(states.state_pump_2);
			    pump3.setState(states.state_pump_3);
                        } 
                    	break;
                      
                     // Clock command (Implemented by Pr.Fraunhofer) 
                     case C_CS_NA_1:
                      connection.sendConfirmation(aSdu);  
                    	break;
                      
                     default:
                         System.out.println("Got unknown request: " + aSdu + ". Will not confirm it.\n");
                    	break;
                     }
 
                 } catch (EOFException e) {
                     System.out.println("Will quit listening for commands on connection (" + connectionId
                             + ") because socket was closed.");
                 } catch (IOException e) {
                     System.out.println("Will quit listening for commands on connection (" + connectionId
                             + ") because of error: \"" + e.getMessage() + "\".");
                 } 
 
             }
 
             @Override
             public void connectionClosed(IOException e) {
                 System.out.println("Connection (" + connectionId + ") was closed. " + e.getMessage());
             }
 
         }
 
         //Give rights to the server (Waiting for the datas)
         @Override
         public void connectionIndication(Connection connection) {
 
             int myConnectionId = connectionIdCounter++;
             System.out.println("A client has connected using TCP/IP. Will listen for a StartDT request. Connection ID: "
                     + myConnectionId);
 
             try {
                 connection.waitForStartDT(new ConnectionListener(connection, myConnectionId), 5000);
             } catch (IOException e) {
                 System.out.println("Connection (" + myConnectionId + ") interrupted while waiting for StartDT: "
                         + e.getMessage() + ". Will quit.");
                 return;
             } catch (TimeoutException e) {
             }
 
             System.out.println(
                     "Started data transfer on connection (" + myConnectionId + ") Will listen for incoming commands.");
 
         }
 
         @Override
         public void serverStoppedListeningIndication(IOException e) {
             System.out.println(
                     "Server has stopped listening for new connections : \"" + e.getMessage() + "\". Will quit.");
         }
 
         @Override
         public void connectionAttemptFailed(IOException e) {
             System.out.println("Connection attempt failed: " + e.getMessage());
 
         }
 
     }
 
     private int connectionIdCounter = 1;
 
    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {  
        try {
            pump1= new Pump(RaspiPin.GPIO_16,RaspiPin.GPIO_18,RaspiPin.GPIO_22);
            pump2= new Pump(RaspiPin.GPIO_23,RaspiPin.GPIO_21,RaspiPin.GPIO_19);
            pump3= new Pump(RaspiPin.GPIO_15,RaspiPin.GPIO_13,RaspiPin.GPIO_11);
            Runtime.getRuntime().addShutdownHook(new Thread()//graceful shutdown in case of CTRL-C  among others       
            {
                @Override
                public void run()
                {
                   try {
                    pump1.stop();
                    pump2.stop();
                    pump3.stop();
                    } catch (Exception e)
                    {
                        System.out.println("ERROR: "+e.getMessage());
                    }
                    System.out.println("Shutdown hook ran!");  
                }
            });
            new ServerPump().start();
        } catch (Exception e)
        {
            System.out.println("Could not start Grovepi and ServerSensor:\n"+e.getMessage());
        }
     }
 
     public void start() {
         Server server = new Server.Builder().build();
         
         try {
             server.start(new ServerListener());
         } catch (IOException e) {
             System.out.println("Unable to start listening: \"" + e.getMessage() + "\". Will quit.");
             return;
         }
     }

}
