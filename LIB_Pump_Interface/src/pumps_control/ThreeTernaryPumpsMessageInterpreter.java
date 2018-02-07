/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pumps_control;

import pumps_control.TernaryPump.TERNARY_PUMP_STATE;
import pumps_control.ThreeTernaryPumpsMessageInterface.ThreeTernaryPumpsStates;

/**
 *
 * @author Will
 */




public class ThreeTernaryPumpsMessageInterpreter {

    
    public static ThreeTernaryPumpsStates getStatesFromMsg(short msg)
    {
        ThreeTernaryPumpsStates r= new ThreeTernaryPumpsStates();
        
        //PUMP 1
        //pump enabled
        if (ThreeTernaryPumpsMessageInterface.checkFlag(msg, ThreeTernaryPumpsMessageInterface.FLAG_ENABLE_PUMP_1))
        {
            //pump in forward movement
	    if (ThreeTernaryPumpsMessageInterface.checkFlag(msg, ThreeTernaryPumpsMessageInterface.FLAG_FORWARD_PUMP_1))
                r.state_pump_1=TERNARY_PUMP_STATE.FORWARD;
            else
                r.state_pump_1=TERNARY_PUMP_STATE.BACKWARDS;
        }
        else//actually OFF or UNKNOWN
            r.state_pump_1=TERNARY_PUMP_STATE.OFF;
        
        //PUMP 2 
        //pump enabled
        if (ThreeTernaryPumpsMessageInterface.checkFlag(msg, ThreeTernaryPumpsMessageInterface.FLAG_ENABLE_PUMP_2))
        {
            //pump in forward movement
            if (ThreeTernaryPumpsMessageInterface.checkFlag(msg, ThreeTernaryPumpsMessageInterface.FLAG_FORWARD_PUMP_2))
                r.state_pump_2=TERNARY_PUMP_STATE.FORWARD;
            else
                r.state_pump_2=TERNARY_PUMP_STATE.BACKWARDS;
        }
        else//actually OFF or UNKNOWN
            r.state_pump_2=TERNARY_PUMP_STATE.OFF;
        
        //PUMP 3
        //pump enabled
        if (ThreeTernaryPumpsMessageInterface.checkFlag(msg, ThreeTernaryPumpsMessageInterface.FLAG_ENABLE_PUMP_3))
        {
            //pump in forward movement
            if (ThreeTernaryPumpsMessageInterface.checkFlag(msg, ThreeTernaryPumpsMessageInterface.FLAG_FORWARD_PUMP_3))
                r.state_pump_3=TERNARY_PUMP_STATE.FORWARD;
            else
                r.state_pump_3=TERNARY_PUMP_STATE.BACKWARDS;
        }
        else//actually OFF or UNKNOWN
            r.state_pump_3=TERNARY_PUMP_STATE.OFF;
        
        return r;
    }
    
}
