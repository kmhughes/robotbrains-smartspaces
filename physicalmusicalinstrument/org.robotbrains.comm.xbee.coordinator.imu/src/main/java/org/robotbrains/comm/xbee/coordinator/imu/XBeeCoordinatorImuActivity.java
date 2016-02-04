/*
 * Copyright (C) 2012 Keith M. Hughes.
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

package org.robotbrains.comm.xbee.coordinator.imu;

import io.smartspaces.activity.impl.BaseActivity;
import io.smartspaces.service.comm.serial.xbee.RxResponseXBeeFrame;
import io.smartspaces.service.comm.serial.xbee.XBeeCommunicationEndpoint;
import io.smartspaces.service.comm.serial.xbee.XBeeCommunicationEndpointService;
import io.smartspaces.service.comm.serial.xbee.XBeeResponseListenerSupport;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A Smart Spaces activity which listens to an XBee radio sending IMU packets.
 *
 * @author Keith M. Hughes
 */
public class XBeeCoordinatorImuActivity extends BaseActivity {

  /**
   * The name of the config property for obtaining the serial port.
   */
  public static final String CONFIGURATION_PROPERTY_HARDWARE_SERIAL_PORT = "space.hardware.serial.port";

  /**
   * The XBee endpoint.
   */
  private XBeeCommunicationEndpoint xbee;

  @Override
  public void onActivitySetup() {
    XBeeCommunicationEndpointService service =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(XBeeCommunicationEndpointService.SERVICE_NAME);

    String portName = getConfiguration().getRequiredPropertyString(CONFIGURATION_PROPERTY_HARDWARE_SERIAL_PORT);

    xbee = service.newXBeeCommunicationEndpoint(portName, getLog());

    xbee.addListener(new XBeeResponseListenerSupport() {
      @Override
      public void onRxXBeeResponse(XBeeCommunicationEndpoint endpoint, RxResponseXBeeFrame response) {
        ByteBuffer data = ByteBuffer.wrap(response.getReceivedData()).order(ByteOrder.LITTLE_ENDIAN);
        float yawl = data.getFloat(0);
        float pitch = data.getFloat(4);
        float roll = data.getFloat(8);
        //int accelX = data.getShort(12);
        //int accelY = data.getShort(14);
        //int accelZ = data.getShort(16);

        getLog().info(
            String.format("Yawl %f pitch %f roll %f", yawl, pitch, roll));
      }
    });

    addManagedResource(xbee);
  }
}
