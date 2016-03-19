/*
 * Copyright (C) 2015 Keith M. Hughes
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

package robotbrains.activity.analog.philipshue;

import java.util.Map;

import io.smartspaces.activity.impl.ros.BaseRoutableRosActivity;
import io.smartspaces.event.trigger.SimpleThresholdTrigger;
import io.smartspaces.event.trigger.Trigger;
import io.smartspaces.event.trigger.TriggerEventType;
import io.smartspaces.event.trigger.TriggerListener;
import io.smartspaces.event.trigger.TriggerState;

/**
 * A Smart Spaces Java-based activity that controls a Philips hue device from a
 * route.
 *
 * @author Keith M. Hughes
 */
public class PhilipsHueAnalogTriggerActivity extends BaseRoutableRosActivity {

  /**
   * Configuration property giving the host of the Philips Hue Hib.
   */
  public static final String CONFIGURATION_PROPERTY_PHILIPSHUE_SERVER_HOST =
      "space.activity.philipshue.server.host";

  /**
   * Configuration property giving the port of the OSC server.
   */
  public static final String CONFIGURATION_PROPERTY_PHILIPSHUE_HUB_USER =
      "space.activity.philipshue.hub.user";

  /**
   * The name of the config property that gives the threshold value for
   * triggering.
   */
  public static final String CONFIGURATION_PROPERTY_TRIGGER_THRESHOLD =
      "activity.example.arduino.analog.trigger.threshold";

  /**
   * The name of the config property that gives how much fuzz is allowed for the
   * trigger threshold.
   */
  public static final String CONFIGURATION_PROPERTY_TRIGGER_FUZZ =
      "activity.example.arduino.analog.trigger.fuzz";

  /**
   * Message field that gives the analog signal.
   */
  public static final String MESSAGE_FIELD_ANALOG = "analog";

  /**
   * Message field that gives the analog signal.
   */
  public static final String MESSAGE_FIELD_ANALOG2 = "analog2";

  /**
   * A threshold trigger to detect when the trigger has gotten to a certain
   * value.
   */
  private SimpleThresholdTrigger trigger;

  /**
   * Endpoint for the philips Hue Hub.
   */
  private PhilipsHueEndpoint philipsHueHubEndpoint;

  @Override
  public void onActivitySetup() {

    String philipsHueHubHost =
        getConfiguration().getRequiredPropertyString(CONFIGURATION_PROPERTY_PHILIPSHUE_SERVER_HOST);
    String philipsHueHubUser =
        getConfiguration().getRequiredPropertyString(CONFIGURATION_PROPERTY_PHILIPSHUE_HUB_USER);

    PhilipsHueEndpointService service = getSpaceEnvironment().getServiceRegistry()
        .getRequiredService(PhilipsHueEndpointService.SERVICE_NAME);
    philipsHueHubEndpoint = service.newEndpoint(philipsHueHubHost, philipsHueHubUser, getLog());
    addManagedResource(philipsHueHubEndpoint);

    trigger = new SimpleThresholdTrigger();
    trigger.addListener(new TriggerListener() {

      @Override
      public void onTrigger(Trigger trigger, TriggerState newState, TriggerEventType type) {
        getLog().info(String.format("The trigger has gone to %s with type %s", newState, type));

        handleTrigger(newState);
      }
    });

    int threshold =
        getConfiguration().getPropertyInteger(CONFIGURATION_PROPERTY_TRIGGER_THRESHOLD, 220);
    int fuzz = getConfiguration().getPropertyInteger(CONFIGURATION_PROPERTY_TRIGGER_FUZZ, 20);
    trigger.setThreshold(threshold).setHysteresis(fuzz);
  }

  @Override
  public void onActivityStartup() {
    philipsHueHubEndpoint.scanForLights();
  }

  @Override
  public void onActivityPostStartup() {
    changeLight(false);
  }

  @Override
  public void onActivityCleanup() {
    changeLight(false);
  }

  @Override
  public void onNewInputJson(String channelName, Map<String, Object> message) {
    getLog().info("Got message on input channel " + channelName);
    int analog1 = (Integer) message.get(MESSAGE_FIELD_ANALOG);
    int analog2 = (Integer) message.get(MESSAGE_FIELD_ANALOG2);

    if (isActivated()) {
      trigger.update(analog1);
    }
  }

  /**
   * Handle a trigger event.
   * 
   * @param newState
   *          new state of the trigger
   */
  private void handleTrigger(TriggerState newState) {
    boolean newLightState = TriggerState.TRIGGERED.equals(newState);
    changeLight(newLightState);
  }

  private void changeLight(boolean newLightState) {
    String lightId = "Hue Lamp 2";
    PhilipsHueLight light = philipsHueHubEndpoint.getLightByName(lightId);
    if (light != null) {
      light.setOn(newLightState);

      philipsHueHubEndpoint.updateLightState(light);
    } else {
      getLog().warn(String.format("Could not find light %s", lightId));
    }
  }
}
