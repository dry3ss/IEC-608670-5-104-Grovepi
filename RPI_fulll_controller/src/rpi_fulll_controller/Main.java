/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rpi_fulll_controller;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


import org.openmuc.j60870.ClientConnectionBuilder;
import org.openmuc.j60870.Connection;
import org.openmuc.j60870.internal.cli.CliParameterBuilder;
import org.openmuc.j60870.internal.cli.IntCliParameter;
import org.openmuc.j60870.internal.cli.StringCliParameter;
import quick_logger.LockedLogger;



/**
 *
 * @author Will
 */
public class Main {

    
    
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
    
    private static Connection startConnection(final StringCliParameter target_host_param,final IntCliParameter target_port_param,final ControllerConnectionManager controller_manager)
    {
        final String name_connection=controller_manager.getName();
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
        
        
        /*
    LockedLogger ll= new LockedLogger("/home/will/PFE/log","essai");
    ll.log("lala");
    ll.log("lolo");
    ll.log("lolo");
    ll.log("lolo");
    ll.log("lolo");
    ll.log("lolo");
    ll.log("lolo");
    ll.log("lili");
    ll.log("lolo");
    if(true)
        System.exit(0);
        */
    	
    StringCliParameter host_param_sensor;
    IntCliParameter port_param_sensor ;    
    IntCliParameter common_addr_param_sensor;
    Connection connection_sensor;
    
    StringCliParameter host_param_sensor2;
    IntCliParameter port_param_sensor2;   
    IntCliParameter common_addr_param_sensor2;
    Connection connection_sensor2;
    
    StringCliParameter host_param_actuator ;
    IntCliParameter port_param_actuator ;
    IntCliParameter common_addr_param_actuator;
    Connection connection_actuator;
    
    
    //Graphical Interface
    	JFrame frame = new JFrame("Dashboard");
    	frame.setSize(500, 500);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setVisible(true);	
	
    
    //SENSOR
    
    	//IP Address initialization
	host_param_sensor=getNewHostParameter("192.168.10.11");
	port_param_sensor=getNewPortParameter(2404);
	common_addr_param_sensor=getNewCommonAddressParameter(1);
	
	//Starting connection
	SensorControllerConnectionManager sccm=new SensorControllerConnectionManager(common_addr_param_sensor);
	
	boolean connection_with_sensor=false;
	
        if( null != (connection_sensor=startConnection(host_param_sensor,port_param_sensor,sccm)))
	{
	    connection_with_sensor=true;
	    JPanel panel_s = new JPanel();
            panel_s.setLayout(new GridLayout(6, 1));
	    sccm.addButtonsToPannel(panel_s);
            
            TitledBorder border = new TitledBorder("Sensor 1");
            border.setTitleJustification(TitledBorder.CENTER);
            border.setTitlePosition(TitledBorder.TOP);

            panel_s.setBorder(border); 
	    frame.add(panel_s, BorderLayout.WEST);
	    sccm.printWithName("Pannel added");
	}
	else
	{
	    sccm.printWithName("Pannel NOT added");
	}
	
	
    //SENSOR 2
        
        //IP Address initialization
	host_param_sensor2=getNewHostParameter("192.168.110.11");
	port_param_sensor2=getNewPortParameter(2404);
	common_addr_param_sensor2=getNewCommonAddressParameter(1);
	
	//Starting connection
	SensorControllerConnectionManager sccm2=new SensorControllerConnectionManager(common_addr_param_sensor2);
	
	boolean connection_with_sensor2=false;
	
        if( null != (connection_sensor=startConnection(host_param_sensor2,port_param_sensor2,sccm2)))
	{
	    connection_with_sensor2=true;
	    JPanel panel_s2 = new JPanel();
            panel_s2.setLayout(new GridLayout(6, 1));
	    sccm2.addButtonsToPannel(panel_s2);
            
            TitledBorder border = new TitledBorder("Sensor 2");
            border.setTitleJustification(TitledBorder.CENTER);
            border.setTitlePosition(TitledBorder.TOP);

            panel_s2.setBorder(border); 
	    frame.add(panel_s2, BorderLayout.EAST);
	    sccm2.printWithName("Pannel added");
	}
	else
	{
	    sccm2.printWithName("Pannel NOT added");
	}
	
	
    //ACTUATOR
    
    	//IP Address initialization
	host_param_actuator=getNewHostParameter("192.168.20.11");
	port_param_actuator=getNewPortParameter(2404);
	common_addr_param_actuator=getNewCommonAddressParameter(1);
	
	//Starting connectio
	String name_co_actuator="actuator";
	ActuatorControllerConnectionManager accm=new ActuatorControllerConnectionManager(common_addr_param_actuator);
        
	boolean connection_with_actuator=false;
	
	if( null != (connection_actuator=startConnection(host_param_actuator,port_param_actuator,accm)))
	{
	    connection_with_actuator=true;
	    JPanel panel_a = new JPanel();            
            panel_a.setLayout(new GridLayout(3, 3));
	    accm.addButtonsToPannel(panel_a);
            
            TitledBorder border = new TitledBorder("Actuator");
            border.setTitleJustification(TitledBorder.CENTER);
            border.setTitlePosition(TitledBorder.TOP);

            panel_a.setBorder(border);            
	    frame.add(panel_a, BorderLayout.SOUTH);  
	    accm.printWithName("Pannel added");
	}
	else
	{
	    accm.printWithName("Pannel NOT added");
	}
        
    // SensorToActuatorAutomaticController
    
	//create it and link it with the ActuatorControllerConnectionManager
	SensorToActuatorAutomaticController saac=new SensorToActuatorAutomaticController(accm);
	
	if(connection_with_actuator)
	{
            boolean ok_for_pannel=false;
            if(connection_with_sensor)
            {
                //register the first sensor controller with it
                saac.registerSCCM(sccm, 0); 
                ok_for_pannel=true;
            }
            if(connection_with_sensor2)
            {
                //register the second sensor controller with it
                saac.registerSCCM(sccm2, 1); 
                ok_for_pannel=true;            
            }
            
            //if one of the sensors is online
            if (ok_for_pannel)
            {
                JPanel panel__m = new JPanel();
                saac.addButtonsToPannel(panel__m);
                frame.add(panel__m, BorderLayout.CENTER); 
            }
	}
	
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
		//clean up all the callbacks
                saac.unregisterSCCMs();	
		
		//there shouldn't be any exception, but we dont want anything
		//to stop us from properly closing should it still happen
		    accm.sendCloseConnection();
		    sccm.sendCloseConnection();
		    sccm2.sendCloseConnection();		
            }
        });
        
        frame.pack();
        frame.setVisible(true);
	System.out.println("\n\nWaiting for commands");
	

    }
    
}
