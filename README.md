# IEC-608670-5-104-Grovepi
Simple Java server/client apps to make a complete SCADA IEC/104 demonstrator using:
- 2 (or 1) Raspberry Pi+Grovepi connected with one ultrasound GrovePi distance sensor each
- 1 Raspberry Pi linked through an electronic circuit with 3 pumps
- 1 computer (as a controller) 

This demonstrator makes the 3 RPi interact with the controller using the IEC-608670-5-104 ( ~= used in SCADA systems ) protocol.

Those are Netbeans projects.

## I. Those apps uses the following libraries:
#### External libraries
- Openmuc j60870 is handling the IEC-608670-5-104 protocol ( https://www.openmuc.org/iec-60870-5-104/ )
- Dexter Industries GrovePi is handling the interraction between the RPi and the sensors,leds... through the grovepi card ( https://github.com/DexterInd/GrovePi )
- Pi4J is handling the IO on the RPi from the GPio pins through Java ( http://pi4j.com/ )

**For simplicity a copy of the .jar files of those libraries along with there respective licenses have been provided in the "_ALL_external_libs_" directory.**
If the software for the GrovePi is not yet installed on youor Raspberry, you need to follow this: https://www.dexterindustries.com/GrovePi/get-started-with-the-grovepi/setting-software/ to install it, and then this: https://www.dexterindustries.com/GrovePi/get-started-with-the-grovepi/updating-firmware/ to update the firmware (you will have an issue with the distance sensors if you don't).

It is recommended to follow the tutorial at https://www.dexterindustries.com/GrovePi/programming/grovepi-programming-java-maven/setting-computer-raspberry-grovepi-java/ 
to set everything up properly so as to be able to compile and execute code from a remote computer directly on the RPi ( the sensors server app for example ) using netbeans

/!\ if you have a PI4J version mismatch error:
- Download PI4J 1.2 SNAPSHOT  here (pi4j-1.2-SNAPSHOT.deb) http://pi4j.com/download.html 
- Install it on the Raspberry Pi following the "Offline/Manual" method here http://pi4j.com/install.html#OfflineManual

#### Homemade library
To launch the project as-is, you will need to download and link the project against ARPDetox_lib, which you can find here: https://github.com/dry3ss/ARP_detox . Otherwise if you don't want it, comment the code calling ARPDetox_lib's function in RPI_actuator_IEC_104_with_ARPD_included and RPI_sensors_IEC_104_with_ARPD_included (less than ~10 lines: the imports, the code starting up the server and the code stopping it properly)

## II. Instructions (for Netbeans)

#### Pre-configuration:


a. Activate SSH on the RPi and configure a more robust password than pi/raspberry

b. Open all the projects in Netbeans

c. Create new remote platforms for all the RPis, each time:
   - Tools > Java Platform > Add Platform > Remote Java SE 
   - Choose a name (ex: "RPI_pumps")
   - The IP address goes in Host
   - Type the Username and Password needed to access the RPi (ex : "pi", "raspberry") 
   - Change the SSH port if needed
   - In "Remote JRE path" type "/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/jre/" (no quotes)
   - Finish

d. Modify the Full_controller_3TPumps_withOUT_ARPD project to change the IP addresses
   - open Full_controller_3TPumps_withOUT_ARPD > Source Package > rpi_full_controller > Main.java
   - Modify lines 148, 181 and 214 with the right IP addresses
   - Save


#### Remote servers startup:

1. Remotely launch the RPI_actuator_IEC_104_with_ARPD_included project on the corresponding RPI:
   - Right click on the project's name > Properties > Run tab 
   - Select the corresponding Runtime Platform (it will probably say you have to create a new configuration, do so if needed)
   - OK
   - Run > Run project

2. Remotely launch the RPI_sensors_IEC_104_with_ARPD_included project on the corresponding RPI(s), each time (yes you can launch several times the same project at the same time):
   - Right click on the project's name > Properties > Run tab 
   - Select the corresponding Runtime Platform (it will probably say you have to create a new configuration, do so if needed)
   - OK
   - Run > Run project

#### Local controller (client) startup:

1. Start the Full_controller_3TPumps_withOUT_ARPD project on the current machine :
   - Run > Run project


#### Control:

The interface should automatically load with the proper pannels once the connections to each RPi is established through SSH.
For each remote server you can:
- Send a **(clock) Synchronization** command (messages are sent but not acted upon, only a confirmation is sent)
- Send a **Stop server afterwards** order, after which, the next disconnection from the targeted server will properly close the server (will not shutdown the RPI, only the IEC/104 server in itself)
- Send an **Interrogation** command, which will update the interface with the current measure/state of the endpoint (either sensor or actuator):
  - Each sensor RPi returns the current measured distance in cm
  - The pump RPi returns a triplet { X, X, X } where X represent the state of one of the pumps, either : 
    - O for Off
    - F for Forward
    - B for Backwards

For the pump (bottom of the interface) you can also send several orders (all pumps forward, 1st pump backwards ...).

For the sensors (left and right sides of the interface) you can also send a LED ON or OFF command if a LED is connected to port D3 of the Grovepi.

The center is the **automatic controller**, where you adjust the desired water level in mm (as measured from the sensors, so height_sensors - water_level_from_the_bottom), then start/stop the automatic adjustment. While the automatic adjustment is ON, the sensor's state will be continually updated and the pumps will move in the direction needed to fill/empty the tank (place the pumps so that FORWARD is FILLING the tank and BACKWARDS emptying it).
To gain more precision, the automatic controller takes the average of the two sensor's measures.

#### Closing:

When you are done, send each endpoint the **Stop server afterwards** then simply close the controller window. You can then properly shutdown each RPi.

## Additional informations:

The goal of this project was to develop an IEC/104 demonstrator (this), a MitM attack (available here : https://github.com/dry3ss/IEC-104_MitM_utilities ) and an effective counter to this attack (available here :  https://github.com/dry3ss/ARP_detox ).

