#
# Read Greenhouse sensor data from an XBee.
#
# Written by Keith M. Hughes
#

from xbee import ZigBee
import sys
import serial
import struct

# The Baud rate of the XBee
SERIAL_BAUD_RATE = 9600

# The name of the serial port to use.
serial_port_name = sys.argv[1]

# The 64 bit address for the XBee radio in the greenhouse
greenhouse_xbee_address = sys.argv[2]
 
# Open serial port.
serial_port = serial.Serial(serial_port_name, SERIAL_BAUD_RATE)
 
# Create the XBee API object for a Series 2 radio in escaped API mode.
xbee = ZigBee(serial_port, escaped=True)
 
# Continuously read and print packets
while True:
  try:
    # Block until an RX frame is received.
    frame = xbee.wait_read_frame()

    # If the data comes from the radio in the greenhouse, print it.
    radio_address = ''.join('%02x' % ord(byte) for byte in frame['source_addr_long'])
    if radio_address == greenhouse_xbee_address:
      rf_data = frame['rf_data']

      # The data is a series of little-endian floats.
      inside_temperature = struct.unpack('f',rf_data[0:4])[0]
      inside_humidity = struct.unpack('f',rf_data[4:8])[0]
      outside_temperature = struct.unpack('f',rf_data[8:12])[0]
      outside_humidity = struct.unpack('f',rf_data[12:16])[0]
      altitude = struct.unpack('f',rf_data[16:20])[0]
      barometric_pressure = struct.unpack('f',rf_data[20:24])[0]

      print radio_address
      print '\tInside: Temperature: ', inside_temperature, 'C Humidity: ', inside_humidity, '%RH'
      print '\tOutside: Temperature: ', outside_temperature, 'C Humidity: ', outside_humidity, '%RH'
      print '\tAltitude: ', altitude, 'M Barometric Pressure: ', barometric_pressure, "Pascals"
  except KeyboardInterrupt:
    break

# Make sure the serial port gets closed.
serial_port.close()
