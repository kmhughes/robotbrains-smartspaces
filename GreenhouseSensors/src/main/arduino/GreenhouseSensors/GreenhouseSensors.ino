/**
 * GreenHouse Sensors
 *
 * Read 2 I2C temperature/humidity sensors and an I2C barometric pressure/altimeter sensor.
 *
 * This program runs on a Teensy 3.1+.
 * 
 * Written by Keith M. Hughes
 */

// Change to Wire.h if not using the Teensy.
#include <i2c_t3.h>

#include <Adafruit_AM2315.h>
#include <Adafruit_MPL3115A2.h>
#include <XBee.h>

// Comment out if not debugging
#define DEBUG 1

// The number of milliseconds to sleep between sensor readings.
const int SLEEP_DELAY = 1000;

// The inside AM2315 temperature and humidity sensor.
Adafruit_AM2315 am2315Inside(&Wire1);

// The barometric pressure sensor.
Adafruit_MPL3115A2 barometric(&Wire1);


// The outside AM2315 temperature and humidity sensor.
Adafruit_AM2315 am2315Outside(&Wire);

// The sensor data packet containing all data to be transmitted
// over the radio.
typedef struct _SensorData {
  // The temperature inside the greenhouse in degrees C.
  float temperatureInside;
  
  // The % relative humidity inside the greenhouse.
  float humidityInside;
  
  // The temperature outside the greenhouse in degrees C.
  float temperatureOutside;
  
  // The % relative humidity outside the greenhouse.
  float humidityOutside;
  
  // The altitude in meters.
  float altitude;
  
  // The barometric pressure in pascals.
  float barometricPressure;
} SensorData;

// The sensor data being read from the sensors.
SensorData sensorData;

// The XBee radio object.
XBee xbee;

// SH + SL Address of receiving XBee. Sending to coordinator.
XBeeAddress64 addr64 = XBeeAddress64(0x00000000, 0x00000000);

// The TX request that will send the data to the Coordinator
ZBTxRequest zbTx = ZBTxRequest(addr64, (uint8_t *)&sensorData, sizeof(sensorData));

void setup() {
#ifdef DEBUG
  Serial.begin(9600);
#endif

  // The XBee will be on Teensy RX1/TX1 which is Serial1.
  Serial1.begin(9600);
  xbee = XBee();
  xbee.begin(Serial1);
}

void loop() {
  // begin() will return 0 if the sensor is not found.
  if (am2315Outside.begin()) {
    am2315Outside.readTemperatureAndHumidity(sensorData.temperatureOutside, sensorData.humidityOutside);
    
#ifdef DEBUG
    Serial.print("Outside Temp: "); 
    Serial.print(sensorData.temperatureOutside); 
    Serial.println(" C");
    Serial.print("Outside Hum: "); 
    Serial.print(sensorData.humidityOutside);
    Serial.println(" %RH");
#endif
  } else {
    // Sensor is not available, send Not A Number values for sensor data.
    sensorData.humidityOutside = NAN;
    sensorData.temperatureOutside = NAN;

#ifdef DEBUG
    Serial.println("Cannot see outside AM2315");
#endif
  }

  // begin() will return 0 if the sensor is not found.
  if (am2315Inside.begin()) {
    am2315Inside.readTemperatureAndHumidity(sensorData.temperatureInside, sensorData.humidityInside);
    
#ifdef DEBUG
    Serial.print("Inside Temp: "); 
    Serial.print(sensorData.temperatureInside);
    Serial.println(" C");
    Serial.print("Inside Hum: "); 
    Serial.print(sensorData.humidityInside);
    Serial.println(" %RH");
#endif
  } else {
    // Sensor is not available, send Not A Number values for sensor data.
    sensorData.humidityInside = NAN;
    sensorData.temperatureInside = NAN;
    
#ifdef DEBUG
    Serial.println("Cannot see inside AM2315");
#endif
  }
 
  // begin() will return 0 if the sensor is not found.
  if (barometric.begin()) {
    sensorData.barometricPressure = barometric.getPressure();
    sensorData.altitude = barometric.getAltitude();

#ifdef DEBUG
    Serial.print(sensorData.altitude); 
    Serial.println(" Meters");
    Serial.print(sensorData.barometricPressure); 
    Serial.println(" Pascals");
#endif
  } else {
    // Sensor is not available, send Not A Number values for sensor data.
    sensorData.barometricPressure = NAN;
    sensorData.altitude = NAN;
    
#ifdef DEBUG
    Serial.println("Couldnt find barometric sensor");
#endif
  }
  
  // Send the packet to the coordinator.
  xbee.send(zbTx);

  delay(SLEEP_DELAY);
}
