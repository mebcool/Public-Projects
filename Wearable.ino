#define USE_ARDUINO_INTERRUPTS true    // Set-up low-level interrupts for most acurate BPM math.
#include <PulseSensorPlayground.h>     // Includes the PulseSensorPlayground Library.   
#include "SparkFun_MMA8452Q.h"
#include <Wire.h>    

const int PulseWire = 0;       // PulseSensor PURPLE WIRE connected to ANALOG PIN 0
const int LED = 13;          // The on-board Arduino LED, close to PIN 13.
int Threshold = 550;           // Determine which Signal to "count as a beat" and which to ignore.  
float xMax = 0;
float yMax = 0;
float zMax = 0;
int myBPM;
PulseSensorPlayground pulseSensor;  // Creates an instance of the PulseSensorPlayground object called "pulseSensor"
MMA8452Q accel;
enum states{
  SYS_OFF,
  SYS_ON,
  SOS
};
enum states mystate = SYS_OFF;
float prevtime = 0;
float time = 0;

void setup() {   
  Serial.begin(9600); 
  // Configure the PulseSensor object, by assigning our variables to it. 
  pulseSensor.analogInput(PulseWire);   
  pulseSensor.blinkOnPulse(LED);       //auto-magically blink Arduino's LED with heartbeat.
  pulseSensor.setThreshold(Threshold);   
  Wire.begin();

  if (accel.begin() == false) {
    Serial.println("Not Connected. Please check connections and read the hookup guide.");
    while (1);
  }
  pulseSensor.begin();
}
byte samplesUntilReport;
const byte SAMPLES_PER_SERIAL_SAMPLE = 10;

void loop() {
  myBPM = pulseSensor.getBeatsPerMinute(); 
  float onOffPoten = analogRead(A1); //potentiometer  
  float x = accel.getCalculatedX();
  float y = accel.getCalculatedY();
  float z = accel.getCalculatedZ();
  if (x > xMax){
    xMax = x;
  }
  if (y > yMax){
    yMax = y;
  }
  if (z > zMax){
    zMax = z;
  }
 

  switch(mystate){
   case SYS_OFF:
      xMax = 0;
      yMax = 0;
      zMax = 0;
      if(onOffPoten > 50){
        mystate = SYS_ON;
      }
    break;

    case SYS_ON:
    if (pulseSensor.sawStartOfBeat()) {   
      Serial.print("BPM: ");
      Serial.println(myBPM);
    }
    delay(20);
    if (millis() - prevtime > 1000){
      if(xMax > 1.9 || yMax > 1.9 || zMax > 1.9){
        Serial.println("Accelerometer threshold reached");
        Serial.println(xMax);
        Serial.println(yMax);
        Serial.println(zMax);
        time = millis();
        mystate = SOS;
      }
      prevtime = millis();
    }
    if(onOffPoten < 50){
       mystate = SYS_OFF;
    }
    if(onOffPoten > 1000){
      mystate = SOS;
    }
    break;

    case SOS:
    if(millis() - prevtime > 1000){
      String dataString = "SOS," + String(myBPM) + "," + String(xMax) + "," + String(yMax) + "," + String(zMax);
      Serial.println(dataString);
      prevtime = millis();
    }
    if(onOffPoten < 50){
       mystate = SYS_OFF;
    }
    break;
    default:
    Serial.println("crash, out of case");
  }
}

  
