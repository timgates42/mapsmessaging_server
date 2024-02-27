package io.mapsmessaging.monitor.top.panes.server;

import com.googlecode.lanterna.graphics.TextGraphics;
import io.mapsmessaging.engine.system.impl.server.StatusMessage;
import io.mapsmessaging.monitor.top.formatters.ByteSizeFormatter;
import io.mapsmessaging.monitor.top.formatters.DecimalSizeFormatter;

public class StorageSizePanel extends ServerStatusUpdate {

  public StorageSizePanel(int row, int col, TextGraphics labelText, TextGraphics valueText) {
    super(row, col, "Disk Usage : ", labelText, valueText, new ByteSizeFormatter());
  }

  @Override
  public void update(StatusMessage statusMessage) {
    panel.update(statusMessage.getStorageSize());
  }
}