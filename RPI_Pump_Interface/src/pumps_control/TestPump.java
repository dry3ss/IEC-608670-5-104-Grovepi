/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Will
 */
package pumps_control;
import pumps_control.Pump;
import pumps_control.ThreePumpsMessageBuilder;
import pumps_control.ThreePumpsMessageInterface;
import pumps_control.ThreePumpsMessageInterpreter;
public class TestPump {
    
    public static void main(String[] args)
    {
	ThreePumpsMessageInterface.ThreePumpsStates desired_states= new ThreePumpsMessageInterface.ThreePumpsStates();
	desired_states.state_pump_1=Pump.PUMPSTATE.FORWARD;
	desired_states.state_pump_2=Pump.PUMPSTATE.OFF;
	desired_states.state_pump_3=Pump.PUMPSTATE.BACKWARDS;
	System.out.print( "desired:\n"+desired_states.toString());
	
        ThreePumpsMessageBuilder builder= new ThreePumpsMessageBuilder(); 
        builder.setMessagePumpState(ThreePumpsMessageBuilder.PUMP_NB.P1, Pump.PUMPSTATE.FORWARD);
        builder.setMessagePumpState(ThreePumpsMessageBuilder.PUMP_NB.P2, Pump.PUMPSTATE.OFF);
        builder.setMessagePumpState(ThreePumpsMessageBuilder.PUMP_NB.P3, Pump.PUMPSTATE.BACKWARDS);
        ThreePumpsMessageInterface.ThreePumpsStates c_states= builder.getCurrentStates();	
	System.out.print( "\nbuilt:\n"+c_states.toString());
	System.out.print( "\n=> equality (built, desired): "+c_states.equals(desired_states));
	
	builder.setMessagePumpState(desired_states);
        ThreePumpsMessageInterface.ThreePumpsStates c_states2= builder.getCurrentStates();	
	System.out.print( "\nbuilt:\n"+c_states2.toString());	
	System.out.print( "\n=> equality (built2, desired): "+c_states2.equals(desired_states));
        
	short msg=builder.getMessage();
	System.out.print("\n=> transformed into "+ msg);
	
	ThreePumpsMessageInterface.ThreePumpsStates interpreted_states=ThreePumpsMessageInterpreter.getStatesFromMsg(msg);
	System.out.print( "\ninterpreted:\n"+interpreted_states.toString());	
	System.out.print( "\n=> equality (interpreted, desired): "+interpreted_states.equals(desired_states));
	System.out.println( "\n=> equality (interpreted, built): "+interpreted_states.equals(c_states));	
	
	
	System.out.println( "Let's change the desired state");	
	desired_states.state_pump_1=Pump.PUMPSTATE.OFF;
	System.out.print( "\n=> equality (built, desired): "+c_states.equals(desired_states));	
	System.out.print( "\n=> equality (built2, desired): "+c_states2.equals(desired_states));
	System.out.print( "\n=> equality (interpreted, desired): "+interpreted_states.equals(desired_states));
	
	
        
    }
    
}
