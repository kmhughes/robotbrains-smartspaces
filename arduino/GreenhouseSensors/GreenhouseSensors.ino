/**
 * GreenHouse Sensors
 * 
 * Written by Keith M. Hughes
 */

// Change to Wire.h if not using the Teensy.
#include <i2c_t3.h>

#include <Adafruit_AM2315.h>
#include <Adafruit_MPL3115A2.h>
#include <XBee.h>

// The inside AM2315 temperature and humidity sensor.
Adafruit_AM2315 am2315Inside(&Wire1);

// The barometric pressure sensor.
Adafruit_MPL3115A2 barometric(&Wire1);


// The outside AM2315 temperature and humidity sensor.
Adafruit_AM2315 am2315Outside(&Wire);

// The sensor data packet containing all data to be transmitted
// to the host.
typedef struct _SensorData {
  float temperatureInside;
  float humidityInside;
  float temperatureOutside;
  float humidityOutside;
  float altitude;
  float barometricPressure;
} SensorData;

SensorData sensorData;

XBee xbee;

// SH + SL Address of receiving XBee. Sending to coordinator.
XBeeAddress64 addr64 = XBeeAddress64(0x00000000, 0x00000000);

// The TX request that will send the data to the Coordinator
ZBTxRequest zbTx = ZBTxRequest(addr64, (uint8_t *)&sensorData, sizeof(sensorData));

void setup() {
  Serial.begin(9600);
  
  // The XBee will be on Teensy RX1/TX1 which is Serial1
  Serial1.begin(9600);
  xbee = XBee();
  xbee.begin(Serial1);
}

void loop() {
 if (am2315Outside.begin()) {
    am2315Outside.readTemperatureAndHumidity(sensorData.temperatureOutside, sensorData.humidityOutside);
    Serial.print("Outside Hum: "); Serial.println(sensorData.humidityOutside);
    Serial.print("Outside Temp: "); Serial.println(sensorData.temperatureOutside);
  } else {
    sensorData.humidityOutside = NAN;
    sensorData.temperatureOutside = NAN;
    Serial.println("Cannot see outside AM2315");
  }

  if (am2315Inside.begin()) {
    am2315Inside.readTemperatureAndHumidity(sensorData.temperatureInside, sensorData.humidityInside);
    Serial.print("Inside Hum: "); Serial.println(sensorData.humidityInside);
    Serial.print("Inside Temp: "); Serial.println(sensorData.temperatureInside);
  } else {
    sensorData.humidityInside = NAN;
    sensorData.temperatureInside = NAN;
    Serial.println("Cannot see inside AM2315");
  }
 
  if (barometric.begin()) {
    sensorData.barometricPressure = barometric.getPressure();
    
    Serial.print(sensorData.barometricPressure); Serial.println(" Pascals");

    sensorData.altitude = barometric.getAltitude();
    Serial.print(sensorData.altitude); Serial.println(" meters");
  } else {
    sensorData.barometricPressure = NAN;
    sensorData.altitude = NAN;
    Serial.println("Couldnt find barometric sensor");
  }
  
  xbee.send(zbTx);

  delay(1000);
}
