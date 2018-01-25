/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rpi_one_binary_pump_iec_104;




/*
 * This file is part of j60870.
 * For more information visit http://www.openmuc.org
 *
 * You are free to use code of this sample file in any
 * way you like and without any restrictions.
 *
 */

import pumps_control.BinaryPump;
import pumps_control.BinaryPump.*;

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
import pumps_control.ThreeTernaryPumpsMessageBuilder;
import pumps_control.ThreeTernaryPumpsMessageInterface;
import pumps_control.ThreeTernaryPumpsMessageInterpreter;

public class ServerPump {

    static BinaryPump pump1;
    
    //states we will use to store values
    static protected BINARY_PUMP_STATE state=BINARY_PUMP_STATE.OFF;
    
     public class ServerListener implements ServerEventListener  {
      
      
         public class ConnectionListener implements ConnectionEventListener {
 
             private final Connection connection;
             private final int connectionId;
             private boolean is_to_be_shutdown_after_disconnection;
 
             public ConnectionListener(Connection connection, int connectionId) {
                 this.connection = connection;
                 this.connectionId = connectionId;
                 this.is_to_be_shutdown_after_disconnection=false;                 
             }
             
             /*
              * ASDU's informations received by the server 
              */            
             @Override
             public void newASdu(ASdu aSdu) {
                 try {
 
                     switch (aSdu.getTypeIdentification()) {
                     
                     // Single command => USED TO STOP PROPERLY THE SERVER in our case
                     case C_SC_NA_1:
                        connection.sendConfirmation(aSdu);
                        System.out.println("Got end server command. Will stop gracefully after next disconnection \n");
                        is_to_be_shutdown_after_disconnection=true;
                        break;                   
                         
                        
                        
                     // interrogation command
                     case C_IC_NA_1:
                        connection.sendConfirmation(aSdu);
                        state=pump1.getState();
			short status=state.getNumVal();
            
                         System.out.println("Got interrogation command. Will send pump state: "+ state.toString() +"\n");
 
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
                            //let's convert the received bytes into the state that our pumps should have now
                            state=BINARY_PUMP_STATE.getStateFromVal((short)scaledValue.getUnnormalizedValue());
                            System.out.println("Received pump request: " + state.toString()+"\n");
                            pump1.setState(state);
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
                 
                 if(is_to_be_shutdown_after_disconnection)                     
                 {
                     System.out.println("is_to_be_shutdown_after_disconnection was set, will stop the server now.");                     
                     System.exit(0);
                 }
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
 
     
    public static void shutdownHook()
    {
        try {
            pump1.stop(); } catch (Exception e)
        {
            System.out.println("ERROR: "+e.getMessage());
        }
        
        try {    BinaryPump.gpio.shutdown(); } catch (Exception e)
        {
            System.out.println("ERROR: "+e.getMessage());
        }
        try {    Thread.sleep(1000); } catch (Exception e)
        {
            System.out.println("ERROR: "+e.getMessage());
        }
       
        System.out.println("Shutdown hook ran!"); 
    }
    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {  
        try {
            
            // pump1 : HARDWARE NB: 16, 18 , 22
            // pump2 : HARDWARE NB: 23, 21 , 19
            // pump3 : HARDWARE NB: 15, 13 , 11
            
            
            
            pump1= new BinaryPump(RaspiPin.GPIO_04,RaspiPin.GPIO_06);
            pump1.stop();
            Runtime.getRuntime().addShutdownHook(new Thread()//graceful shutdown in case of CTRL-C  among others       
            {
                @Override
                public void run()
                {
                   shutdownHook();
                }
            });
           new ServerPump().start();
            Thread.sleep(3000);
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
