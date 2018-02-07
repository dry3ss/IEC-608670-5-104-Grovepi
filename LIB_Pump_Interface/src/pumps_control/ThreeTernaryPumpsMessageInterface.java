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
public class ThreeTernaryPumpsMessageInterface {
    
        public static final short FLAG_ENABLE_PUMP_1 = 1;    // Binary 0000 0000 0000 0001
        public static final short FLAG_ENABLE_PUMP_2 = 2;    // Binary        ...0000 0010
        public static final short FLAG_ENABLE_PUMP_3 = 4;    // Binary        ...0000 0100
        public static final short FLAG_FORWARD_PUMP_1 = 32;  // Binary        ...0010 0000
        public static final short FLAG_FORWARD_PUMP_2 = 64;  // Binary        ...0100 0000
        public static final short FLAG_FORWARD_PUMP_3 = 128; // Binary        ...1000 0000 
    
    
    public static boolean checkFlag(short msg, short flag)
    {
	return (msg & flag) == flag;
    }
    
    public static class ThreeTernaryPumpsStates
    {
        public TernaryPump.TERNARY_PUMP_STATE state_pump_1;
        public TernaryPump.TERNARY_PUMP_STATE state_pump_2;
        public TernaryPump.TERNARY_PUMP_STATE state_pump_3; 

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
	    final ThreeTernaryPumpsStates other = (ThreeTernaryPumpsStates) obj;
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

	public ThreeTernaryPumpsStates()
	{
	    state_pump_1=TernaryPump.TERNARY_PUMP_STATE.OFF;
	    state_pump_2=TernaryPump.TERNARY_PUMP_STATE.OFF;
	    state_pump_3=TernaryPump.TERNARY_PUMP_STATE.OFF;
	}
	
        @Override
        public String toString() {
            return "{" + state_pump_1 + ", " + state_pump_2 + ", " + state_pump_3 + '}';
        }
        
    }
}
