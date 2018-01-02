/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rpi_fulll_controller;

import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;



/**
 *
 * @author Will
 */
public class SensorToActuatorAutomaticController {
    protected ActuatorControllerConnectionManager aacm;
    protected boolean work_to_do;
    
    
    protected Pair_SSCM_SensorValue[] SSCM_sensor_values_pairs;
    
    
    protected class Pair_SSCM_SensorValue
    {
	final protected static float DEFAULT_VALUE_SENSOR=-1;
	protected SensorControllerConnectionManager sscm;
	protected float latest_sensor_levels;

	public Pair_SSCM_SensorValue()
	{
	    sscm=null;
	    latest_sensor_levels=DEFAULT_VALUE_SENSOR;
	}
	
	public SensorControllerConnectionManager getSscm() {
	    return sscm;
	}

	public void setSscm(SensorControllerConnectionManager sscm) {
	    this.sscm = sscm;
	}

	public float getLatestSensorLevels() {
	    return latest_sensor_levels;
	}

	public void setLatestSensorLevels(float latest_sensor_levels) {
	    this.latest_sensor_levels = latest_sensor_levels;
	}
	
	public void reset()
	{
	    sscm=null;
	    latest_sensor_levels=DEFAULT_VALUE_SENSOR;
	}
	
    }
    
    public void printWithName(String msg)
    {
        System.out.println("(SensorToActuatorAutomaticController:) "+msg);
    }
    
    
    
    
    public void setLatestSensorLevel(float new_level,int sensor_nb)
    {
        printWithName("setLatestSensorLevel "+Float.toString(new_level)+" from "+Float.toString(sensor_nb));
	SSCM_sensor_values_pairs[sensor_nb].setLatestSensorLevels(new_level);
    }
    
    public void resetSSCMPair(int sensor_nb)
    {
        printWithName("resetSSCMPair "+Integer.toString(sensor_nb));
	SSCM_sensor_values_pairs[sensor_nb].reset();
    }
    
    
    public void registerSCCM(SensorControllerConnectionManager sccm, int assigned_id)
    {
	if(sccm == null || assigned_id <0)
	    return;
        printWithName("registerSCCM with id "+Integer.toString(assigned_id));
        SSCM_sensor_values_pairs[assigned_id].setSscm(sccm);
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
	SSCM_sensor_values_pairs[assigned_id].getSscm().resetAndResignSensorDataCallback();
    }
    
    public void unregisterSCCMs()
    {
	for(int i=0; i<SSCM_sensor_values_pairs.length;i++)
	{
            printWithName("unregisterSCCM with id "+Integer.toString(SSCM_sensor_values_pairs[i].getSscm().getIdSensorDataCallback()));
	    SSCM_sensor_values_pairs[i].getSscm().resetAndResignSensorDataCallback();
	}
    }
    
    
    
    //CONSTRUCTOR
    public SensorToActuatorAutomaticController(ActuatorControllerConnectionManager aacm_ )
    {
	aacm=aacm_;
	work_to_do=false;
	SSCM_sensor_values_pairs= new Pair_SSCM_SensorValue[2];
        for(int i=0;i<SSCM_sensor_values_pairs.length;i++)
            SSCM_sensor_values_pairs[i]=new Pair_SSCM_SensorValue();
    }
    
    
    public float convertSensorInputToDecisionLevel()
    {
	float r=0;
	for(int i=0;i<SSCM_sensor_values_pairs.length;i++)
	{
	    float v=SSCM_sensor_values_pairs[i].getLatestSensorLevels();
	    if(v >0)
		r+=v;
	}
	/*TODO: add the treatment when 2 sensors are present*/
	//Java passes them by value, so we are not modifying the actual levels
	//only our processing of them
	return r;
    }
    
    /**
     * TODO:
     * THIS FUNCTION IS TO BE REMOVED IT SHOULD NOT BE NECESSARY ONCE THE SENSORS 
     * AUTOMATICALLY SEND THEIR DATA PERIODICALLY
     */
    public void getFreshSensorInputs()
    {
	for(int i=0;i<SSCM_sensor_values_pairs.length;i++)
	{
	    try{
		SSCM_sensor_values_pairs[i].getSscm().sendInterrogationCommand();
	    }catch (Exception e){}
	}
    }
    
    public void stopRegulatingLevel()
    {
	work_to_do=false;
    }
    
    /**
     * 
     * @param desired_level
     * @param precision such as current level € [desired_level - precision;desired_level + precision]
     */
    public void startRegulatingLevel(float desired_level,float precision)
    {
	/*TODO : remove the getFreshSensorInputs(); */
	work_to_do=true;
	getFreshSensorInputs();
	float min_accepted=desired_level-precision;
	float max_accepted=desired_level+precision;
	while(work_to_do)
	{	    
	    float decision_lvl=convertSensorInputToDecisionLevel();
	    
	    //we do it now for the next iteration because the request-answer is slow
	    getFreshSensorInputs();
	    if(decision_lvl<min_accepted)
		aacm.sendForwardAll();
	    else if (decision_lvl> max_accepted)
		aacm.sendBackwardsAll();
	    else
		break;
	}
	aacm.sendStopAll();
	work_to_do=false;	
    }


    //INTERFACE 
    public void addButtonsToPannel(JPanel panel)
    {
	//"Start_regulating" Button
    	JButton bstart = new JButton("Start_regulating");
    	bstart.addActionListener(new ActionListener(){
	    public void actionPerformed(java.awt.event.ActionEvent e){
		startRegulatingLevel(18,1);
	    }
	});
    	
    	//"Stop_regulating" Button 
    	JButton bstop = new JButton("Stop_regulating");
    	bstop.addActionListener(new ActionListener(){
	    public void actionPerformed(java.awt.event.ActionEvent e){
		stopRegulatingLevel();
	    }	
	});
    	
    	//Add buttons on the dashboard
    	panel.add(bstart);
    	panel.add(bstop);
    }


}