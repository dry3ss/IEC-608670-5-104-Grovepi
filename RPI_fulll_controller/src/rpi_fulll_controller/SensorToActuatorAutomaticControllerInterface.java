/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rpi_fulll_controller;

/**
 *
 * @author will
 */
public interface SensorToActuatorAutomaticControllerInterface {
    
    public abstract void resetSCCMPair(int sensor_nb);
    public abstract void setLatestSensorLevel(float new_level,int sensor_nb);
    
}
