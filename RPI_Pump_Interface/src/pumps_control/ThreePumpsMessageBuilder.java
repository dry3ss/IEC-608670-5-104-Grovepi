/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pumps_control;

import pumps_control.ThreePumpsMessageInterface.ThreePumpsStates;

/**
 *
 * @author Will
 */
public class ThreePumpsMessageBuilder {

    PumpMessageFlags pump_msg_1;
    PumpMessageFlags pump_msg_2;
    PumpMessageFlags pump_msg_3;
    
    
    public ThreePumpsMessageBuilder()
    {
	pump_msg_1=new PumpMessageFlags();
	pump_msg_2=new PumpMessageFlags();
	pump_msg_3=new PumpMessageFlags();
    }
    
    public enum PUMP_NB
    {
        P1,P2,P3;
    };
    
    public ThreePumpsStates getCurrentStates()
    {
        ThreePumpsStates r= new ThreePumpsStates();
        r.state_pump_1=pump_msg_1.getState();
        r.state_pump_2=pump_msg_2.getState();
        r.state_pump_3=pump_msg_3.getState();
	return r;
    }
        
        
    public void setMessagePumpState(PUMP_NB nb,Pump.PUMPSTATE state)
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
    public void setMessagePumpState(ThreePumpsStates states)
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
            r = r  | ThreePumpsMessageInterface.FLAG_ENABLE_PUMP_1;
            if (pump_msg_1.isForward())
                r=r | ThreePumpsMessageInterface.FLAG_FORWARD_PUMP_1;  
        }
        if(pump_msg_2.isEnabled())
        {
            r = r  | ThreePumpsMessageInterface.FLAG_ENABLE_PUMP_2;
            if (pump_msg_2.isForward())
                r=r | ThreePumpsMessageInterface.FLAG_FORWARD_PUMP_2;  
        }
        if(pump_msg_3.isEnabled())
        {
            r = r  | ThreePumpsMessageInterface.FLAG_ENABLE_PUMP_3;
            if (pump_msg_3.isForward())
                r=r | ThreePumpsMessageInterface.FLAG_FORWARD_PUMP_3;  
        }          
        return (short)r;
    }
}
