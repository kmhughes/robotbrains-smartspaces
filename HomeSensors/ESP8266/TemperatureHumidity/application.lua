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
-- application.lua
--
-- Read DHT values and send them via MQTT to a subscriber.
--

local module = {}

-- The MQTT client to be used for publishing sensor data.
local mqtt_client = nil

-- Do a sensor sample and send out the data.
local function do_sampling()
  -- Read the DHT
  status, temperature, humidity, temperature_dec, humidity_dec =
      dht.read(config.DHT_PIN)

  if status == dht.OK then
    message = "{ \"sensor\": \"" .. config.MQTT_CLIENTID ..
        "\", \"temperature\": " .. temperature ..
        ", \"humidity\": " .. humidity .. "}"

    print(message)
    mqtt_client:publish(config.MQTT_TOPIC, message, 
        config.MQTT_QOS, 0, 
        function(client) print("Sensor message sent") end)
  elseif status == dht.ERROR_CHECKSUM then
    print( "DHT Checksum error." )
  elseif status == dht.ERROR_TIMEOUT then
    print( "DHT timed out." )
  end
end

-- MQTT is now connected to the broker.
local function mqtt_connected(client)
  print("Connected to MQTT broker")

  -- Start the timer going for sampling.
  tmr.start(config.TIMER_SENSOR_SAMPLE)
end

-- Have lost the connection to the MQTT broker.
local function mqtt_lost_connection(client) 
  print("Lost connection to MQTT broker")

  -- Stop the sample timer.
  tmr.stop(config.TIMER_SENSOR_SAMPLE)
end

-- MQTT is has connected to the broker for the first time.
local function mqtt_initial_connected(client)
  print("Initial Connection to MQTT broker")
  
  tmr.register(config.TIMER_SENSOR_SAMPLE, 
      config.SENSOR_SAMPLE_DELAY, 
      tmr.ALARM_AUTO, do_sampling)

  mqtt_connected(client)
end

-- The connection to the MQTT broker failed.
local function mqtt_connection_failed(client, reason)
  print("Connection to MQTT broker has failed, reason: "..reason)
end

-- Start the application
function module.start()
  -- init mqtt client with keepalive timer 120sec
  mqtt_client = mqtt.Client(config.MQTT_CLIENTID, 120)

  mqtt_client:on("connect", mqtt_connected)
  mqtt_client:on("offline", mqtt_lost_connection)

  -- 0 is non-secure connection, 1 is autoreconnect
  mqtt_client:connect(config.MQTT_HOST, config.MQTT_PORT, 0, 1,
      mqtt_initial_connected, mqtt_connection_failed)
end

return module
