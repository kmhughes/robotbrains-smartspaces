/*
  * Copyright (C) 2016 Keith M. Hughes
* Copyright (C) 2013 Google Inc.
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

package org.robotbrains.particle;

import io.smartspaces.activity.impl.web.BaseRoutableRosWebActivity;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * A Smart Spaces activity which controls a browser based particle system via a
 * route.
 *
 * @author Keith M. Hughes
 */
public class RoutableWebParticleActivity extends BaseRoutableRosWebActivity {

  @Override
  public void onActivitySetup() {
  }

  @Override
  public void onNewInputJson(String channelName, Map<String, Object> message) {
    // There is only 1 channel for this activity, so don't bother checking
    // which one it is.
    if (isActivated()) {
      int signal1 = (Integer) message.get("analog");
      int signal2 = (Integer) message.get("analog2");

      Map<String, Object> browserMessage = Maps.newHashMap();
      browserMessage.put("field1", signal1 - 250);
      browserMessage.put("field2", signal2 - 250);
      sendAllWebSocketJson(browserMessage);
    }
  }
}
