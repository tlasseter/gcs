//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.Utilities;

import java.io.*;

public class StsMilliTimer extends StsTimer
 {

     public StsMilliTimer constructor()
     {
         return new StsMilliTimer();
     }

     static public StsMilliTimer constructor(String name)
     {
         return new StsMilliTimer(name);
     }

     public StsMilliTimer()
     {
         // 		timer = new MicroTimer();
     }

     StsMilliTimer(String name)
     {
         this.name = name;
         if (debug) System.out.println("Created millisec timer " + name);
     }

     public void printIntervalElapsedTimes()
     {
         printIntervalElapsedTimes = true;
     }

     public void reset()
     {
         startTime = System.currentTimeMillis();
         elapsedTime = 0.0;
         //		timer.start();
     }

     public void start()
     {
         startTime = System.nanoTime();
         //		timer.start();
     }

     public void stopPrint(String s)
     {
         stop();
         System.out.println(s + " " + getTimeString());
     }

     public void stopPrintReset(String s)
     {
         stop();
         System.out.println(s + " " + getTimeString());
         elapsedTime = 0.0;
     }

     public String getTimeString()
     {
         return getTimeString(elapsedTime);
     }

     static public String getTimeString(double time)
     {
         if (time > 1.e9)
             return new String(" " + time * 1.e-9 + " secs");
         else if (time > 1.e6)
             return new String(" " + time * 1.e-6 + " msecs");
         else if (time > 1.e3)
             return new String(" " + time * 1.e-3 + " microsecs");
         else
             return new String(" " + time + " nanosecs");
     }

     public String getElapsedTimeString()
     {
         return getTimeString(elapsedTime);
     }

     public String getIntervalAvgString()
     {
         if (count <= 1) return "";

         double intervalAvg = elapsedTime / count;
         String timeString = getTimeString(intervalAvg);
         return timeString + " avg";
     }

     public double stop()
     {
         stopTime = System.nanoTime();
         elapsedTime = stopTime - startTime;
         return elapsedTime;
     }

     public double stopAccumulate()
     {
         stopTime = System.nanoTime();
         intervalElapsedTime = stopTime - startTime;
         elapsedTime += intervalElapsedTime;
         return elapsedTime;
     }

     public double stopAccumulateIncrementCount()
     {
         count++;
         return stopAccumulate();
     }

     public double stopAccumulateAddCount(int increment)
     {
         addCount(increment);
         return stopAccumulate();
     }

     public void addCount(int increment)
     {
         count += increment;
     }

     public double stopAccumulateIncrementCountPrintInterval(String s)
     {
         stopAccumulateIncrementCount();
         printIntervalElapsedTime(s);
         return elapsedTime;
     }

     public void printIntervalElapsedTime()
     {
         System.out.println("    " + name + getElapsedTimeString() + getIntervalAvgString());
     }

     public void printIntervalElapsedTime(String s)
     {
         System.out.println("    " + name + " " + s + " " + getElapsedTimeString() + getIntervalAvgString());
     }

     public void printElapsedTime()
     {
         System.out.println("    " + name + getElapsedTimeString() + " Count: " + count + getIntervalAvgString());
     }

     public double getElapsedTime()
     {
         return elapsedTime;
     }

     public void restartElapsedTime()
     {
         elapsedTime = 0.0;
         start();
     }

     public void clear()
     {
         elapsedTime = 0.0;
         count = 0;
     }

     public void stop(PrintWriter out, String s)
     {
         //    	stopTime = System.currentTimeMillis();
         //    	System.out.println("Java timer" + s + (stopTime - startTime) + " msecs");

         stop();
         out.println(s + " " + getTimeString());
     }
 }

