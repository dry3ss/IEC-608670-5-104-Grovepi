/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rpi_fulll_controller;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import javax.swing.JFrame;
import javax.swing.JPanel;


import org.openmuc.j60870.ClientConnectionBuilder;
import org.openmuc.j60870.Connection;
import org.openmuc.j60870.internal.cli.CliParameterBuilder;
import org.openmuc.j60870.internal.cli.IntCliParameter;
import org.openmuc.j60870.internal.cli.StringCliParameter;



/**
 *
 * @author Will
 */
public class Main {

    	
    private static StringCliParameter host_param_sensor;
    private static IntCliParameter port_param_sensor ;    
    private static IntCliParameter common_addr_param_sensor;
    private static volatile Connection connection_sensor;
    
    private static StringCliParameter host_param_actuator ;
    private static IntCliParameter port_param_actuator ;
    private static IntCliParameter common_addr_param_actuator;
    private static volatile Connection connection_actuator;
    
    
    private static StringCliParameter getNewHostParameter( String IP_address)
    {
	StringCliParameter target_host_param= new CliParameterBuilder("-h").buildStringParameter("host",IP_address );
	return target_host_param;
    }
    private static IntCliParameter getNewPortParameter(int port)
    {
	IntCliParameter target_port_param= new CliParameterBuilder("-p")
            .setDescription("The port to connect to.").buildIntParameter("port", port);
	return target_port_param;
    }
    private static IntCliParameter getNewCommonAddressParameter(int common_address)
    {
	IntCliParameter target_common_addr_param= new CliParameterBuilder("-ca")
            .setDescription("The address of the target station or the broad cast address.")
            .buildIntParameter("common_address", common_address);
	return target_common_addr_param;
    }
    
    private static Connection startConnection(final StringCliParameter target_host_param,final IntCliParameter target_port_param,final ControllerConnectionManager controller_manager,final String name_connection )
    {
	InetAddress target_address;
	Connection target_connection;
        try {
            target_address = InetAddress.getByName(target_host_param.getValue());
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: ("+name_connection+")" + target_host_param.getValue());
            return null;
        }

        ClientConnectionBuilder clientConnectionBuilder = new ClientConnectionBuilder(target_address)
                .setPort(target_port_param.getValue());
        
        //establish Connection 
        try {
            target_connection = clientConnectionBuilder.connect();
        } catch (IOException e) {
            System.out.println("Unable to connect to remote host ("+name_connection+"): " + target_host_param.getValue() + ".");
            return null;
        }
	
	
	//set the established conenction in the connectionManager
	controller_manager.setConnection(target_connection);
	
        //Starting data transfer
        try {
            target_connection.startDataTransfer(controller_manager, 5000);
        } catch (TimeoutException e2) {
            System.out.println("Starting data transfer commonAddrParamr timed out. Closing connection with  "+name_connection+" .");
            target_connection.close();
            return null;
        } catch (IOException e) {
            System.out.println("Connection with "+name_connection+" closed for the following reason: " + e.getMessage());
	    target_connection.close();
            return null;
        }
        System.out.println("successfully connected to  "+name_connection);
	return target_connection;
	
    }
    
    
    public static void main(String[] args) {
    
	
    //Graphic Interface
    	JFrame frame = new JFrame("Dashboard");
    	frame.setSize(500, 500);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setVisible(true);	
	
    
    //SENSOR
    
    	//IP Address initialization
	host_param_sensor=getNewHostParameter("192.168.1.46");
	port_param_sensor=getNewPortParameter(2404);
	common_addr_param_sensor=getNewCommonAddressParameter(1);
	
	//Starting connection
	String name_co_sensor="sensor";
	SensorControllerConnectionManager sccm=new SensorControllerConnectionManager(name_co_sensor,common_addr_param_sensor);
	
	boolean connection_with_sensor=false;
	
        if( null != (connection_sensor=startConnection(host_param_sensor,port_param_sensor,sccm,name_co_sensor)))
	{
	    connection_with_sensor=true;
	    JPanel panel_s = new JPanel();
	    sccm.addButtonsToPannel(panel_s);
	    frame.add(panel_s, BorderLayout.NORTH);
	    sccm.printWithName("Pannel added");
	}
	else
	{
	    sccm.printWithName("Pannel NOT added");
	}
	
	
	
        
	/*TODO:
	Add the second sensor
	*/
	
	
    //ACTUATOR
    
    	//IP Address initialization
	host_param_actuator=getNewHostParameter("192.168.1.30");
	port_param_actuator=getNewPortParameter(2404);
	common_addr_param_actuator=getNewCommonAddressParameter(1);
	
	//Starting connection
	String name_co_actuator="actuator";
	ActuatorControllerConnectionManager accm=new ActuatorControllerConnectionManager(name_co_sensor,common_addr_param_actuator);
        
	boolean connection_with_actuator=false;
	
	if( null != (connection_actuator=startConnection(host_param_actuator,port_param_actuator,accm,name_co_actuator)))
	{
	    connection_with_actuator=true;
	    JPanel panel_a = new JPanel();
	    accm.addButtonsToPannel(panel_a);	  
	    frame.add(panel_a, BorderLayout.SOUTH);  
	    accm.printWithName("Pannel added");
	}
	else
	{
	    accm.printWithName("Pannel NOT added");
	}
	
	SensorToActuatorAutomaticController saac=new SensorToActuatorAutomaticController(accm);
	
	if(connection_with_actuator && connection_with_sensor)
	{
	    saac.registerSCCM(sccm, 0);
	    //saac.startRegulatingLevel(20, 2);
	}
	
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
		//clean up all the callbacks
		saac.unregisterSCCMs();	
		
		//there shouldn't be any exception, but we dont want anything
		//to stop us from properly closing should it still happen
		try{
		    accm.sendCloseConnection();
		}catch (Exception e)
		{
		    System.out.println("Exception with accm.sendCloseConnection(): "+e.getMessage());
		}
		try{
		    sccm.sendCloseConnection();
		}catch (Exception e)
		{
		    System.out.println("Exception with  sccm.sendCloseConnection(): "+e.getMessage());
		}
		
            }
        });
	System.out.println("\n\nWaiting for commands");
	

    }
    
}
