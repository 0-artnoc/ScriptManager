package com.example.scriptmanager;

import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
//import android.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
//import android.app.FragmentTransaction;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.Hashtable;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar = null;

    public boolean isInSelectMode = false;

     // Objects that encapsulate the data //
     public ViewJobsFragment jobs_view = null;
     public SettingsFragment sf = null;
     public OverflowMenu ow_menu = null;
     private Hashtable <Integer, Fragment> views = null;

     public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get Shell locations path
        Shell s = new Shell(
                getApplicationContext().getFilesDir().getAbsolutePath(),
                getApplicationContext().getExternalFilesDir(null).getAbsolutePath());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)  {
                handlerFabClick();
            }
        });

        this.views = new Hashtable<>();
        jobs_view = new ViewJobsFragment();
        sf = new SettingsFragment();
        views.put(R.id.nav_host_fragment, jobs_view);
        views.put(R.id.action_settings, sf);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.nav_host_fragment, jobs_view);
        ft.commit();
        Log.v("scriptmanager","MainActivity:onCreate");
    }
    @Override
    protected  void onStart() {
        super.onStart();
        Log.v("scriptmanager","MainActivity:onStart");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        ow_menu = new OverflowMenu(this, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_stopselected) {
            jobs_view.stopAllFragments(true);
            jobs_view.unselectAllFragments();
        }
        if (id == R.id.action_stopall) {
            jobs_view.unselectAllFragments();
            jobs_view.stopAllFragments();
        }
        if (id == R.id.action_unselectall) {
            jobs_view.unselectAllFragments();
        }
        if (id == R.id.action_settings) {

            jobs_view.unselectAllFragments();
            ow_menu.leaveSelectMode();

            ActionBar ab = super.getSupportActionBar();
            ab.setTitle(R.string.settings_page_title);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayUseLogoEnabled(false);

            findViewById(R.id.fab).setVisibility(View.INVISIBLE);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            Fragment fg = views.get(R.id.action_settings);
            ft.replace(R.id.nav_host_fragment,fg);
            ft.commit();

            ow_menu.setMenuVisibility(false);

            return true;
        }

        if ( id == android.R.id.home) {
            ActionBar ab = super.getSupportActionBar();
            ab.setTitle(R.string.app_name);
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setDisplayUseLogoEnabled(true);

            if( isInSelectMode ) {
                jobs_view.unselectAllFragments();
                ow_menu.leaveSelectMode();
            }
            else {
                findViewById(R.id.fab).setVisibility(View.VISIBLE);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.nav_host_fragment,jobs_view);
                ft.commit();
                ow_menu.setMenuVisibility(true);
            }
            return true;
        }

        if ( id == R.id.action_oneonly_edit) {
            jobs_view.unselectAllFragments();

            Context context = getApplicationContext();
            String pvd = context.getApplicationContext().getPackageName() + ".provider";
            Log.v("scriptmanager",pvd);
            File f = new File(Shell.externalStorage+"/test.sh");
            Uri uri = FileProvider.getUriForFile(context, pvd, f);

            Intent myIntent = new Intent(Intent.ACTION_VIEW);
            myIntent.setData(uri);
            myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(myIntent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // => tout ce qui est en rapport avec le menu


    /*
     * get color associated with the current theme.
     */
    public static int getColorFromId(MainActivity main, int colorID) {
        TypedValue typedValue = new TypedValue();
        main.getTheme().resolveAttribute(colorID, typedValue, true);
        return typedValue.data;
    }

    // Handlers for click on interface
    public void handlerFabClick() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // ex : setting remove animations
        // not very visible lol
        ft.setCustomAnimations(FragmentTransaction.TRANSIT_NONE,
                FragmentTransaction.TRANSIT_NONE,
                FragmentTransaction.TRANSIT_NONE,
                FragmentTransaction.TRANSIT_NONE);

        JobFragment f = JobFragment.newInstance("my text");
        ft.add(R.id.linear_layout_actions_list, f);
        ft.commit();

        jobs_view.fragments.add(f);
    }
}