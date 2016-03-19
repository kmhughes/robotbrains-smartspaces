/*
 * Copyright (C) 2015 Keith M. Hughes.
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

package org.robotbrains.opensoundcontrol.routable.supercollider.activity;

import java.util.Map;

import io.smartspaces.activity.impl.ros.BaseRoutableRosActivity;
import io.smartspaces.service.control.opensoundcontrol.OpenSoundControlClientCommunicationEndpoint;
import io.smartspaces.service.control.opensoundcontrol.OpenSoundControlClientCommunicationEndpointService;
import io.smartspaces.util.SmartSpacesUtilities;

/**
 * A Smart Spaces activity that controls an Supercollider synthesizer from
 * analog signals coming over a route.
 *
 * @author Keith M. Hughes
 */
public class SuperColliderOpenSoundControlRoutableActivity extends BaseRoutableRosActivity {

  /**
   * Configuration property giving the host of the Supercollider sserver.
   */
  public static final String CONFIGURATION_PROPERTY_SUPERCOLLIDER_SERVER_HOST =
      "space.activity.supercollider.server.host";

  /**
   * Configuration property giving the maximum of the analog signal coming in.
   */
  public static final String CONFIGURATION_PROPERTY_ANALOG_MAX = "analog.max";

  /**
   * Configuration property giving the multipler for frequencies to be sent in
   * the OSC message.
   */
  public static final String CONFIGURATION_PROPERTY_OSC_SIGNAL_MULTIPLER = "osc.signal.multiplier";

  /**
   * Configuration property giving the base frequency for the OSC message.
   */
  public static final String CONFIGURATION_PROPERTY_OSC_FREQUENCY_BASE = "osc.frequency.base";

  /**
   * Message field that gives the analog signal.
   */
  public static final String MESSAGE_FIELD_ANALOG = "analog";

  /**
   * Message field that gives the analog signal.
   */
  public static final String MESSAGE_FIELD_ANALOG2 = "analog2";

  /**
   * The SuperCollider OSC address for loading a synthdef.
   * 
   * <p>
   * The argument is a string giving the file path to the synthdef file.
   */
  public static final String SUPERCOLLIDER_OSC_ADDRESS_SYNTHDEF_LOAD = "/d_load";

  /**
   * The SuperCollider OSC address for creating a synth.
   * 
   * <p>
   * The arguments are
   * 
   * <ul>
   * <li>A string giving the synthdef name</li>
   * <li>An integer giving the node number</li>
   * </ul>
   */
  public static final String SUPERCOLLIDER_OSC_ADDRESS_SYNTH_NEW = "/s_new";

  /**
   * The SuperCollider OSC address for setting the values on a node.
   * 
   * <p>
   * The arguments are first an integer giving the node ID. Then repeat for as
   * many arguments as need to be modified
   * 
   * <ul>
   * <li>A string giving the argument name</li>
   * <li>A float giving the argument value</li>
   * </ul>
   */
  public static final String SUPERCOLLIDER_OSC_ADDRESS_NODE_SET = "/n_set";

  /**
   * The SuperCollider OSC address for freeing a node.
   * 
   * <p>
   * The arguments are
   * 
   * <ul>
   * <li>The node ID to free</li>
   * </ul>
   * 
   * <p>
   * The arguments can be repeated for as many nodes as should be freed in the
   * call.
   */
  public static final String SUPERCOLLIDER_OSC_ADDRESS_NODE_FREE = "/n_free";

  /**
   * The name for the synth.s
   */
  public static final String SYNTH_NAME = "SmartSpacesExample";

  /**
   * The synthdef to load. It is found in the install directory.
   */
  public static final String SYNTH_DEF_LOCATION =
      "${activity.installdir}/" + SYNTH_NAME + ".scsyndef";

  /**
   * The synth argument for controlling the frequency.
   */
  public static final String SYNTH_ARG_FREQUENCY = "freq";

  /**
   * The synth argument for controlling the volume.
   */
  public static final String SYNTH_ARG_VOLUME = "volume";

  /**
   * The node ID for the node to be used for the synth.
   */
  public static final int NODE_ID = 1000;

  /**
   * The communication endpoint for speaking to the OSC service.
   */
  private OpenSoundControlClientCommunicationEndpoint controlEndpoint;

  /**
   * The multiplier for the signal being sent to the OSC address.
   */
  private float signalMultiplier;

  /**
   * The base frequency for the OSC oscillator.
   */
  private float frequencyBase;

  /**
   * The maximum value seen on the analog2 signal.
   */
  private float analog2Maximum = 0;

  @Override
  public void onActivitySetup() {

    OpenSoundControlClientCommunicationEndpointService endpointService =
        getSpaceEnvironment().getServiceRegistry()
            .getRequiredService(OpenSoundControlClientCommunicationEndpointService.SERVICE_NAME);

    String remoteHost = getConfiguration()
        .getRequiredPropertyString(CONFIGURATION_PROPERTY_SUPERCOLLIDER_SERVER_HOST);
    int remotePort = 57110;

    controlEndpoint = endpointService.newUdpEndpoint(remoteHost, remotePort, getLog());
    addManagedResource(controlEndpoint);

    frequencyBase = getConfiguration()
        .getRequiredPropertyDouble(CONFIGURATION_PROPERTY_OSC_FREQUENCY_BASE).floatValue();
    signalMultiplier = getConfiguration()
        .getRequiredPropertyDouble(CONFIGURATION_PROPERTY_OSC_SIGNAL_MULTIPLER).floatValue()
        / getConfiguration().getRequiredPropertyInteger(CONFIGURATION_PROPERTY_ANALOG_MAX);
  }

  @Override
  public void onActivityActivate() {
    allocateSynth();
  }

  @Override
  public void onActivityDeactivate() {
    freeSynth();
  }

  @Override
  public void onActivityCleanup() {
    freeSynth();
  }

  @Override
  public void onNewInputMessage(String channelName, Map<String, Object> message) {
    getLog().debug("Got message on input channel " + channelName);
    int analog1 = (Integer) message.get(MESSAGE_FIELD_ANALOG);
    int analog2 = (Integer) message.get(MESSAGE_FIELD_ANALOG2);

    if (isActivated()) {
      sendOscMessage(analog1, analog2);
    }
  }

  /**
   * Send an OSC packet.
   *
   * @param analog1
   *          the first analog signal to send
   * @param analog2
   *          the second analog signal to send
   */
  private void sendOscMessage(float analog1, float analog2) {
    // The second analog signal should be a value from 0 to 1
    if (analog2 > analog2Maximum) {
      analog2Maximum = analog2;
    }

    float frequency = analog1 * signalMultiplier + frequencyBase;
    float volume = analog2 / analog2Maximum;

    controlEndpoint.sendRequestMessage(SUPERCOLLIDER_OSC_ADDRESS_NODE_SET, NODE_ID,
        SYNTH_ARG_FREQUENCY, frequency, SYNTH_ARG_VOLUME, volume);
  }

  /**
   * Allocate the synth.
   */
  private void allocateSynth() {
    String synthDefLocation = getConfiguration().evaluate(SYNTH_DEF_LOCATION);
    getLog().info(synthDefLocation);
    controlEndpoint.sendRequestMessage(SUPERCOLLIDER_OSC_ADDRESS_SYNTHDEF_LOAD, synthDefLocation,
        NODE_ID);

    // TODO(keith): Get sequencer done!
    getManagedCommands().submit(new Runnable() {
      @Override
      public void run() {
        SmartSpacesUtilities.delay(500);
        controlEndpoint.sendRequestMessage(SUPERCOLLIDER_OSC_ADDRESS_SYNTH_NEW, SYNTH_NAME,
            NODE_ID);
      }
    });
  }

  private void freeSynth() {
    controlEndpoint.sendRequestMessage(SUPERCOLLIDER_OSC_ADDRESS_NODE_FREE, NODE_ID);
  }
}
