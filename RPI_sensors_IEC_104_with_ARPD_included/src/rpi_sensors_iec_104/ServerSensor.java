
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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;


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
             private boolean is_to_be_shutdown_after_disconnection;
 
             public ConnectionListener(Connection connection, int connectionId) {
                 this.connection = connection;
                 this.connectionId = connectionId;
                 is_to_be_shutdown_after_disconnection=false;
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
                        double distance = ranger.get();
                        float sensor_dist_float=(float) distance;
            
                         connection.sendConfirmation(aSdu);
                         System.out.println("Got interrogation command. Will send measured distance value.\n");
 
                         connection.send(new ASdu(TypeId.M_ME_NC_1, true, CauseOfTransmission.REQUEST, false, false,
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
     
     
     
 
    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {  
        
	try {
            /*
                //To start an ARPD server at the same time add the following imports:
                    import arpdetox_lib.ARPDServer;
                    import static arpdetox_lib.ARPDServer.ARDP_SLAVE_PORT;
                    import arpdetox_lib.ARPDSlaveConsumerRunnable;
                    import arpdetox_lib.IPInfoContainers;
                    import arpdetox_lib.UDPServer;
            
                //Then uncomment the following at the start of the main():
                    //ARPD slave server start:
                    byte[] password= "lala".getBytes();    
                    int common_port_slaves=ARDP_SLAVE_PORT;

                    String my_addr=getMyTrueIP().getHostAddress();
                    System.out.println("starting ARPD server with ip: "+my_addr);
                    IPInfoContainers.SourceIPInfo s1_src_info= new IPInfoContainers.SourceIPInfo(my_addr,common_port_slaves);
                    IPInfoContainers.DestIPInfo s1_dst_info = new IPInfoContainers.DestIPInfo(my_addr,common_port_slaves);

                    ARPDSlaveConsumerRunnable cr1= new ARPDSlaveConsumerRunnable();            
                    ARPDServer.ARPDServerSlave s1= new ARPDServer.ARPDServerSlave(s1_src_info,cr1);

                    s1.setPasswd(password);
                    s1.start();            
                    System.out.println("ARPD slave server started");
            
                //Then surround what you just added with the necessary try-catch statements
            
                //Add the following function in the ServerPump class:
            
                    public static Inet4Address getMyTrueIP()
                    {
                        Inet4Address r=null;
                        try {

                            Enumeration<NetworkInterface> physical_network_interfaces = NetworkInterface.getNetworkInterfaces();
                            while (physical_network_interfaces.hasMoreElements())
                            {
                                NetworkInterface ni = physical_network_interfaces.nextElement();
                                List<InterfaceAddress> list_interface_address = ni.getInterfaceAddresses();
                                for(InterfaceAddress ia : list_interface_address)
                                {
                                    InetAddress potential_address=ia.getAddress();
                                    if(potential_address!=null && potential_address.getClass() == Inet4Address.class)
                                    {
                                        if(! potential_address.isLoopbackAddress() && ! potential_address.isLinkLocalAddress() )
                                            r= (Inet4Address) potential_address;
                                    }
                                }
                            }

                        } catch (SocketException ex) {
                            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        return r;
                    }
            
                //Finally add this inside the run() functions passed to addShutdownHook:
                    try {
                        s1.close();
                    } catch (InterruptedException | IOException ex) {
                        Logger.getLogger(ServerPump.class.getName()).log(Level.SEVERE, null, ex);
                    }
           
            
            */
            
            
            
            // IEC 104 sensor server start
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
                    } catch (IOException  e)
                    {
                        System.out.println(e.getMessage());
                    }
                    System.out.println("Shutdown hook ran!");
                }
            });
            new ServerSensor().start();
        } catch (IOException | InvalidParameterException e)
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
