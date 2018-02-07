/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rpi_fulll_controller;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import pumps_control.TernaryPump;



/**
 *
 * @author Will
 */
public class SensorToActuatorAutomaticController implements SensorToActuatorAutomaticControllerInterface{
    protected ActuatorControllerConnectionManager accm;
    protected boolean is_running;
    protected float desired_level;
    protected float precision;
    private Thread saac_thread;
    private final RunnableSaaC rsaac;
    protected Pair_SCCM_SensorValue[] SCCM_sensor_values_pairs;
    
    protected JLabel current_desired_level_text;
    protected JLabel current_mean_level_value;
    protected JLabel current_mean_level_text;
    protected JLabel current_desired_level_value;
    
    public static final int SLIDER_MIN=0;
    public static final int SLIDER_MAX=300;
    protected JSlider slider_desired_value; 
    
    public static class RunnableSaaC implements Runnable
    {
        protected SensorToActuatorAutomaticController ssac;
        int timeout_ms;
        int ticks_bf_action;
        
        TernaryPump.TERNARY_PUMP_STATE wished_decision;
        TernaryPump.TERNARY_PUMP_STATE latest_decision;
        int ticks_since_last_decision;
        
        public RunnableSaaC()
        {
            ssac=null;
            timeout_ms=100;
            ticks_bf_action=500/timeout_ms;
            resetwishedDirection();
        }
        
        protected void resetwishedDirection()
        {
            ticks_since_last_decision=0;
            latest_decision=TernaryPump.TERNARY_PUMP_STATE.OFF;
            wished_decision=TernaryPump.TERNARY_PUMP_STATE.OFF;         
        }
         
        @Override
        public void run()
        {
            resetwishedDirection();
            ssac.setSlavesToSilence(true);
            ssac.getFreshSensorInputs();
            float min_accepted=ssac.desired_level-ssac.precision;
            float max_accepted=ssac.desired_level+ssac.precision;
            while(ssac.is_running)
            {	    
                float decision_lvl=ssac.convertSensorInputToDecisionLevel();

                //we do it now for the next iteration because the request-answer is slow
                ssac.getFreshSensorInputs();
                if(decision_lvl<min_accepted)
                    wished_decision=TernaryPump.TERNARY_PUMP_STATE.BACKWARDS;
                else if (decision_lvl> max_accepted)
                    wished_decision=TernaryPump.TERNARY_PUMP_STATE.FORWARD;
                else
                    wished_decision=TernaryPump.TERNARY_PUMP_STATE.OFF;
                mayActIfChangeNeeded();
                try {
                    Thread.sleep(timeout_ms);
                } catch (InterruptedException ex) {
                    ssac.printWithName("Automatic thread interrupted");
                    break;
                }
            }
            ssac.accm.sendStopAll();
            ssac.is_running=false;
            ssac.setSlavesToSilence(false);
        }
        
        protected void mayActIfChangeNeeded()
        {
            ticks_since_last_decision++;
            if(ticks_since_last_decision >ticks_bf_action )
            {
                actOnDecision();
            }
            else if(wished_decision != latest_decision)
            {
                actOnDecision();    
            }
            return;            
        }
        
        protected void actOnDecision()
        {
            switch(wished_decision)
            {
                case OFF:
                    ssac.accm.sendStopAll();
                    break;
                case FORWARD:
                    ssac.accm.sendForwardAll();
                    break;
                case BACKWARDS:
                    ssac.accm.sendBackwardsAll();
                    break;                    
            }
            latest_decision=wished_decision;
            ticks_since_last_decision=0;
        }
    
        protected void setSaacOnceAndForAll(SensorToActuatorAutomaticController ssac_)
        {
            if(ssac==null)
                ssac=ssac_;
        }

        public int getTimeout_ms() {
            return timeout_ms;
        }

        public void setTimeout_ms(int timeout_ms) {
            this.timeout_ms = timeout_ms;
        }

        public int getTicks_bf_action() {
            return ticks_bf_action;
        }

        public void setTicks_bf_action(int ticks_bf_action) {
            this.ticks_bf_action = ticks_bf_action;
        }

        public int getTicks_since_last_decision() {
            return ticks_since_last_decision;
        }

        public void setTicks_since_last_decision(int ticks_since_last_decision) {
            this.ticks_since_last_decision = ticks_since_last_decision;
        }
        
        
    }
    
    protected class Pair_SCCM_SensorValue
    {
	final protected static float DEFAULT_VALUE_SENSOR=-1;
	protected SensorControllerConnectionManager sccm;
	protected float latest_sensor_levels;

	public Pair_SCCM_SensorValue()
	{
	    sccm=null;
	    latest_sensor_levels=DEFAULT_VALUE_SENSOR;
	}
	
	public SensorControllerConnectionManager getSccm() {
	    return sccm;
	}

	public void setSccm(SensorControllerConnectionManager sccm) {
	    this.sccm = sccm;
	}

	public float getLatestSensorLevels() {
	    return latest_sensor_levels;
	}

	public void setLatestSensorLevels(float latest_sensor_levels) {
	    this.latest_sensor_levels = latest_sensor_levels;
	}
	
	public void reset()
	{
	    sccm=null;
	    latest_sensor_levels=DEFAULT_VALUE_SENSOR;
	}        

    }
    
       
    //CONSTRUCTOR
    public SensorToActuatorAutomaticController(ActuatorControllerConnectionManager aacm_ )
    {
	accm=aacm_;
	is_running=false;
	SCCM_sensor_values_pairs= new Pair_SCCM_SensorValue[2];
        for(int i=0;i<SCCM_sensor_values_pairs.length;i++)
            SCCM_sensor_values_pairs[i]=new Pair_SCCM_SensorValue();
        precision=0;
        desired_level=18;
        rsaac=new RunnableSaaC();
        rsaac.setSaacOnceAndForAll(this);
        saac_thread= null;//new Thread(rsaac); will be set in the startRegulating() function
        //text labels:
        current_desired_level_text = new JLabel("Current_desired_level: ");
        current_mean_level_text = new JLabel("Current_mean_level: ");
        current_mean_level_value = new JLabel("X");
        current_desired_level_value = new JLabel(getDesiredLevelString());
        //slider:
        slider_desired_value=new JSlider(JSlider.HORIZONTAL,
                                      SLIDER_MIN, SLIDER_MAX, 180);
    }
      
    public String getDesiredLevelString()
    {
        return (Float.toString(desired_level)+" +/- "+Float.toString(precision));
    }
    public void printWithName(String msg)
    {
        System.out.println("(SensorToActuatorAutomaticController:) "+msg);
    }
    
    
    public void setSlavesToSilence(boolean yes)
    {
        for(int i =0;i<SCCM_sensor_values_pairs.length;i++)
            SCCM_sensor_values_pairs[i].getSccm().setSilence(yes);
        accm.setSilence(yes);
    }
    
    public void setLatestSensorLevel(float new_level,int sensor_nb)
    {
        if(is_running && false)
            printWithName("setLatestSensorLevel "+Float.toString(new_level)+" from "+Float.toString(sensor_nb));
	SCCM_sensor_values_pairs[sensor_nb].setLatestSensorLevels(new_level);
    }
    
    public void resetSCCMPair(int sensor_nb)
    {
        printWithName("resetSCCMPair "+Integer.toString(sensor_nb));
	SCCM_sensor_values_pairs[sensor_nb].reset();
    }
    
    
    public void registerSCCM(SensorControllerConnectionManager sccm, int assigned_id)
    {
	if(sccm == null || assigned_id <0)
	    return;
        printWithName("registerSCCM with id "+Integer.toString(assigned_id));
        SCCM_sensor_values_pairs[assigned_id].setSccm(sccm);
	sccm.registerSensorDataCallback(this, assigned_id);
    }
    public void unregisterSCCM(SensorControllerConnectionManager sccm)
    {
	if(sccm == null )
	    return;
        printWithName("unregisterSCCM with id "+Integer.toString(sccm.getIdSensorDataCallback()));
	sccm.resetAndResignSensorDataCallback();
    }
    public void unregisterSCCM(int assigned_id)
    {
	if( assigned_id<0 )
	    return;
        printWithName("unregisterSCCM with id "+Integer.toString(assigned_id));
	SCCM_sensor_values_pairs[assigned_id].getSccm().resetAndResignSensorDataCallback();
    }
    
    public void unregisterSCCMs()
    {
	for(int i=0; i<SCCM_sensor_values_pairs.length;i++)
	{
            if(SCCM_sensor_values_pairs[i]==null)
                    continue;
            if(SCCM_sensor_values_pairs[i].getSccm()==null)
                    continue;
            printWithName("unregisterSCCM with id "+Integer.toString(SCCM_sensor_values_pairs[i].getSccm().getIdSensorDataCallback()));
	    SCCM_sensor_values_pairs[i].getSccm().resetAndResignSensorDataCallback();
	}
    }

    public void setDesiredLevel(float desired_level) {
        this.desired_level = desired_level;
    }
    public void setDesiredLevel(int desired_level_in_mm) {
        this.desired_level = (desired_level_in_mm/10.0F);
        current_desired_level_value.setText(getDesiredLevelString());
    }

    public void setPrecision(float precision) {
        this.precision = precision;
        current_desired_level_value.setText(getDesiredLevelString());
    }
    
    
    public float convertSensorInputToDecisionLevel()
    {
	float r=0;
        int s=0;
	for(int i=0;i<SCCM_sensor_values_pairs.length;i++)
	{
	    float v=SCCM_sensor_values_pairs[i].getLatestSensorLevels();
	    if(v >0)
            {
                r+=v;
                s++;
            }
	}
        if(s<=0)
            s=1;
	/*TODO: refine le logic instead of simple mean value*/
        r=r/s;
        current_mean_level_value.setText(Float.toString(r));
	return r;
    }
    
    /**
     * TODO:
     * THIS FUNCTION IS TO BE REMOVED IT SHOULD NOT BE NECESSARY ONCE THE SENSORS 
     * AUTOMATICALLY SEND THEIR DATA PERIODICALLY
     */
    public void getFreshSensorInputs()
    {
	for(int i=0;i<SCCM_sensor_values_pairs.length;i++)
	{
	    try{
		SCCM_sensor_values_pairs[i].getSccm().sendInterrogationCommand();
	    }catch (Exception e){}
	}
    }
    
    public void stopRegulatingLevel() throws InterruptedException 
    {
	is_running=false;
        saac_thread.join();
        printWithName("Stopping regulating the level ");
    }
    
    /**
     * 
     * @param desired_level_
     * @param precision_
     * @param desired_level
     * @param precision such as current level € [desired_level - precision;desired_level + precision]
     */
    public void startRegulatingLevel() throws InterruptedException
    {
         if(is_running && saac_thread!=null)
        {
            System.err.println("Stopping and restarting the saac thread to chang the desired level and precision");
            stopRegulatingLevel();
        }
        saac_thread= new Thread(rsaac);
        printWithName("Starting regulating the level to "+Float.toString(desired_level)+" +/- "+Float.toString(precision));
	is_running=true;
        saac_thread.start();
    }


    //INTERFACE 
    public void addButtonsToPannel(JPanel panel)
    {
        //layout
        panel.setLayout(new GridLayout(2,1));
        JPanel panel_slider= new JPanel();
        JPanel panel_buttons= new JPanel();
        panel_buttons.setLayout(new GridLayout(3,2));
        
        //border ...
        TitledBorder border = new TitledBorder("Automatic Controller");
        border.setTitleJustification(TitledBorder.CENTER);
        border.setTitlePosition(TitledBorder.TOP);                
        panel.setBorder(border);
        
        
	//"Start_regulating" Button
    	JButton bstart = new JButton("Start_regulating");
    	bstart.addActionListener(new ActionListener(){
	    public void actionPerformed(java.awt.event.ActionEvent e){
                try{
                    startRegulatingLevel();
                }
                catch (InterruptedException ie)
                {
                }
	    }
	});
    	
    	//"Stop_regulating" Button 
    	JButton bstop = new JButton("Stop_regulating");
    	bstop.addActionListener(new ActionListener(){
	    public void actionPerformed(java.awt.event.ActionEvent e){
		try{
                    stopRegulatingLevel();
                }
                catch (InterruptedException ie)
                {
                }
	    }	
	});   	
             
        //slider config:
        slider_desired_value.setPreferredSize(new Dimension(1000, 120));
        slider_desired_value.setMajorTickSpacing(10);
        slider_desired_value.setMinorTickSpacing(5);
        slider_desired_value.setPaintTicks(true);
        slider_desired_value.setPaintLabels(true);
        slider_desired_value.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e) 
            {
            JSlider source = (JSlider)e.getSource();
            if (!source.getValueIsAdjusting()) 
            {// if we are done playing around and not currently changing the value
                int v=source.getValue();
                setDesiredLevel(v);
                if(is_running)
                {
                    try{
                        startRegulatingLevel();
                    }
                    catch (InterruptedException ie)
                    {
                    }
                }
            }
        }
        });
        
        
        panel_slider.add(slider_desired_value);
        
    	panel_buttons.add(current_desired_level_text);
    	panel_buttons.add(current_desired_level_value);
        panel_buttons.add(current_mean_level_text);
    	panel_buttons.add(current_mean_level_value);
        panel_buttons.add(bstart);
    	panel_buttons.add(bstop);
        
        panel.add(panel_slider);
        panel.add(panel_buttons);
    }


}