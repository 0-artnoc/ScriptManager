package com.releasestandard.scriptmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/*
 * This class is a wrapper for an underlying shell : e.g. sh
 */

public class Shell {

    private static List<Process> processes = new ArrayList<Process>();
    private static List<PendingIntent> intents = new ArrayList<PendingIntent>();


    public StorageManager sm = null;
    public BashInterface bi = null;

    public void dump() { Logger.debug(dump("")); }
    public String dump(String offset) {
        return "" +
                offset + "Shell { \n" +
                offset + "\tprocesses=" + processes.size() + "\n" +
                offset + "\tintents=" + intents.size() + "\n" +
                bi.dump(offset + "\t" ) +
                sm.dump(offset + "\t") +
                offset + "}\n";
    }

    /**
     *  Constructor will build the storage required to store scripts.
     */
    public Shell(StorageManager sm) {
            this.sm = new StorageManager(sm);
            this.bi = new BashInterface(sm);
            EventsReceiver.listeners.add(this);
    }

    /**
     *  Run one time
     * @param cmd
     * @return
     */
    public int execCmd(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh","-c",cmd});
            processes.add(p);
        } catch (IOException e) {
            return 1;
        }
        return 0;
    }

    /**
     * parameter could be the name of the script or an absolute path.
     * @param scriptname
     * @return
     */
    public int execScript(String scriptname) {
        sm.setScriptName(scriptname);
        String script_path = sm.getScriptAbsolutePath();
        String output = sm.getOutputAbsolutePath();
        output = bi.wrappScript(script_path,output);

        Logger.log("Job execution : " + output + "\n   log=" + sm.getLogAbsolutePath());
        Process p = _execScript(output,sm.getLogAbsolutePath());
        if ( p != null ) {
            Logger.debug("Shell: Process has started");
            processes.add(p);
            return 0;
        }
        Logger.debug("Shell: Process failed to start");
        return 1;
    }
    public static Process _execScript(String script) {
        return _execScript(script,null);
    }
    public static Process _execScript(String script, String log) { return _execCmd(". "+script,log); }
    public static Process _execCmd(String cmd) {
        return _execCmd(cmd,null);
    }
    public static Process _execCmd(String cmd, String log) {
        try {
            ProcessBuilder builder = null;
            if ( log != null) {
                builder = new ProcessBuilder("sh", "-c", "&>> " + log + "  " + cmd);
            } else {
                builder = new ProcessBuilder("sh", "-c",cmd);
            }
            Process p = builder.start();
            return p;
        } catch (IOException e) {
            return null;
        }
    }
    public void clearLog() throws IOException { clearLog(this.sm.script_name); }
    public void clearLog(String logpath) throws IOException {
        Shell._execCmd("> "+logpath);
    }

    public void scheduleJob(Context context, String scriptname, int sched[]) {
        intents.add(_scheduleJob(context,scriptname, sched));
    }

    public static PendingIntent _scheduleJob(Context context, String scriptname, int sched[])  {
        // need to get the time here
        Calendar next = TimeManager.nextSched(sched);

        Logger.log("[" + (new Integer(AlarmReceiver.REQUEST_CODE + 1)) + "] Job " + scriptname + " scheduled for " + next.getTime().toString());
        long t = next.getTimeInMillis();

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("script",scriptname);
        intent.putExtra("sched",sched);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, AlarmReceiver.REQUEST_CODE++, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT < 23) {
            if (Build.VERSION.SDK_INT >= 19) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, t, alarmIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, t, alarmIntent);
            }
        } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, alarmIntent);
            }
        return alarmIntent;
    }

    public void terminateAll() {
        for(Process p : processes) {
            p.destroy();
        }
        processes.clear();
        for(PendingIntent p : intents) {
            p.cancel();
        }
        intents.clear();
    }
}
