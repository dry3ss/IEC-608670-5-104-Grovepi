/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pumps_control;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.Pin;

/**
 *
 * @author Will
 */
public class TernaryPump {
    public static final GpioController gpio;
    
    static{
        gpio= GpioFactory.getInstance(); 
    }
    
    protected final GpioPinDigitalOutput pin_f;
    protected final GpioPinDigitalOutput pin_b;
    protected final GpioPinDigitalOutput pin_e;    
    
    public enum TERNARY_PUMP_STATE {
        FORWARD((short)1), BACKWARDS((short)2),OFF((short)0),UNKNOWN((short)5);

        final private short numVal;

        TERNARY_PUMP_STATE(short numVal) {
            this.numVal = numVal;
        }
        TERNARY_PUMP_STATE()
        {
            this.numVal=0;
        }
        TERNARY_PUMP_STATE(TERNARY_PUMP_STATE i)
        {
            this.numVal=i.getNumVal();
        }

        public String toString()
        {
            switch(this)
            {
                case FORWARD:
                    return new String("F");
                case BACKWARDS:
                    return new String("B");
                case OFF:
                    return new String("O");
                default:
                    return new String("UNKNOWN");
            }
        }
        public short getNumVal() {
            return numVal;
        }
    }
    
    public TernaryPump(Pin pin_forward,Pin pin_backwards,Pin pin_enable)
    {
        
        
        // provision gpio pins as output pins and turn them off
        pin_f=gpio.provisionDigitalOutputPin(pin_forward, "forward", PinState.LOW);
        pin_b=gpio.provisionDigitalOutputPin(pin_backwards, "backwards", PinState.LOW);
        pin_e=gpio.provisionDigitalOutputPin(pin_enable, "enable", PinState.LOW);
        
        // set shutdown state for this pin
        pin_f.setShutdownOptions(true, PinState.LOW);
        pin_b.setShutdownOptions(true, PinState.LOW);
        pin_e.setShutdownOptions(true, PinState.LOW);


    }
    
    public void forward()
    {
        pin_e.low();//just in case, stop the motor while we switch
        
        pin_f.high();
        pin_b.low();
        pin_e.high();
    }
    public void backwards()
    {
        pin_e.low();//just in case, stop the motor while we switch
        
        pin_f.low();
        pin_b.high();
        pin_e.high();
    }
    public void stop()
    {
        pin_e.low();        
        pin_f.low();
        pin_b.low();
    }
    
    public boolean isWorking()
    {
        return pin_e.isHigh();
    }
    public boolean isForward()
    {
        boolean r= pin_e.isHigh();
        
        return (pin_e.isHigh() && ! pin_b.isHigh() && pin_f.isHigh());
    }
    public boolean isBackwards()
    {
        return (pin_e.isHigh() && ! pin_f.isHigh() && pin_b.isHigh());
    }
    
    
    public TERNARY_PUMP_STATE getState()
    {
        if(!isWorking())
            return TERNARY_PUMP_STATE.OFF;
        if(isForward())
            return TERNARY_PUMP_STATE.FORWARD;
        if(isBackwards())
            return TERNARY_PUMP_STATE.BACKWARDS;
        else
            return TERNARY_PUMP_STATE.UNKNOWN;        
    }

    public void setState(TERNARY_PUMP_STATE state)
    {
	switch (state)
	{
	    case FORWARD:
		forward();
		break;
	    case BACKWARDS:
		backwards();
		break;
	    default:
		stop();
		break;                                    
	}
    }
}
