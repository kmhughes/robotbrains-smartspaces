/**
 * GreenHouse Sensors
 * 
 * Written by Keith M. Hughes
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
 
#include <Wire.h>
#include <Adafruit_AM2315.h>
#include <Adafruit_MPL3115A2.h>

// The inside AM2315 temperature and humidity sensor.
Adafruit_AM2315 am2315Inside(&Wire);

// The barametric pressure sensor.
// This is on the indoor I2C bus.
Adafruit_MPL3115A2 barometric(&Wire);


// The outside AM2315 temperature and humidity sensor.
Adafruit_AM2315 am2315Outside(&Wire1);

// The sensor data packet containing all data to be transmitted
// to the host.
typedef struct {
  float temperatureInside;
  float humidityInside;
  float temperatureOutside;
  float humidityOutside;
  int windSpeed;
} SenseData;

SenseData senseData;

void setup() {
  Serial.begin(9600);

  if (! am2315Outside.begin()) {
     Serial.println("Outside AM2315 sensor not found, check wiring & pullups!");
     while (1);
  }

  if (! am2315Inside.begin()) {
     Serial.println("Inside AM2315 sensor not found, check wiring & pullups!");
     while (1);
  }
  
  // TODO(keith): initialize barametric sensors
}

void loop() {
  am2315Outside.readTemperatureAndHumidity(senseData.temperatureOutside, senseData.humidityOutside);
  am2315Inside.readTemperatureAndHumidity(senseData.temperatureInside, senseData.humidityInside);

  senseData.windSpeed = analogRead(0);
  
  // TODO(keith): Remove when sending data to host
  Serial.print("Outside Hum: "); Serial.println(senseData.humidityOutside);
  Serial.print("Outside Temp: "); Serial.println(senseData.temperatureOutside);
  Serial.print("Inside Hum: "); Serial.println(senseData.humidityInside);
  Serial.print("Inside Temp: "); Serial.println(senseData.temperatureInside);

  Serial.print("Wind Speed: "); Serial.println(senseData.windSpeed);
  
  // TODO(keith): Write data to host computer
  
  // TODO(keith): change sample rate to every 5 minutes or so
  delay(1000);
}
