/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pumps_control;

import pumps_control.ThreeTernaryPumpsMessageInterface.ThreeTernaryPumpsStates;

/**
 *
 * @author Will
 */
public class ThreeTernaryPumpsMessageBuilder {

    TernaryPumpMessageFlags pump_msg_1;
    TernaryPumpMessageFlags pump_msg_2;
    TernaryPumpMessageFlags pump_msg_3;
    
    
    public ThreeTernaryPumpsMessageBuilder()
    {
	pump_msg_1=new TernaryPumpMessageFlags();
	pump_msg_2=new TernaryPumpMessageFlags();
	pump_msg_3=new TernaryPumpMessageFlags();
    }
    
    public enum PUMP_NB
    {
        P1,P2,P3;
    };
    
    public ThreeTernaryPumpsStates getCurrentStates()
    {
        ThreeTernaryPumpsStates r= new ThreeTernaryPumpsStates();
        r.state_pump_1=pump_msg_1.getState();
        r.state_pump_2=pump_msg_2.getState();
        r.state_pump_3=pump_msg_3.getState();
	return r;
    }
        
        
    public void setMessagePumpState(PUMP_NB nb,TernaryPump.TERNARY_PUMP_STATE state)
    {
        switch (nb){
            case P1:
                pump_msg_1.setState(state);
                break;
            case P2:
                pump_msg_2.setState(state);
                break;
            case P3:
                pump_msg_3.setState(state);
                break;
        }
    }
    public void setMessagePumpState(ThreeTernaryPumpsStates states)
    {
	pump_msg_1.setState(states.state_pump_1);
	pump_msg_2.setState(states.state_pump_2);
	pump_msg_3.setState(states.state_pump_3);        
    }   
    
    public short getMessage()
    {
        int r=0;
        if(pump_msg_1.isEnabled())
        {
            r = r  | ThreeTernaryPumpsMessageInterface.FLAG_ENABLE_PUMP_1;
            if (pump_msg_1.isForward())
                r=r | ThreeTernaryPumpsMessageInterface.FLAG_FORWARD_PUMP_1;  
        }
        if(pump_msg_2.isEnabled())
        {
            r = r  | ThreeTernaryPumpsMessageInterface.FLAG_ENABLE_PUMP_2;
            if (pump_msg_2.isForward())
                r=r | ThreeTernaryPumpsMessageInterface.FLAG_FORWARD_PUMP_2;  
        }
        if(pump_msg_3.isEnabled())
        {
            r = r  | ThreeTernaryPumpsMessageInterface.FLAG_ENABLE_PUMP_3;
            if (pump_msg_3.isForward())
                r=r | ThreeTernaryPumpsMessageInterface.FLAG_FORWARD_PUMP_3;  
        }          
        return (short)r;
    }
}
