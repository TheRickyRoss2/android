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
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
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

public class MonthlyStatsFragment extends Fragment implements WebInterface, OnChartValueSelectedListener {

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
    private SeadsAppliance seadsAppliance;
    private ArrayList<Double> energyPoints;
    private int mRequestCount=0;
    private double cost1, cost2, cost3, energy1;
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
        View v = inflater.inflate(R.layout.monthly_stats_fragment, container, false);
        energyPoints = new ArrayList<>();
        mBarChart = (BarChart) v.findViewById(R.id.MonthlyStatsChart);
        mBarChart.setNoDataTextColor(Color.WHITE);
        mBarChart.setNoDataText("");
        daily_cost = v.findViewById(R.id.DailyCost);
        last_week_cost = v.findViewById(R.id.LastWeekCost);
        yesterday_cost = v.findViewById(R.id.YesterdayCost);
        panel = getArguments().getString("device");
        seadsAppliance = getArguments().getParcelable("seads");
        mBarChart.setMaxVisibleValueCount(60);
        mBarChart.setDrawGridBackground(false);
        barEntryList = new ArrayList<>();
        mBarChart.setDrawBarShadow(false);
        webInterfacer = new WebInterfacer(this);
        progressBar = (ProgressBar) v.findViewById(R.id.weekly_progress);
        week_data = new double[4];
        mBarChart.setOnChartValueSelectedListener(this);
        //test.getJSONObject();
        //mBarChart.setOnChartValueSelectedListener(this);
        mBarChart.setDrawBarShadow(true);
        mBarChart.getDescription().setEnabled(false);
        mBarChart.setClickable(false);

        XAxis xAxis = mBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1);
        xAxis.setValueFormatter(new TimeAxisFormatter());
        xAxis.setTextSize(16f);

        mBarChart.getAxisLeft().setTextSize(14f);
        mBarChart.getAxisRight().setTextSize(14f);
        mBarChart.getAxisRight().setDrawGridLines(false);
        mBarChart.getAxisLeft().setDrawGridLines(false);
        mBarChart.getAxisLeft().setDrawAxisLine(false);
        mBarChart.getAxisRight().setDrawAxisLine(false);

        mBarChart.invalidate();

        setUpQuery();

        Legend legend = mBarChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.BLACK);
        LegendEntry[] legendEntries = legend.getEntries();
        ArrayList<LegendEntry> legendEntryArrayList = new ArrayList<>();
        for(int i = 0; i< legendEntries.length; i++){
            LegendEntry legendEntry = legendEntries[i];
            legendEntry.formColor = Color.WHITE;
            legendEntry.label = "SDFA";
            legendEntryArrayList.add(legendEntry);
        }
        legend.setEntries(legendEntryArrayList);

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
        ArrayAdapter<CharSequence> adapter= ArrayAdapter.createFromResource(
                getContext(),
                R.array.time_selection,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        long current_time = System.currentTimeMillis()/1000;
        long start_time = CostCalculator.getFirstDayMonth();
        long end_time = (current_time-current_time%DAY_INT-21)/1000;
        Log.d("Timing", "start_time="+start_time);
        Log.d("Timing", "end_time="+end_time);
        Log.d("Timing", "granularity="+(end_time-start_time)/2);
        boolean stillInPresent = true;
        IAxisValueFormatter iAxisValueFormatter = new MonthAxisFormatter(start_time);
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
        while(stillInPresent){
            if(current_time < start_time+DAY_INT*7){
                end_time = current_time-60;
                stillInPresent = false;
            }else{
                end_time = start_time+DAY_INT*7;
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
            Log.d("MonthlyStatsFragment", "Sent:"+mRequestCount);
        }
        webInterfacer.getJSONObject(
                CostCalculator.getFirstDayMonth(),
                current_time-current_time%DAY_INT,
                "energy",
                60*60,
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
                    Log.d("MonthlyStatsFragment", "Energy:"+energyValue/(3600*1000.0));
                }
                for(float value : weekly_values){
                    BarEntry barEntry = new BarEntry(weekly_values.indexOf(value), value);
                    //barEntry.setX(CostCalculator.getFirstDayMonth());
                    barEntryList.add(barEntry);
                }
                Collections.reverse(weekly_values);

                //d.invalidate();
                IBarDataSet iBarDataSet = new BarDataSet(barEntryList, "Power Data");
                iBarDataSet.setDrawIcons(false);
                ArrayList<IBarDataSet> dataSets = new ArrayList<>();
                iBarDataSet.setLabel("Energy Data");
                iBarDataSet.setValueTextColor(Color.BLACK);
                iBarDataSet.setDrawIcons(false);
                iBarDataSet.setValueTextSize(18f);

                dataSets.add(iBarDataSet);
                BarData barData = new BarData(dataSets);
                barData.setValueTextSize(18f);
                barData.setValueFormatter(new IValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                        return decimalFormat.format(value)+" kWh";
                    }
                });
                mBarChart.setData(barData);
                BarDataSet barDataSet = (BarDataSet) mBarChart.getData().getDataSetByIndex(0);
                barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

                mBarChart.setDrawBarShadow(false);
                mBarChart.setHighlightFullBarEnabled(false);
                mBarChart.invalidate();
            }else if(mRequestCount == -1){
                mBarChart.setClickable(true);
                ArrayList<DataPoint> mDataPoints = new ArrayList<>();
                ArrayList<DataPoint> mWeekPoints = new ArrayList<>();
                ArrayList<DataPoint> mYstrPoints = new ArrayList<>();

                for(int i = 0; i<data.length();i++){
                    JSONObject dataPoint = data.getJSONObject(i);
                    int hour = CostCalculator.getHourFromDateTime(dataPoint.getString("time"));
                    mDataPoints.add(new DataPoint(hour, dataPoint.getDouble("energy")));
                }

                for(int i = 0;i<data.length();i++){
                    JSONObject dataPoint = data.getJSONObject(i);
                    int hour = CostCalculator.getHourFromDateTime(dataPoint.getString("time"));
                    mWeekPoints.add(new DataPoint(hour, dataPoint.getDouble("energy")));
                }

                for(int i = 0;i<24;i++){
                    JSONObject dataPoint = data.getJSONObject(i);
                    int hour = CostCalculator.getHourFromDateTime(dataPoint.getString("time"));
                    mYstrPoints.add(new DataPoint(hour, dataPoint.getDouble("energy")));
                }
                Log.d("Data Length:", data.length()+"");


                String daily_cost_string = "\n  Daily Usage\n\n  "+
                        new DecimalFormat("$#0").format(CostCalculator.energyCost(mDataPoints)/7.0)+
                        "\n\n               "+
                        new DecimalFormat("#0.00 kWh").format(CostCalculator.avgEnergy(mDataPoints)/7.0);
                cost1 = CostCalculator.energyCost(mDataPoints);
                energy1 = CostCalculator.avgEnergy(mDataPoints)/7.0;
                daily_cost.setTextSize(22f);
                daily_cost.setTextColor(Color.WHITE);
                daily_cost.setText(daily_cost_string);
                String last_week_cost_String = "\n    Last week\n\n    "+
                        new DecimalFormat("$#0").format(CostCalculator.energyCost(mWeekPoints));
                cost2 = CostCalculator.energyCost(mWeekPoints);

                last_week_cost.setTextSize(14f);
                last_week_cost.setTextColor(Color.WHITE);
                last_week_cost.setText(last_week_cost_String);
                String yesterday_cost_string = "\n    Yesterday\n\n    "
                        +new DecimalFormat("$#0").format(CostCalculator.energyCost(mYstrPoints));
                cost3 = CostCalculator.energyCost(mYstrPoints);

                yesterday_cost.setTextSize(14f);
                yesterday_cost.setTextColor(Color.WHITE);
                yesterday_cost.setText(yesterday_cost_string);
                progressBar.setVisibility(View.INVISIBLE);

            }
            //textView_Peak.setText("Peak:\n"+truncate(""+peak)+"kW");
            //textView_Average.setText("Avg\n"+truncate(""+average)+"kW");
            Log.d("JSONRET", "done");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h){
        if(mRequestCount!=-1){
            Log.d("Clicked", "WTF");
            return;
        }
        Log.d("Clicked", "WTFman"+mRequestCount);
        Log.d("Position", ""+e.getX());
        Bundle bundle = new Bundle();
        bundle.putString("Panel", panel);
        bundle.putInt("Week", (int)e.getX());
        bundle.putDouble("cost1",cost1);
        bundle.putDouble("cost2",cost2);
        bundle.putDouble("cost3",cost3);
        bundle.putDouble("energy1", energy1);
        bundle.putParcelable("seads", this.seadsAppliance);
        Fragment fragment = new WeeklyStatsFragment();
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.monthly_layout, fragment);
        mBarChart.clear();
        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commit();
    }

    @Override
    public void onNothingSelected(){}
}
