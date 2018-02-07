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
import pumps_control.TernaryPump;
import pumps_control.ThreeTernaryPumpsMessageBuilder;
import pumps_control.ThreeTernaryPumpsMessageInterface;
import pumps_control.ThreeTernaryPumpsMessageInterpreter;
public class TestPump {
    
    public static void main(String[] args)
    {
	ThreeTernaryPumpsMessageInterface.ThreeTernaryPumpsStates desired_states= new ThreeTernaryPumpsMessageInterface.ThreeTernaryPumpsStates();
	desired_states.state_pump_1=TernaryPump.TERNARY_PUMP_STATE.FORWARD;
	desired_states.state_pump_2=TernaryPump.TERNARY_PUMP_STATE.OFF;
	desired_states.state_pump_3=TernaryPump.TERNARY_PUMP_STATE.BACKWARDS;
	System.out.print( "desired:\n"+desired_states.toString());
	
        ThreeTernaryPumpsMessageBuilder builder= new ThreeTernaryPumpsMessageBuilder(); 
        builder.setMessagePumpState(ThreeTernaryPumpsMessageBuilder.PUMP_NB.P1, TernaryPump.TERNARY_PUMP_STATE.FORWARD);
        builder.setMessagePumpState(ThreeTernaryPumpsMessageBuilder.PUMP_NB.P2, TernaryPump.TERNARY_PUMP_STATE.OFF);
        builder.setMessagePumpState(ThreeTernaryPumpsMessageBuilder.PUMP_NB.P3, TernaryPump.TERNARY_PUMP_STATE.BACKWARDS);
        ThreeTernaryPumpsMessageInterface.ThreeTernaryPumpsStates c_states= builder.getCurrentStates();	
	System.out.print( "\nbuilt:\n"+c_states.toString());
	System.out.print( "\n=> equality (built, desired): "+c_states.equals(desired_states));
	
	builder.setMessagePumpState(desired_states);
        ThreeTernaryPumpsMessageInterface.ThreeTernaryPumpsStates c_states2= builder.getCurrentStates();	
	System.out.print( "\nbuilt:\n"+c_states2.toString());	
	System.out.print( "\n=> equality (built2, desired): "+c_states2.equals(desired_states));
        
	short msg=builder.getMessage();
	System.out.print("\n=> transformed into "+ msg);
	
	ThreeTernaryPumpsMessageInterface.ThreeTernaryPumpsStates interpreted_states=ThreeTernaryPumpsMessageInterpreter.getStatesFromMsg(msg);
	System.out.print( "\ninterpreted:\n"+interpreted_states.toString());	
	System.out.print( "\n=> equality (interpreted, desired): "+interpreted_states.equals(desired_states));
	System.out.println( "\n=> equality (interpreted, built): "+interpreted_states.equals(c_states));	
	
	
	System.out.println( "Let's change the desired state");	
	desired_states.state_pump_1=TernaryPump.TERNARY_PUMP_STATE.OFF;
	System.out.print( "\n=> equality (built, desired): "+c_states.equals(desired_states));	
	System.out.print( "\n=> equality (built2, desired): "+c_states2.equals(desired_states));
	System.out.print( "\n=> equality (interpreted, desired): "+interpreted_states.equals(desired_states));
	
	
        
    }
    
}
