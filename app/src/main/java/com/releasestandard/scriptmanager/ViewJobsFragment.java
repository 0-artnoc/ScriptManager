package com.releasestandard.scriptmanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

public class ViewJobsFragment extends Fragment {

    // list of Jobs
    public List<JobFragment> fragments = new ArrayList<JobFragment>();
    public List<SavedState> fragments_ss = new ArrayList<SavedState>();

    public StorageManager ptr_sm = null;

    public ViewJobsFragment() {
        // Required empty public constructor
    }
    public ViewJobsFragment(StorageManager ptr_sm) {
        this.ptr_sm = ptr_sm;
    }

    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if ( savedInstanceState == null ) {
           Logger.debug( "savedInstanceState is null");
        }
        else{
            Logger.debug("savedInstanceState is not null");
        }
        super.onCreate(savedInstanceState);
    }
    public ArrayList<JobFragment>getSelecteds() {
        ArrayList<JobFragment> list = new ArrayList<>();
        for(JobFragment js : fragments) {
            if  (js.isSelected) {
                list.add(js);
            }
        }
        return list;
    }
    public JobFragment getSelected() {
        for(JobFragment js : fragments) {
            if(js.isSelected) {
                return js;
            }
        }
        return null;
    }
    // here we need registerJobs
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        Integer i = new Integer(fragments.size());
        Logger.debug("size in fragments : "+i);
        if ( savedInstanceState == null ) {
            Logger.debug("ViewJobsFragment:saveInstanceNull");
        }
        View v =  inflater.inflate(R.layout.view_jobs_fragment, container, false);
        Logger.debug("ViewJobsFragment:onCreateView");
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.debug("ViewJobsFragment:onViewCreated");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.debug("ViewJobsFragment:onSaveInstanceState");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Integer in = new Integer(getFragmentManager().getFragments().size());
        Logger.debug("ViewJobsFragment:nb of frags "+in);
        Logger.debug("ViewJobsFragment:onViewStateRestored");
        restoreFragments();
    }

    public void restoreFragments() {
        // we need to restore the view for all childs
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        for(JobFragment jf : fragments) {
            ft.add(R.id.view_jobs_linearlayout, jf);
        }
        ft.commit();
    }

    // Tools
    public void stopAllFragments() {
        stopAllFragments(false);
    }

    public void stopAllFragments(boolean onlySelected) {
        for (JobFragment jf : fragments) {
            if( onlySelected)  {
                if(jf.isSelected) {
                    jf.stopJob();
                }
            }
            else
            {
                jf.stopJob();
            }
        }
    }
/*
    public void backup() {
        for ( JobFragment jf : fragments) {
            jf.backup();
        }
    }

    public void restore() {
        // jf.restore();
    }
*/
    public void unselectAllFragments() {
        for (JobFragment jf : fragments) {
            jf.unselectView();
        }
        if ( getNumberSelected() == 0) {
            MainActivity main = (MainActivity) getActivity();
            main.ow_menu.leaveSelectMode();
        }
    }

    public int getNumberSelected() {
        int count = 0;
        for (JobFragment jf : fragments) {
            if ( jf.isSelected ) {
                count += 1;
            }
        }
        return count;
    }

    public int getNumberStartedAndSelected() {
        int count = 0;
        for (JobFragment jf : fragments) {
            if ( jf.isStarted() && jf.isSelected) {
                count += 1;
            }
        }
        return count;
    }

    public int getNumberStarted() {
        int count = 0;
        for (JobFragment jf : fragments) {
            if ( jf.isStarted()) {
                count += 1;
            }
        }
        return count;
    }


    public JobFragment addNewJob(){
        return addNewJob(null);
    }
    public JobFragment addNewJob(String statefile) {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        JobFragment f = null;
        if ( statefile == null ) {
            f = new JobFragment(ptr_sm);
        }
        else {
            // don't create the files please
            f = new JobFragment(ptr_sm, statefile);
        }
        ft.add(R.id.view_jobs_linearlayout, f);
        ft.commit();

        fragments.add(f);
        return f;
    }

    public void writeState() {
        for ( JobFragment jf : fragments) {
            jf.writeState();
        }
    }

    public void readState() {
        for(String internal_relative_name : this.ptr_sm.getScriptsFromFilesystem()) {
            String statefile = this.ptr_sm.getStateFileAbsolutePath(internal_relative_name);
            JobFragment jf = addNewJob(statefile);
            jf.readState(getActivity(),internal_relative_name);
            jf.dump();
        }
        if ( fragments.size() > 0) {
            JobFragment.fragmentCount = fragments.get(fragments.size() - 1).jd.id + 1;
        }
    }

}