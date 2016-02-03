package io.pivotal.dis.task;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.pivotal.dis.R;
import io.pivotal.dis.lines.Line;
import io.pivotal.dis.lines.LinesDataSource;

public class DisplayDisruptionsAsyncTask extends AsyncTask<Void, Void, List<Map<String, String>>> {
  private final LinesDataSource linesDataSource;
  private final ListView viewToUpdate;
  private boolean requestSuccessful = true;

  public DisplayDisruptionsAsyncTask(LinesDataSource linesDataSource, ListView viewToUpdate) {
    this.linesDataSource = linesDataSource;
    this.viewToUpdate = viewToUpdate;
  }

  @Override
  protected List<Map<String, String>> doInBackground(Void... params) {
    try {
      List<Line> disruptedLines = linesDataSource.getDisruptedLines();
      List<Map<String, String>> disruptedLinesForDisplay = new ArrayList<>();
      for (Line line : disruptedLines) {
        Map<String, String> map = new HashMap<>();
        map.put("name", line.getName());
        map.put("status", line.getStatus());
        if(line.getStartTime() != null) {
          map.put("startTime", "Started: " + line.getStartTime());
          map.put("endTime", "Ends: " + line.getEndTime());
        }
        disruptedLinesForDisplay.add(map);
      }
      return disruptedLinesForDisplay;
    } catch (SocketTimeoutException e) {
      requestSuccessful = false;
      return Collections.emptyList();
    } catch (UnknownHostException e) {
      requestSuccessful = false;
      return Collections.emptyList();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void onPostExecute(List<Map<String, String>> disruptedLines) {
    try {
      final SimpleAdapter linesAdapter = new SimpleAdapter(viewToUpdate.getContext(),
          disruptedLines,
          R.layout.line_view,
          new String[]{"name", "status", "startTime", "endTime"},
          new int[]{R.id.line_name, R.id.line_status, R.id.line_disruption_started_time, R.id.line_disruption_end_time});

      if (requestSuccessful) {
        viewToUpdate.setAdapter(linesAdapter);
        Activity activity = (Activity) viewToUpdate.getContext();
        TextView emptyListView = (TextView) activity.findViewById(R.id.message_view);
        emptyListView.setText(activity.getString(R.string.no_disruptions));
      } else {
        Activity activity = (Activity) viewToUpdate.getContext();
        TextView emptyListView = (TextView) activity.findViewById(R.id.message_view);
        emptyListView.setText(activity.getString(R.string.refresh_failed));
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
