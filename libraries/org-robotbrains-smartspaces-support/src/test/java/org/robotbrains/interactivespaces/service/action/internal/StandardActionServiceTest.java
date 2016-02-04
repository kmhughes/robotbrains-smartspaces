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
package org.robotbrains.interactivespaces.service.action.internal;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.robotbrains.smartspaces.service.action.Action;
import org.robotbrains.smartspaces.service.action.ActionSource;
import org.robotbrains.smartspaces.service.action.BasicGroupActionReference;
import org.robotbrains.smartspaces.service.action.BasicSimpleActionReference;
import org.robotbrains.smartspaces.service.action.GroupActionReference;
import org.robotbrains.smartspaces.service.action.SimpleActionReference;
import com.google.common.collect.Maps;

/**
 * Test the {@link StandardActionService}.
 * 
 * @author Keith M. Hughes
 */
public class StandardActionServiceTest {
  private StandardActionService actionService;

  @Before
  public void setup() {
    actionService = new StandardActionService();
  }

  @Test
  public void testSuccessfulFind() {
    ActionSource source = Mockito.mock(ActionSource.class);

    String actionName = "bar";
    Action action = Mockito.mock(Action.class);
    Mockito.when(source.getAction(actionName)).thenReturn(action);

    String sourceName = "foo";
    actionService.addActionSource(sourceName, source);

    Map<String, Object> data = Maps.newHashMap();
    actionService.performAction(sourceName, actionName, data);

    Mockito.verify(action, Mockito.times(1)).perform(data);

  }

  @Test
  public void testSuccessfulFindReference() {
    ActionSource source = Mockito.mock(ActionSource.class);

    String actionName = "bar";
    Action action = Mockito.mock(Action.class);
    Mockito.when(source.getAction(actionName)).thenReturn(action);

    String sourceName = "foo";
    actionService.addActionSource(sourceName, source);

    Map<String, Object> data = Maps.newHashMap();

    SimpleActionReference actionReference =
        new BasicSimpleActionReference("ref1", "test1", sourceName, null, actionName);
    actionService.performActionReference(actionReference, data);

    Mockito.verify(action, Mockito.times(1)).perform(data);

  }

  /**
   * Test creating a simple reference which merges data.
   */
  @Test
  public void testSuccessfulDataFromSimpleReference() {
    ActionSource source1 = Mockito.mock(ActionSource.class);

    String actionName1 = "bar";
    Action action1 = Mockito.mock(Action.class);
    Mockito.when(source1.getAction(actionName1)).thenReturn(action1);

    String sourceName1 = "foo";
    actionService.addActionSource(sourceName1, source1);

    Map<String, Object> referenceData1 = Maps.newHashMap();
    referenceData1.put("a", "b");
    referenceData1.put("c", "d");

    SimpleActionReference actionReference1 =
        new BasicSimpleActionReference("ref1", "test", sourceName1, null, actionName1,
            referenceData1);

    // Overwrite one value from the reference data.
    Map<String, Object> callData = Maps.newHashMap();
    callData.put("e", "f");
    callData.put("c", "g");

    actionService.performActionReference(actionReference1, callData);

    // Calculate the merge, which is reference data possibly overwritten by call
    // data.
    Map<String, Object> mergedData1 = Maps.newHashMap(referenceData1);
    mergedData1.putAll(callData);

    Mockito.verify(action1, Mockito.times(1)).perform(mergedData1);
  }

  /**
   * Test creating a group reference which merges data.
   */
  @Test
  public void testSuccessfulDataFromGroupReference() {
    ActionSource source1 = Mockito.mock(ActionSource.class);

    String actionName1 = "bar";
    Action action1 = Mockito.mock(Action.class);
    Mockito.when(source1.getAction(actionName1)).thenReturn(action1);

    String sourceName1 = "foo";
    actionService.addActionSource(sourceName1, source1);

    Map<String, Object> referenceData1 = Maps.newHashMap();
    referenceData1.put("a1", "b1");
    referenceData1.put("c", "d");
    referenceData1.put("g", "h");

    SimpleActionReference actionReference1 =
        new BasicSimpleActionReference("ref1", "test", sourceName1, null, actionName1,
            referenceData1);

    ActionSource source2 = Mockito.mock(ActionSource.class);

    String actionName2 = "bar2";
    Action action2 = Mockito.mock(Action.class);
    Mockito.when(source2.getAction(actionName2)).thenReturn(action2);

    String sourceName2 = "foo2";
    actionService.addActionSource(sourceName2, source2);

    Map<String, Object> referenceData2 = Maps.newHashMap();
    referenceData2.put("a2", "b2");
    referenceData2.put("c", "d");
    referenceData2.put("g", "i");

    SimpleActionReference actionReference2 =
        new BasicSimpleActionReference("ref2", "test", sourceName2, null, actionName2,
            referenceData2);

    Map<String, Object> groupReferenceData = Maps.newHashMap();
    groupReferenceData.put("g", "m");
    groupReferenceData.put("h", "i");
    groupReferenceData.put("j", "k");

    GroupActionReference groupReference =
        new BasicGroupActionReference("group1", "test", groupReferenceData).addActionReference(
            actionReference1, actionReference2);

    // Overwrite one value from the reference data.
    Map<String, Object> callData = Maps.newHashMap();
    callData.put("e", "f");
    callData.put("c", "g");
    callData.put("h", "q");

    actionService.performActionReference(groupReference, callData);

    // Calculate the merge, which is reference data possibly overwritten by
    // group data and then call
    // data.
    Map<String, Object> mergedData1 = Maps.newHashMap(referenceData1);
    mergedData1.putAll(groupReferenceData);
    mergedData1.putAll(callData);

    Map<String, Object> mergedData2 = Maps.newHashMap(referenceData2);
    mergedData2.putAll(groupReferenceData);
    mergedData2.putAll(callData);

    Mockito.verify(action1, Mockito.times(1)).perform(mergedData1);
    Mockito.verify(action2, Mockito.times(1)).perform(mergedData2);
  }
}
