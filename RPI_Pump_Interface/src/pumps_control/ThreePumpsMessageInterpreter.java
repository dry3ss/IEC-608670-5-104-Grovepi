/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pumps_control;

import pumps_control.Pump.PUMPSTATE;
import pumps_control.ThreePumpsMessageInterface.ThreePumpsStates;

/**
 *
 * @author Will
 */




public class ThreePumpsMessageInterpreter {

    
    public static ThreePumpsStates getStatesFromMsg(short msg)
    {
        ThreePumpsStates r= new ThreePumpsStates();
        
        //PUMP 1
        //pump enabled
        if (ThreePumpsMessageInterface.checkFlag(msg, ThreePumpsMessageInterface.FLAG_ENABLE_PUMP_1))
        {
            //pump in forward movement
	    if (ThreePumpsMessageInterface.checkFlag(msg, ThreePumpsMessageInterface.FLAG_FORWARD_PUMP_1))
                r.state_pump_1=PUMPSTATE.FORWARD;
            else
                r.state_pump_1=PUMPSTATE.BACKWARDS;
        }
        else//actually OFF or UNKNOWN
            r.state_pump_1=PUMPSTATE.OFF;
        
        //PUMP 2 
        //pump enabled
        if (ThreePumpsMessageInterface.checkFlag(msg, ThreePumpsMessageInterface.FLAG_ENABLE_PUMP_2))
        {
            //pump in forward movement
            if (ThreePumpsMessageInterface.checkFlag(msg, ThreePumpsMessageInterface.FLAG_FORWARD_PUMP_2))
                r.state_pump_2=PUMPSTATE.FORWARD;
            else
                r.state_pump_2=PUMPSTATE.BACKWARDS;
        }
        else//actually OFF or UNKNOWN
            r.state_pump_2=PUMPSTATE.OFF;
        
        //PUMP 3
        //pump enabled
        if (ThreePumpsMessageInterface.checkFlag(msg, ThreePumpsMessageInterface.FLAG_ENABLE_PUMP_3))
        {
            //pump in forward movement
            if (ThreePumpsMessageInterface.checkFlag(msg, ThreePumpsMessageInterface.FLAG_FORWARD_PUMP_3))
                r.state_pump_3=PUMPSTATE.FORWARD;
            else
                r.state_pump_3=PUMPSTATE.BACKWARDS;
        }
        else//actually OFF or UNKNOWN
            r.state_pump_3=PUMPSTATE.OFF;
        
        return r;
    }
    
}
