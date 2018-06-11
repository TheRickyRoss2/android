package com.seads.seadsv3.graph;


import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextClock;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.seads.seadsv3.R;
import com.seads.seadsv3.SeadsAppliance;
import com.seads.seadsv3.http.WebInterface;
import com.seads.seadsv3.http.WebInterfacer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


/**
 *   Dymanic real-time updated chart fragment used in conjunction with DynamicChartActivity and
 *   DemoBase.
 */

public class RoomVisualizationFragment extends Fragment implements WebInterface {

    private LineChart mChart;
    private final long DAY_INT = 86400;
    private Spinner mSpinner;
    private WebInterfacer webInterfacer;
    private int indexCount;
    private String panel;
    public HashMap<Integer, String> data_point_date_map;
    private ProgressBar progressBar;
    private TextView avg_energy;
    private TextView max_energy;
    private SeadsAppliance seadsAppliance;
    int day, week;
    long startTime;
    /**
     * Populate the layout with the chart and instantiate data aggregation
     * @param inflater Base layout
     * @param container Which view this belongs to
     * @param savedInstanceState Arguments passed to fragment
     * @return Populated view
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_fragment_5, container, false);
        mChart = (LineChart) v.findViewById(R.id.chart1);
        panel = getArguments().getString("Panel");
        seadsAppliance = getArguments().getParcelable("seads");
        day = getArguments().getInt("Day");
        week = getArguments().getInt("Week");
        data_point_date_map = new HashMap<>();
        progressBar = v.findViewById(R.id.daily_progress);
        mChart.setNoDataText("");
        mChart.setNoDataTextColor(Color.WHITE);
        // enable description text
        mChart.getDescription().setEnabled(false);
        startTime = CostCalculator.getDay(week, day);


        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);
        mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        IAxisValueFormatter iAxisValueFormatter = new HourAxisFormatter(startTime);
        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);
        mChart.getXAxis().setValueFormatter(iAxisValueFormatter);

        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);
        // add empty data
        mChart.setData(data);
        YAxis rightAxis = mChart.getAxisRight();
        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawGridLines(false);
        rightAxis.setDrawGridLines(false);
        mChart.getAxisLeft().setDrawGridLines(false);
        //xAxis.
        rightAxis.setEnabled(true);

        //set button

        webInterfacer = new WebInterfacer(this);
        mChart.setClickable(false);

        getData();
        //test.getJSONObject();

        return v;
    }

    /**
     * Creates the x axis formatted in a more user-friendly readable manner
     * @param start_time Time of start in unix milliseconds
     * @param end_time Time of end in unix milliseconds
     * @param granularity space between points in seconds
     * @param nPoints How many points there are in this dataset
     * @param type Hour, Day, Date of month
     */
    public void fillXAxis(long start_time, long end_time, int granularity, int nPoints, int type){
        Log.d("Viz/Time", new Date(start_time).toString());
        Log.d("Viz/Time", new Date(start_time).toString().split(" ")[3]);
        data_point_date_map.clear();
        long current_time = start_time;
        switch (type){
            case 0:
                for(int i = 0;i<nPoints; i++){
                    String hour_min_sec= new Date(current_time).toString().split(" ")[3];
                    hour_min_sec = hour_min_sec.split(":")[0] + ":" +hour_min_sec.split(":")[1];
                    this.data_point_date_map.put(i, hour_min_sec);
                    current_time = current_time+(granularity*1000);
                }
                break;
            case 1:
                for(int i = 0;i<nPoints; i++){
                    String hour_min_sec= new Date(current_time).toString().split(" ")[0];
                    this.data_point_date_map.put(i, hour_min_sec);
                    current_time = current_time+(granularity*1000);
                }
                break;
            case 2:
                for(int i = 0;i<nPoints; i++){
                    String[] hour_min_sec= new Date(current_time).toString().split(" ");
                    String dates = hour_min_sec[1] + " " +hour_min_sec[2];
                    this.data_point_date_map.put(i, dates);
                    current_time = current_time+(granularity*1000);
                }
                break;
        }
    }

    public void getData(){
        long current_time = System.currentTimeMillis();

        webInterfacer.getJSONObject(
                startTime,
                startTime+DAY_INT>current_time?current_time:startTime+DAY_INT,
                "energy",
                60/**/,
                this.seadsAppliance.getQueryId(),
                "P",
                this.seadsAppliance.getSeadsId()
        );

    }

    /**
     * What to do when we get a response from the server
     * In this case we populate the chart with the data we get back,
     * taking into account the different granularies and formattings
     * @param result the result of the HTTP response
     */
    @Override
    public void onJSONRetrieved(JSONObject result){
        try{
            final JSONArray data= result.getJSONArray("data");
            Log.d("JSON", ""+data.length());
            JSONObject index0 = data.getJSONObject(0);
            Float energy_values[] = new Float[data.length()];
            LineData lineData = mChart.getData();
            lineData.removeDataSet(0);
            ILineDataSet iLineDataSet = lineData.getDataSetByIndex(0);
            if(iLineDataSet == null){
                iLineDataSet = createSet();
                lineData.addDataSet(iLineDataSet);
            }
            double average = 0;
            double peak = 0;

            for(int i = energy_values.length-1; i>=0; i--) {
                energy_values[i] = Float.parseFloat(data.getJSONObject(i).getString("energy"));
                lineData.addEntry(new Entry(energy_values.length-i-1, energy_values[i]), 0);
                if (energy_values[i] > peak)
                    peak = energy_values[i];
                average += energy_values[i];
            }

            //textView_Peak.setText("Peak:\n"+truncate(""+peak)+"kW");
            //textView_Average.setText("Avg\n"+truncate(""+average)+"kW");
            lineData.notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(2000);
            /*
            mChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    DateFormat dateFormat = new SimpleDateFormat("hh:mm", Locale.getDefault());
                    Log.d("FORMAT", startTime+":"+value);
                    return dateFormat.format(new Date(startTime+(long)value));
                }
            });
            */
            mChart.moveViewToX(lineData.getEntryCount());
            progressBar.setVisibility(View.INVISIBLE);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Old method for random data. Do not use, we have real data now
     */
    @Deprecated
    private void addEntry() {
        LineData data = mChart.getData();
        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(120);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());
        }
    }

    /**
     * Creates the dataset for filling the chart
     * @return LineDataSet for use in the chart
     */
    public LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Energy Usage");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(Color.GREEN);
        set.setDrawCircles(false);
        set.setLineWidth(0.5f);
        set.setHighLightColor(Color.rgb(0, 0, 0));
        set.setDrawValues(false);
        set.setDrawFilled(true);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.fade_green);
        set.setFillDrawable(drawable);
        set.setDrawHighlightIndicators(false);
        return set;
    }

}