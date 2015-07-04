/**
 * GreenHouse Sensors
 * 
 * Written by Keith M. Hughes
 *
 */

// CHange to Wire.h if not using the Teensy.
#include <i2c_t3.h>

#include <Adafruit_AM2315.h>
#include <Adafruit_MPL3115A2.h>

// The inside AM2315 temperature and humidity sensor.
Adafruit_AM2315 am2315Inside(&Wire1);

Adafruit_MPL3115A2 barometric(&Wire1);


// The outside AM2315 temperature and humidity sensor.
Adafruit_AM2315 am2315Outside(&Wire);

// The sensor data packet containing all data to be transmitted
// to the host.
typedef struct _SenseData {
  float temperatureInside;
  float humidityInside;
  float temperatureOutside;
  float humidityOutside;
  float altitude;
  float barometricPressure;
  float barometicTemp;
} SenseData;;

SenseData senseData;

void setup() {
  Serial.begin(9600);
}

void loop() {
 if (am2315Outside.begin()) {
    am2315Outside.readTemperatureAndHumidity(senseData.temperatureOutside, senseData.humidityOutside);
    Serial.print("Outside Hum: "); Serial.println(senseData.humidityOutside);
    Serial.print("Outside Temp: "); Serial.println(senseData.temperatureOutside);
  } else {
    Serial.println("Cannot see outside AM2315");
  }

  if (am2315Inside.begin()) {
    am2315Inside.readTemperatureAndHumidity(senseData.temperatureInside, senseData.humidityInside);
    Serial.print("Inside Hum: "); Serial.println(senseData.humidityInside);
    Serial.print("Inside Temp: "); Serial.println(senseData.temperatureInside);
  } else {
    Serial.println("Cannot see inside AM2315");
  }
 
  if (barometric.begin()) {
    senseData.barometricPressure = barometric.getPressure();
    // Our weather page presents pressure in Inches (Hg)
    // Use http://www.onlineconversion.com/pressure.htm for other units
    Serial.print(senseData.barometricPressure/3377); Serial.println(" Inches (Hg)");

    senseData.altitude = barometric.getAltitude();
    Serial.print(senseData.altitude); Serial.println(" meters");

    senseData.barometicTemp = barometric.getTemperature();
    Serial.print(senseData.barometicTemp); Serial.println("*C");
  } else {
    Serial.println("Couldnt find barametric sensor");
  }

  delay(1000);
}
