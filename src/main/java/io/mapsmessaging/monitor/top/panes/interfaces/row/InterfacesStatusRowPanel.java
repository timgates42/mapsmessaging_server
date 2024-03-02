/*
 * Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.mapsmessaging.monitor.top.panes.interfaces.row;

import com.googlecode.lanterna.graphics.TextGraphics;
import io.mapsmessaging.engine.system.impl.server.InterfaceStatusTopic;
import io.mapsmessaging.monitor.top.panes.destination.DestinationStatusUpdate;
import io.mapsmessaging.monitor.top.panes.destination.row.DestinationDiskPanel;
import io.mapsmessaging.monitor.top.panes.destination.row.DestinationMetricsPanel;
import io.mapsmessaging.monitor.top.panes.destination.row.DestinationNamePanel;
import io.mapsmessaging.monitor.top.panes.interfaces.InterfacesStatusUpdate;
import io.mapsmessaging.rest.data.destination.DestinationStatus;
import io.mapsmessaging.rest.data.interfaces.InterfaceStatus;

import java.util.ArrayList;
import java.util.List;

public class InterfacesStatusRowPanel {

  private final List<InterfacesStatusUpdate> rowItems;

  public InterfacesStatusRowPanel(int row, TextGraphics labelText, TextGraphics valueText) {
    rowItems = new ArrayList<>();
    rowItems.add(new InterfaceNamePanel(0, row, labelText, valueText));
    rowItems.add(new InterfaceMetricsPanel(32, row, labelText, valueText));
  }

  public void update(InterfaceStatus statusMessage) {
    for (InterfacesStatusUpdate update : rowItems) {
      update.update(statusMessage);
    }
  }
}