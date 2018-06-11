package com.seads.seadsv3.graph;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Home on 4/17/18.
 */

public class CostCalculator {
    private static final long DAY_INT = 86400;

    private static double[] hourlyCost = {
            0.17918, 0.17918, 0.17918, // 12am 1am 2am
            0.17918, 0.17918, 0.17918, // 3am 4am 5am
            0.17918, 0.17918, 0.17918, // 6am 7am 8am
            0.17918, 0.25596 , 0.25596 , // 9am 10am 11am
            0.25596 , 0.37123, 0.37123, // 12pm 1pm 2pm
            0.37123, 0.37123, 0.37123, // 3pm 4pm 5pm
            0.37123, 0.25596 , 0.25596 , // 6pm 7pm 8pm
            0.25596 , 0.17918, 0.17918  // 9pm 10pm 11pm
    };

    @Deprecated
    protected static double energyCost(double total_energy){
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.HOUR_OF_DAY;
        double total_cost = total_energy*hourlyCost[hour];
        Log.d("Current Hour", hour+"");
        Log.d("Current Cost", total_energy * hourlyCost[hour]+"");
        return (total_cost);
    }

    protected static double energyCost(ArrayList<DataPoint> dataPoints){
        double totalCost = 0;
        for(DataPoint dataPoint : dataPoints){
            totalCost += hourlyCost[dataPoint.getTime()] * dataPoint.getEnergy();
        }
        return totalCost;
    }

    protected static double avgEnergy(ArrayList<DataPoint> dataPoints){
        double avgEnergy = 0;
        for(DataPoint dataPoint : dataPoints){
            avgEnergy += dataPoint.getEnergy();
        }
        return avgEnergy;
    }

    protected static long getFirstDayMonth(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        //Log.d("CostCalc", ""+getMidnight(cal.getTimeInMillis()/1000));
        return getMidnight(cal.getTimeInMillis()/1000);
    }

    protected static long getFirstDayWeek(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        //Log.d("CostCalc", ""+getMidnight(cal.getTimeInMillis()/1000));
        return getMidnight(cal.getTimeInMillis()/1000);
    }

    private static long getMidnight(long time){
        return time-time%DAY_INT;
    }

    protected  static int getHourFromDateTime(String dateTime){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        Date date;
        try {
            date = dateFormat.parse(dateTime);
            long time = date.getTime()/1000;
            return (int)((time%DAY_INT)/3600);
        }catch (ParseException e){
            e.printStackTrace();
        }
        return 0;
    }

    protected static long getFirstDayWeek(int weekNumber){
        long baseMonth = CostCalculator.getFirstDayMonth();
        return baseMonth+DAY_INT*weekNumber*7;

    }

    protected static long getDay(int weekNumber, int dayNumber){
        long baseMonth = CostCalculator.getFirstDayMonth();
        return baseMonth+DAY_INT*weekNumber*7+dayNumber*DAY_INT;
    }
}
