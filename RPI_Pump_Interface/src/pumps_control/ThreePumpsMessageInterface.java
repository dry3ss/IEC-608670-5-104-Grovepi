/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pumps_control;

import java.util.Objects;


/**
 *
 * @author Will
 */
public class ThreePumpsMessageInterface {
    
    public static final short FLAG_ENABLE_PUMP_1 = 1;    // Binary 00000001
    public static final short FLAG_ENABLE_PUMP_2 = 2;    // Binary 00000010
    public static final short FLAG_ENABLE_PUMP_3 = 4;    // Binary 00000100
    public static final short FLAG_FORWARD_PUMP_1 = 32;  // Binary 00100000
    public static final short FLAG_FORWARD_PUMP_2 = 64;  // Binary 01000000
    public static final short FLAG_FORWARD_PUMP_3 = 128; // Binary 10000000 
    
    public static boolean checkFlag(short msg, short flag)
    {
	return (msg & flag) == flag;
    }
    
    public static class ThreePumpsStates
    {
        public Pump.PUMPSTATE state_pump_1;
        public Pump.PUMPSTATE state_pump_2;
        public Pump.PUMPSTATE state_pump_3; 

	@Override
	public int hashCode() {
	    int hash = 7;
	    hash = 89 * hash + Objects.hashCode(this.state_pump_1);
	    hash = 89 * hash + Objects.hashCode(this.state_pump_2);
	    hash = 89 * hash + Objects.hashCode(this.state_pump_3);
	    return hash;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj) {
		return true;
	    }
	    if (obj == null) {
		return false;
	    }
	    if (getClass() != obj.getClass()) {
		return false;
	    }
	    final ThreePumpsStates other = (ThreePumpsStates) obj;
	    if (this.state_pump_1 != other.state_pump_1) {
		return false;
	    }
	    if (this.state_pump_2 != other.state_pump_2) {
		return false;
	    }
	    if (this.state_pump_3 != other.state_pump_3) {
		return false;
	    }
	    return true;
	}

	public ThreePumpsStates()
	{
	    state_pump_1=Pump.PUMPSTATE.OFF;
	    state_pump_2=Pump.PUMPSTATE.OFF;
	    state_pump_3=Pump.PUMPSTATE.OFF;
	}
	
        @Override
        public String toString() {
            return "ThreePumpsStates{" + "P1=" + state_pump_1 + ", P2=" + state_pump_2 + ", P3=" + state_pump_3 + '}';
        }
        
    }
}
