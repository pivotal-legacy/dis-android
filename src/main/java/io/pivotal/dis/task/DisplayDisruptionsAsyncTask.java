package io.pivotal.dis.task;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.pivotal.dis.R;
import io.pivotal.dis.lines.Line;
import io.pivotal.dis.lines.LinesClient;

public class DisplayDisruptionsAsyncTask extends AsyncTask<Void, Void, List<Map<String, String>>> {
  private final LinesClient linesClient;
  private final ListView viewToUpdate;
  private boolean requestSuccessful = true;

  public DisplayDisruptionsAsyncTask(ListView viewToUpdate, LinesClient linesClient) {
    this.linesClient = linesClient;
    this.viewToUpdate = viewToUpdate;
  }

  @Override
  protected List<Map<String, String>> doInBackground(Void... params) {
    try {

      List<Line> disruptedLines = linesClient.fetchDisruptedLines();
      return buildDisruptedLinesForDisplay(disruptedLines);

    } catch (SocketTimeoutException | UnknownHostException | FileNotFoundException e) {
      requestSuccessful = false;
      return Collections.emptyList();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Unexpected error", e);
    }
  }

  @Override
  protected void onPostExecute(List<Map<String, String>> disruptedLines) {
    try {
      Activity activity = (Activity) viewToUpdate.getContext();
      TextView emptyListView = (TextView) activity.findViewById(R.id.message_view);

      if (requestSuccessful) {
        final LinesAdapter linesAdapter = new LinesAdapter(disruptedLines, viewToUpdate.getContext());


        viewToUpdate.setAdapter(linesAdapter);
        emptyListView.setText(activity.getString(R.string.no_disruptions));
      } else {
        emptyListView.setText(activity.getString(R.string.refresh_failed));
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Unexpected error", e);
    }
  }

  @NonNull
  private List<Map<String, String>> buildDisruptedLinesForDisplay(List<Line> disruptedLines) {
    List<Map<String, String>> disruptedLinesForDisplay = new ArrayList<>();
    for (Line line : disruptedLines) {
      Map<String, String> map = new HashMap<>();

      map.put("name", line.getName());
      map.put("status", line.getStatus());

      if (line.getStartTime() != null)
        map.put("startTime", "Started: " + line.getStartTime());

      if (line.getEndTime() != null)
        map.put("endTime", "Ends: " + line.getEarliestEndTime() + " - " + line.getLatestEndTime());

      map.put("backgroundColor", line.getBackgroundColor());
      map.put("foregroundColor", line.getForegroundColor());

      disruptedLinesForDisplay.add(map);
    }
    return disruptedLinesForDisplay;
  }

  private class LinesAdapter extends BaseAdapter {

    private List<Map<String, String>> disruptedLines;
    private Context context;

    public LinesAdapter(List<Map<String, String>> disruptedLines, Context context) {
      this.disruptedLines = disruptedLines;
      this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = LayoutInflater.from(context).inflate(R.layout.line_view, parent, false);

      TextView lineNameView = (TextView) view.findViewById(R.id.line_name);
      lineNameView.setText(disruptedLines.get(position).get("name"));

      TextView lineStatusView = (TextView) view.findViewById(R.id.line_status);
      lineStatusView.setText(disruptedLines.get(position).get("status"));

      TextView lineStartTimeView = (TextView) view.findViewById(R.id.line_disruption_started_time);
      lineStartTimeView.setText(disruptedLines.get(position).get("startTime"));

      TextView lineEndTimeView = (TextView) view.findViewById(R.id.line_disruption_end_time);
      lineEndTimeView.setText(disruptedLines.get(position).get("endTime"));

      String backgroundColor = normalizeColor(disruptedLines.get(position).get("backgroundColor"));
      view.setBackgroundColor(Color.parseColor(backgroundColor));

      String foregroundColor = normalizeColor(disruptedLines.get(position).get("foregroundColor"));
      lineNameView.setTextColor(Color.parseColor(foregroundColor));

      return view;
    }

    private String normalizeColor(String backgroundColor) {
      if (backgroundColor.length() == 7) {
        return backgroundColor;
      } else if (backgroundColor.length() == 4) {
        StringBuilder builder = new StringBuilder();
        builder.append("#");

        builder.append(backgroundColor.charAt(1));
        builder.append(backgroundColor.charAt(1));
        builder.append(backgroundColor.charAt(2));
        builder.append(backgroundColor.charAt(2));
        builder.append(backgroundColor.charAt(3));
        builder.append(backgroundColor.charAt(3));
        return builder.toString();
      } else {
        throw new IllegalArgumentException("Unknown color: " + backgroundColor);
      }
    }

    @Override
    public int getCount() {
      return disruptedLines.size();
    }

    @Override
    public Object getItem(int position) {
      return null;
    }

    @Override
    public long getItemId(int position) {
      return 0;
    }
  }
}
