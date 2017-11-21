
package rpi_sensors_iec_104;

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


import org.iot.raspberry.grovepi.GrovePi;
import org.iot.raspberry.grovepi.pi4j.GrovePi4J;

import org.iot.raspberry.grovepi.GroveDigitalIn;
import org.iot.raspberry.grovepi.GroveDigitalOut;
import org.iot.raspberry.grovepi.devices.GroveUltrasonicRanger;

import org.openmuc.j60870.ASdu;
import org.openmuc.j60870.CauseOfTransmission;
import org.openmuc.j60870.Connection;
import org.openmuc.j60870.ConnectionEventListener;
import org.openmuc.j60870.IeNormalizedValue;
import org.openmuc.j60870.IeQuality;
import org.openmuc.j60870.IeShortFloat;
import org.openmuc.j60870.InformationElement;
import org.openmuc.j60870.InformationObject;
import org.openmuc.j60870.Server;
import org.openmuc.j60870.ServerEventListener;
import org.openmuc.j60870.TypeId;

public class ServerSensor {

    static GrovePi grovePi;
    static GroveDigitalOut led ;//led on D4
    static GroveUltrasonicRanger ranger;//range on d3
 
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
                        double distance = ranger.get();
                        float sensor_dist_float=(float) distance;
            
                         connection.sendConfirmation(aSdu);
                         System.out.println("Got interrogation command. Will send measured distance value.\n");
 
                         connection.send(new ASdu(TypeId.M_ME_NC_1, true, CauseOfTransmission.SPONTANEOUS, false, false,
                                 0, aSdu.getCommonAddress(),
                                 new InformationObject[] { new InformationObject(1, new InformationElement[][] 
                                 {
                                         { new IeShortFloat(sensor_dist_float), new IeQuality(true, true, true, true, true) }
                                 }) }));
                        break;
                        
                    // Action command
                    case C_SE_NA_1:
                    	connection.sendConfirmation(aSdu);
                    	
                    	//Gets the Normalized Value and the Information Object Address
                    	IeNormalizedValue normalizedValue = (IeNormalizedValue) aSdu.getInformationObjects()[0].getInformationElements()[0][0];
                	int informationObjectAddress = aSdu.getInformationObjects()[0].getInformationObjectAddress();
                		
                        // led action
                        if(informationObjectAddress == 2){

                                // ON action
                                if (normalizedValue.getUnnormalizedValue() == 1){
                                        System.out.println("Got LED ON command");
                                        led.set(true);
                                }

                                // OFF action
                                if (normalizedValue.getUnnormalizedValue() == -1){
                                        System.out.println("Got LED OFF command");
                                        led.set(false);
                                }
                        } 
                    	break;
                      
                     // Clock command (Implemented by Pr.Fraunhofer) 
                     case C_CS_NA_1:
                      connection.sendConfirmation(aSdu);  
                      
                     default:
                         System.out.println("Got unknown request: " + aSdu + ". Will not confirm it.\n");
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
            grovePi= new GrovePi4J();      
            led = grovePi.getDigitalOut(4);//led on D4
            ranger = new GroveUltrasonicRanger(grovePi, 3);//range on d3
            Runtime.getRuntime().addShutdownHook(new Thread()//graceful shutdown in case of CTRL-C  among others       
            {
                @Override
                public void run()
                {
                   try {
                    led.set(false);
                    } catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                    }
                    System.out.println("Shutdown hook ran!");
                    
                    
                    
                    
                         
//                    int i=0;
//                    while (i<1000) {
//                        double distance = ranger.get();
//                        System.out.println("sensor value"+Double.toString(distance));
//                        led.set(true);
//                        Thread.sleep(500);
//                        led.set(false);
//                        Thread.sleep(500);
//                        i++;
//                    }        
//                    led.set(false);
                    
                }
            });
            new ServerSensor().start();
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
