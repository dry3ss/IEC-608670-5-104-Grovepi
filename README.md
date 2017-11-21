# IEC-608670-5-104-Grovepi
A simple Java app to make a Raspberry Pi+Grovepi interact with a controller using the IEC-608670-5-104 ( ~= used in SCADA systems ) protocol.

This app uses the following libraries:
- Openmuc j60870 is handling the IEC-608670-5-104 protocol ( https://www.openmuc.org/iec-60870-5-104/ )
- Dexter Industries GrovePi is handling the interraction between the RPi and the sensors,leds... through the grovepi card ( https://github.com/DexterInd/GrovePi )
- Pi4J is handling the IO on the RPi from the GPio pins through Java ( http://pi4j.com/ )

For simplicity a copy of the .jar files of those libraries along with there respective licenses have been provided in the _ALL_libs directory.

It is recommended to follow the tutorial at https://www.dexterindustries.com/GrovePi/programming/grovepi-programming-java-maven/setting-computer-raspberry-grovepi-java/ 
to set everything up properly so as to be able to compile and execute code from a remote computer directly on the RPi ( the sensors server app for example ) using netbeans