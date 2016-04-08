--
-- Copyright (C) 2016 Keith M. Hughes
--
-- Licensed under the Apache License, Version 2.0 (the "License"); you may not
-- use this file except in compliance with the License. You may obtain a copy of
-- the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
-- WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
-- License for the specific language governing permissions and limitations under
-- the License.
--

--
-- config.lua
--
-- The configuration file for the sensor node.
--

local module = {}

-- A map of WIFI AP information.
-- for now SSIDs to password for the the AP.
module.WIFI_AP_INFO = {}  
module.WIFI_AP_INFO["myssid"] = "sekret"
-- $include: WIFI_AP_INFO

-- The exact SSID to connect to.
-- 
-- If this value exists, the ESP will attach to it and fail
-- if the SSID is not available. If it is not defined,
-- the ESP will see which APs it finds and will try and
-- connect to the first one it finds it has a password for.
--
-- module.WIFI_SSID = "myssid"
-- $include: WIFI_SSID

-- The unique name of this node.
module.NODE_ID = "nodemcu"..node.chipid()

-- The MQTT host and port
--
-- module.MQTT_PORT = 1883
-- module.MQTT_HOST = "broker.example.com"
-- $include: MQTT_HOST_PORT

-- The client ID that the sensor node will show up with in MQTT.
module.MQTT_CLIENTID = "/sensornode/" .. module.NODE_ID

-- The MQTT topic the data will be sent on.
module.MQTT_TOPIC = "/home/sensor"

-- The MQTT QualityOfService for published data.
module.MQTT_QOS = 0

-- The hardware pin the DHT is attached to.
module.DHT_PIN = 1

-- How often to sample the sensors, in milliseconds
module.SENSOR_SAMPLE_DELAY = 5 * 1000

-- The ESP timer to use for checking for WIFI connection.
module.TIMER_WIFI_CONNECTION = 1

-- The ESP timer to use for triggering a sensor sample.
module.TIMER_SENSOR_SAMPLE = 2

return module 
