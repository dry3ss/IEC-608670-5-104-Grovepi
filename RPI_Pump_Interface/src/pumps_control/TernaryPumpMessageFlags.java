/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pumps_control;

/**
 *
 * @author Will
 */
public class TernaryPumpMessageFlags
    {
        protected boolean pump_enabled;
        protected boolean pump_forward;
        TernaryPumpMessageFlags()
        {
            pump_enabled=false;
            pump_forward=false;
        }
        
        public boolean isEnabled()
        {
            return pump_enabled;
        }
        public boolean isForward()
        {
            return pump_forward && pump_enabled ;
        }
        public boolean isBackwards()
        {
            return !pump_forward && pump_enabled ;
        }
        
        public void setState(TernaryPump.TERNARY_PUMP_STATE state)
        {
            switch (state)
            {
                case FORWARD:
                    pump_enabled=true;
                    pump_forward=true;
                    break;
                   
                case BACKWARDS:
                    pump_enabled=true;
                    pump_forward=false;
                    break;
                    
                default://UNKOWN or OFF gets set to OFF
                    pump_enabled=false;
                    pump_forward=false;
                    break;
            }
        }
        
        
        public TernaryPump.TERNARY_PUMP_STATE getState()
    {
        if(!isEnabled())
            return TernaryPump.TERNARY_PUMP_STATE.OFF;
        if(isForward())
            return TernaryPump.TERNARY_PUMP_STATE.FORWARD;
        else
            return TernaryPump.TERNARY_PUMP_STATE.BACKWARDS;
        //UNKNOWN state unrecognizable from here
    }
                
}
