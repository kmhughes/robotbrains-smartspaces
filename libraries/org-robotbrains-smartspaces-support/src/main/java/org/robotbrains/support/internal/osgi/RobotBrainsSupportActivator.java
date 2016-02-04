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

package org.robotbrains.support.internal.osgi;

import io.smartspaces.osgi.service.SmartSpacesServiceOsgiBundleActivator;

import org.robotbrains.interactivespaces.service.action.internal.StandardActionService;
import org.robotbrains.interactivespaces.service.hardware.philipshue.internal.StandardPhilipsHueEndpointService;
import org.robotbrains.interactivespaces.service.scheduler.action.internal.quartz.QuartzActionSchedulerService;

/**
 * Bundle activator for Robotbrains support library.
 * 
 * @author Keith M. Hughes
 */
public class RobotBrainsSupportActivator extends SmartSpacesServiceOsgiBundleActivator {
  @Override
  protected void allRequiredServicesAvailable() {
    registerNewSmartSpacesService(new StandardActionService());
    registerNewSmartSpacesService(new StandardPhilipsHueEndpointService());
    registerNewSmartSpacesService(new QuartzActionSchedulerService());
  }
}
