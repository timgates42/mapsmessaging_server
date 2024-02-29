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

package io.mapsmessaging.monitor.top.panes.destination;

import com.googlecode.lanterna.graphics.TextGraphics;
import io.mapsmessaging.monitor.top.formatters.Formatter;
import io.mapsmessaging.monitor.top.panels.Panel;
import io.mapsmessaging.monitor.top.panels.StringPanel;
import io.mapsmessaging.rest.data.destination.DestinationStatus;

public abstract class DestinationStatusUpdate {

  protected final Panel panel;

  protected DestinationStatusUpdate(int row, int col, String label, TextGraphics labelText, TextGraphics valueText, Formatter formatter) {
    panel = new StringPanel(row, col, label, labelText, valueText, formatter);
  }

  public abstract void update(DestinationStatus statusMessage);

}