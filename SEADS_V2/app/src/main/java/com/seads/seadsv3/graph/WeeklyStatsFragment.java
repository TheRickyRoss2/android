package com.seads.seadsv3.graph;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.seads.seadsv3.R;
import com.seads.seadsv3.SeadsAppliance;
import com.seads.seadsv3.http.WebInterface;
import com.seads.seadsv3.http.WebInterfacer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Home on 4/17/18.
 */

public class WeeklyStatsFragment extends Fragment implements WebInterface, OnChartValueSelectedListener {

    private BarChart mBarChart;
    private final long DAY_INT = 86400;
    private WebInterfacer webInterfacer;
    private int indexCount;
    private String panel;
    private int recv=0;
    private double week_data[];
    private ArrayList<BarEntry> barEntryList;
    public HashMap<Integer, String> data_point_date_map;
    private TextView daily_cost;
    private TextView yesterday_cost;
    private TextView last_week_cost;
    private ProgressBar progressBar;
    private int week;
    private SeadsAppliance seadsAppliance;
    private int mRequestCount=0;
    private ArrayList<Double> energyPoints;
    private  double cost1, cost2, cost3, energy1;
    /**
     * 1523923200
     * 86400
     * 43200
     * 1523059200
     */

    /**
     * Populate the layout with the chart and instantiate data aggregation
     * @param inflater Base layout
     * @param container Which view this belongs to
     * @param savedInstanceState Arguments passed to fragment
     * @return Populated view
     */

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.weekly_stats_fragment, container, false);
        mBarChart =  v.findViewById(R.id.WeeklyStatsChart);
        panel = getArguments().getString("Panel");
        seadsAppliance = getArguments().getParcelable("seads");
        week = getArguments().getInt("Week");
        cost1 = getArguments().getDouble("cost1");
        cost2 = getArguments().getDouble("cost2");
        cost3 = getArguments().getDouble("cost3");
        energy1 = getArguments().getDouble("energy1");
        mBarChart.setMaxVisibleValueCount(60);
        mBarChart.setDrawGridBackground(false);
        barEntryList = new ArrayList<>();
        mBarChart.setDrawBarShadow(false);
        webInterfacer = new WebInterfacer(this);
        progressBar =  v.findViewById(R.id.wWeekly_progress);
        progressBar.setVisibility(View.VISIBLE);
        week_data = new double[7];
        mBarChart.setOnChartValueSelectedListener(this);
        mBarChart.setNoDataText("");
        mBarChart.setNoDataTextColor(Color.WHITE);
        energyPoints = new ArrayList<>();

        mBarChart.setDrawBarShadow(true);
        mBarChart.getDescription().setEnabled(false);

        mBarChart.getAxisLeft().setTextSize(14f);
        mBarChart.getAxisRight().setTextSize(14f);
        mBarChart.getAxisRight().setDrawGridLines(false);
        mBarChart.getAxisLeft().setDrawGridLines(false);
        mBarChart.getAxisLeft().setDrawAxisLine(false);
        mBarChart.getAxisRight().setDrawAxisLine(false);

        XAxis xAxis = mBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return "Day "+(int)(value+1);
            }
        });




        setUpQuery();

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


    /**
     * Sets up drop down menu for selecting the data set to be analyzed
     */
    public void setUpQuery(){
        long current_time = System.currentTimeMillis()/1000;
        long start_time = CostCalculator.getFirstDayWeek(week);
        long end_time = (current_time-current_time%DAY_INT-21)/1000;
        boolean stillInPresent = true;
        IAxisValueFormatter iAxisValueFormatter = new DayAxisFormatter(start_time);
        mBarChart.getXAxis().setValueFormatter(iAxisValueFormatter);


        /*
        Get base energy for start of month
         */
        webInterfacer.getJSONObject(
                start_time,
                "energy",
                this.seadsAppliance.getQueryId(),
                "P",
                this.seadsAppliance.getSeadsId()
        );
        mRequestCount++;
        /*
        Generate requests for weeks until reach present time
         */
        while(stillInPresent && start_time<CostCalculator.getFirstDayWeek(week+1)){
            if(current_time < start_time+DAY_INT){
                end_time = current_time-60;
                stillInPresent = false;
            }else{
                end_time = start_time+DAY_INT;
            }
            webInterfacer.getJSONObject(
                    end_time,
                    "energy",
                    this.seadsAppliance.getQueryId(),
                    "P",
                    this.seadsAppliance.getSeadsId()
            );
            start_time = end_time;
            mRequestCount++;
            Log.d("WeeklyStatsFragment", "Sent:"+mRequestCount);
        }
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
            JSONArray data = result.getJSONArray("data");
            JSONObject index0 = data.getJSONObject(0);
            Log.d("Got:", ""+index0);

            if(mRequestCount!=-1) {
                double total_energy = index0.getDouble("energy");
                energyPoints.add(total_energy);
            }
            recv++;

            if(recv==mRequestCount) {

                mRequestCount =-1;
                ArrayList<Float> weekly_values= new ArrayList<>();
                double basePoint = energyPoints.get(0);
                for(int i = 1; i< energyPoints.size();i++){
                    double energyValue = Math.abs(basePoint-energyPoints.get(i));
                    basePoint = energyPoints.get(i);
                    weekly_values.add((float)(energyValue/(3600*1000.0)));
                    Log.d("WeeklyStatsFragment", "Energy:"+energyValue/(3600*1000.0));
                }
                for(float value : weekly_values){
                    barEntryList.add(new BarEntry(weekly_values.indexOf(value), value));
                }
                Collections.reverse(weekly_values);

                //d.invalidate();

                IBarDataSet iBarDataSet = new BarDataSet(barEntryList, "Power Data");
                iBarDataSet.setDrawIcons(false);
                ArrayList<IBarDataSet> dataSets = new ArrayList<>();
                dataSets.add(iBarDataSet);
                BarData barData = new BarData(dataSets);
                barData.setValueFormatter(new IValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                        return decimalFormat.format(value)+" kWh";
                    }
                });
                barData.setValueTextSize(10f);
                mBarChart.setData(barData);
                BarDataSet barDataSet = (BarDataSet) mBarChart.getData().getDataSetByIndex(0);
                barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

                mBarChart.setDrawBarShadow(false);
                mBarChart.setHighlightFullBarEnabled(false);
                mBarChart.invalidate();
                mBarChart.setClickable(true);
                progressBar.setVisibility(View.INVISIBLE);


            }
            Log.d("JSONRET", "done");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h){

        Log.d("Position", ""+e.getX());
        Bundle bundle = new Bundle();
        bundle.putString("Panel", panel);
        bundle.putInt("Day", (int)e.getX());
        bundle.putInt("Week", week);
        bundle.putParcelable("seads", this.seadsAppliance);
        Fragment fragment = new RoomVisualizationFragment();
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.weekly_layout, fragment);
        mBarChart.clear();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onNothingSelected(){}
}
