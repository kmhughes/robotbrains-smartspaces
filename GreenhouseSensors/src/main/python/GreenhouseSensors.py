#!/usr/bin/env python

#
# Read Greenhouse sensor data from an XBee.
#
# Written by Keith M. Hughes
#

from xbee import ZigBee
import sys
import serial
import struct
import yaml
import paho.mqtt.client as mqtt
import json
import sys
import time
import signal
import logging

# Set the logging level
logging.basicConfig(level=logging.INFO)

# 1 if the script should connect to the MQTT broker
# 0 if the script should not connect to the MQTT broker
CONNECT = 1

# This script will read samples every time the radio sends them. The following
# value gives the number of milliseconds to wait between samples transmitted
# to the MQTT topic. For example, if the radio is sending samples every 1000
# milliseconds, the value 10000 will skip 9 samples and send the 10th.
TIME_INTERVAL_BETWEEN_TRANSMITTED_SAMPLES = 600000

# The source of the data.
#
# This is a string that identifies the collection of sensors, for example, everything in
# your home.
DATA_SOURCE = 'keith.test'

# The actual sensing unit that is collecting the data.
#
# The thought here is that you would have multiple sensing units per data source.
SENSING_UNIT = 'greenhouse1.test'

# The Baud rate of the XBee
SERIAL_BAUD_RATE = 9600

# Get the logger.
logger = logging.getLogger(__name__)

#
# Various MQTT routines.
#

# The callback for when the MQTT client gets an acknowledgement from the MQTT
# broker.
def on_connect(client, userdata, rc):
  logger.debug("Connected to message broker with result code "+str(rc))

# Signal handler for sigint.
#
# This is used to catch ^C to the client and will do any needed cleanup, for
# example, shut down the connection to the MQTT broker.
def signal_handler(signal, frame):
  # mqttClient.loop_stop()
  serial_port.close()
  sys.exit(0)

signal.signal(signal.SIGINT, signal_handler)

# Read the properties that define user names and passwords.
#
# The configuration file is in YAML.
# be used in both languages.
with open(sys.argv[1]) as fp:
  properties = yaml.safe_load(fp)

# The name of the serial port to use.
serial_port_name = properties['greenhouse.radio.xbee.explorer.serialport']

# The 64 bit address for the XBee radio in the greenhouse
greenhouse_xbee_address = properties['greenhouse.radio.xbee.addr64']
 
# Open serial port.
serial_port = serial.Serial(serial_port_name, SERIAL_BAUD_RATE)

# Get the sensor offsets to bring into calibration witha "true" value.

greenhouseSensorInsideHumidityOffset = properties['greenhouse.sensor.inside.humidity.offset']
greenhouseSensorOutsideHumidityOffset = properties['greenhouse.sensor.outside.humidity.offset']

# Create the XBee API object for a Series 2 radio in escaped API mode.
xbee = ZigBee(serial_port, escaped=True)

# Only create the MQTT client if we are supposed to connect to the MQTT broker.
if CONNECT:
  # Create the outgoing topic for data
  topicOutgoing = properties['smartspaces.cloud.timeseries.topic.incoming']

  # Create the MQTT client.
  mqttClient = mqtt.Client()

  # Set the methods to use for connection
  mqttClient.on_connect = on_connect

  # Set the user name and password from the properties
  mqttClient.username_pw_set(properties['mqtt.username'], properties['mqtt.password'])

  # Connect to the broker.
  mqttClient.connect(properties['mqtt.server.host'], int(properties['mqtt.server.port']), 60)

  # This method will not return and will continually loop to receive network
  # traffic.
  mqttClient.loop_start()


# THe timestamp when data was last sent.
#
# By setting this to 0, the first packet will go out.
timestamp_last_sent = 0

# Continuously read and print packets
while True:
  # Block until an RX frame is received.
  frame = xbee.wait_read_frame()

  # Adding time.timezone to get the time in UTC
  timestamp = int(round((time.time()) * 1000))

  # If the data comes from the radio in the greenhouse, print it.
  radio_address = ''.join('%02x' % ord(byte) for byte in frame['source_addr_long'])
  if radio_address == greenhouse_xbee_address:
    # If too soon for data then don't process this sample
    if timestamp - timestamp_last_sent < TIME_INTERVAL_BETWEEN_TRANSMITTED_SAMPLES:
      continue

    # OK, this sample is going to be transmitted.
    timestamp_last_sent = timestamp

    rf_data = frame['rf_data']

    # The data is a series of little-endian floats.
    inside_temperature = struct.unpack('f',rf_data[0:4])[0]
    inside_humidity = struct.unpack('f',rf_data[4:8])[0]
    outside_temperature = struct.unpack('f',rf_data[8:12])[0]
    outside_humidity = struct.unpack('f',rf_data[12:16])[0]
    altitude = struct.unpack('f',rf_data[16:20])[0]
    barometric_pressure = struct.unpack('f',rf_data[20:24])[0]

    # Adjust humidity values by offset to get in line with "true" measurement.
    inside_humidity = inside_humidity + greenhouseSensorInsideHumidityOffset
    outside_humidity = outside_humidity + greenhouseSensorOutsideHumidityOffset

    logger.debug(radio_address)
    logger.debug('\tTimestamp: %d, Date: %s' % (timestamp, time.strftime('%Y/%m/%d@%H:%M:%S', time.gmtime())))
    logger.debug('\tInside: Temperature: %f C Humidity: %f %%RH' % (inside_temperature, inside_humidity))
    logger.debug('\tOutside: Temperature: %f C Humidity: %f %%RH' % (outside_temperature, outside_humidity))
    logger.debug('\tAltitude: %f M Barometric Pressure: %f Pascals' % (altitude, barometric_pressure))

    message = {
      'type': 'data.sensor',
      'data': {
        'source': DATA_SOURCE,
        'sensingunit': SENSING_UNIT,
        'sensor.data': [
          {
            'sensor': 'inside.temperature',
            'value': inside_temperature,
            'timestamp': timestamp
            },
          {
            'sensor': 'inside.humidity',
            'value' : inside_humidity,
            'timestamp': timestamp
            },
          {
            'sensor': 'outside.temperature',
            'value': outside_temperature,
            'timestamp': timestamp
            },
          {
            'sensor': 'outside.humidity',
            'value' : outside_humidity,
            'timestamp': timestamp
            },
          {
            'sensor': 'barometric.pressure',
            'value' : barometric_pressure,
            'timestamp': timestamp
            }
          ]
        }
      }

    # Write data to the MQTT broker if supposed to.
    if CONNECT:
      mqttClient.publish(topicOutgoing, json.dumps(message))
