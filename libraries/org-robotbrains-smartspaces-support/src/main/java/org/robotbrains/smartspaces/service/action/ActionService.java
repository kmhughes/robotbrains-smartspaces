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
package org.robotbrains.smartspaces.service.action;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.resource.Version;
import io.smartspaces.resource.VersionRange;
import io.smartspaces.service.SupportedService;

import java.util.Map;

/**
 * A service for working with actions.
 * 
 * @author Keith M. Hughes
 */
public interface ActionService extends SupportedService {

  /**
   * The name of the service.
   */
  String SERVICE_NAME = "action";

  /**
   * The source to use for groups.
   */
  String GROUP_ACTION_SOURCE = "interactivespaces.service.action";

  /**
   * Add in a new source for actions.
   * 
   * <p>
   * The action source gets the {@link #DEFAULT_VERSION}.
   * 
   * @param sourceName
   *          the name of the source
   * @param source
   *          the source
   */
  void addActionSource(String sourceName, ActionSource source);

  /**
   * Add in a new source for actions.
   * 
   * @param sourceName
   *          the name of the source
   * @param sourceVersion
   *          the source of the version
   * @param source
   *          the source
   */
  void addActionSource(String sourceName, Version sourceVersion, ActionSource source);

  /**
   * Perform an action.
   * 
   * <p>
   * The action is looked up. An exception will be thrown if a source cannot be
   * found with the given name or an action with the given name.
   * 
   * @param sourceName
   *          the name of the action source
   * @param actionName
   *          the name of the action
   * @param data
   *          the data for the action
   */
  void performAction(String sourceName, String actionName, Map<String, ? extends Object> data)
      throws SmartSpacesException;

  /**
   * Perform the given action reference.
   * 
   * @param actionReference
   *          the action reference
   * @param data
   *          the data for the action invocation
   */
  void performActionReference(ActionReference actionReference, Map<String, ? extends Object> data);

  /**
   * Perform the given simple action reference.
   * 
   * @param actionReference
   *          the simple action reference
   * @param data
   *          the data for the action invocation
   */
  void performSimpleActionReference(SimpleActionReference actionReference,
      Map<String, ? extends Object> data);

  /**
   * Perform the given group action reference.
   * 
   * @param actionReference
   *          the group action reference
   * @param data
   *          the data for the action invocation
   */
  void performGroupActionReference(GroupActionReference actionReference,
      Map<String, ? extends Object> data);

  /**
   * Perform an action.
   * 
   * @param actionSourceName
   *          the source name for the action
   * @param actionName
   *          the name of the action
   * @param versionRange
   *          the version range for the action
   * @param data
   *          the data for the action
   */
  void performAction(String actionSourceName, String actionName, VersionRange versionRange,
      Map<String, ? extends Object> data);
}