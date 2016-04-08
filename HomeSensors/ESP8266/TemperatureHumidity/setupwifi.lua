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
-- wifisetup.lua
--
-- The setup of the WIFI connection.
--

local module = {}

-- Start the application onece the IP address is obtained.
local function wifi_wait_ip()
  -- Only stop if an IP address has been obtained.  
  if wifi.sta.getip() ~= nil then
    -- Don't need the timer anymore so stop it.
    tmr.stop(config.TIMER_WIFI_CONNECTION)

    -- Print out some useful information about the connection.
    print("WIFI MAC address is: " .. wifi.ap.getmac())
    print("WIFI IP is " .. wifi.sta.getip())

    -- Can now start the application
    application.start()
  else
    print("Still attempting WIFI connection")
  end
end

-- Initiate a WIFI connection attempt to the given SSID.
local function wifi_connect(ssid) 
  print("Attempting connection to SSID " .. ssid .. " ...")

  wifi.setmode(wifi.STATION);
  wifi.sta.config(ssid, config.WIFI_AP_INFO[ssid])
  wifi.sta.connect()
                
  -- Now that the connection attempt has started, start sampling
  -- to see when the IP is obtained.
  tmr.alarm(config.TIMER_WIFI_CONNECTION, 2500,
      tmr.ALARM_AUTO, wifi_wait_ip)
end

-- Get the list of APs that the ESP finds and try and connect.
local function wifi_ap_scan(list_aps)  
  if list_aps then
    print("WIFI APs detected, checking for connection.")
    for ssid,v in pairs(list_aps) do
      print("Checking SSID "..ssid)
      if config.WIFI_AP_INFO and config.WIFI_AP_INFO[ssid] then
        wifi_connect(ssid)
      end
    end
    print("Done AP List scan. None found")
  else
    print("Error getting AP list")
  end
end

-- Start the WIFI module.
--
-- This will attempt a connection to a known SSID.
function module.start()  
  print("Configuring WIFI")
  wifi.setmode(wifi.STATION);

  -- If a specific SSID has been set, use it.
  if config.WIFI_SSID ~= nil then
    wifi_connect(config.WIFI_SSID)
  else
    wifi.sta.getap({}, 0, wifi_ap_scan)
  end
end

return module
