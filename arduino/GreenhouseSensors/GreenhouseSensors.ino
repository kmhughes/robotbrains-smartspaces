/**
 * GreenHouse Sensors
 * 
 * Written by Keith M. Hughes
 *
 */
 
#include <Wire.h>
#include <Adafruit_AM2315.h>
#include <Adafruit_MPL3115A2.h>

// The inside AM2315 temperature and humidity sensor.
Adafruit_AM2315 am2315Inside(&Wire);

Adafruit_MPL3115A2 barometric(&Wire);


// The outside AM2315 temperature and humidity sensor.
Adafruit_AM2315 am2315Outside(&Wire1);

// The sensor data packet containing all data to be transmitted
// to the host.
typedef struct _SenseData {
  float temperatureInside;
  float humidityInside;
  float temperatureOutside;
  float humidityOutside;
} SenseData;

SenseData senseData;

void setup() {
  Serial.begin(9600);

  if (! am2315Outside.begin()) {
     Serial.println("Sensor not found, check wiring & pullups!");
     while (1);
  }
}

void loop() {
  am2315Outside.readTemperatureAndHumidity(senseData.temperatureOutside, senseData.humidityOutside);
  Serial.print("Hum: "); Serial.println(senseData.humidityOutside);
  Serial.print("Temp: "); Serial.println(senseData.temperatureOutside);

  Serial.println(analogRead(0));
  delay(1000);
}
