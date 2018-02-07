/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pumps_control;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

/**
 *
 * @author will
 */
public class BinaryPump {
        public static final GpioController gpio;
    
    static{
        gpio= GpioFactory.getInstance(); 
    }
    
    protected final GpioPinDigitalOutput pin_f;
    protected final GpioPinDigitalOutput pin_e;    
    
    public enum BINARY_PUMP_STATE {
        FORWARD,OFF,UNKNOWN;
        private short numVal;
        
        public final static short FORWARD_VAL=1;
        public final static short OFF_VAL=0;
        public final static short UNKNOWN_VAL=5;
        
        static{
            FORWARD.numVal=FORWARD_VAL;
            OFF.numVal=OFF_VAL;
            UNKNOWN.numVal=UNKNOWN_VAL;
        }


        public String toString()
        {
            switch(this)
            {
                case FORWARD:
                    return new String("F");
                case OFF:
                    return new String("O");
                default:
                    return new String("UNKNOWN");
            }
        }
        public short getNumVal() {
            return numVal;
        }
        
        public static BINARY_PUMP_STATE getStateFromVal(short val)
        {
            switch (val) {
                case FORWARD_VAL:
                    return FORWARD;
                case OFF_VAL:
                    return OFF;
                default:
                    return UNKNOWN;
            }
        }
        
        public boolean isValid()
        {
            return !this.equals(UNKNOWN);
        }
    }
    
    
    public BinaryPump(Pin pin_forward,Pin pin_enable)
    {
        
        
        // provision gpio pins as output pins and turn them off
        pin_f=gpio.provisionDigitalOutputPin(pin_forward, "forward", PinState.LOW);
        pin_e=gpio.provisionDigitalOutputPin(pin_enable, "enable", PinState.LOW);
        
        // set shutdown state for this pin
        pin_f.setShutdownOptions(true, PinState.LOW);
        pin_e.setShutdownOptions(true, PinState.LOW);


    }
    
    public void forward()
    {
        pin_f.high();
        pin_e.high();
    }
    public void stop()
    {
        pin_e.low();        
        pin_f.low();
    }
    
    public boolean isWorking()
    {
        return pin_e.isHigh();
    }
    public boolean isForward()
    {
        boolean r= pin_e.isHigh();
        
        return (pin_e.isHigh() && pin_f.isHigh());
    }
    
    
    public BINARY_PUMP_STATE getState()
    {
        if(isForward())
            return BINARY_PUMP_STATE.FORWARD;
        else
            return BINARY_PUMP_STATE.OFF;
    }

    public void setState(BINARY_PUMP_STATE forward)
    {
	if(forward==BINARY_PUMP_STATE.FORWARD)
            forward();
        else
            stop();
    }
}
