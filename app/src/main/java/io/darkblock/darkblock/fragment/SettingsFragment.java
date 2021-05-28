package io.darkblock.darkblock.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.darkblock.darkblock.R;
import io.darkblock.darkblock.activity.MainActivity;
import io.darkblock.darkblock.app.App;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set up button handlers
        View.OnClickListener changeOrientationListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int orientation = 0;
                switch (v.getId()) {
                    // TODO make this an if-else branch
                    case R.id.buttonOrientationLandscape:
                        orientation = 0;
                        break;

                    case R.id.buttonOrientationLandscapeFlipped:
                        orientation = 2;
                        break;

                    case R.id.buttonOrientationPortrait:
                        orientation = 1;
                        break;

                    case R.id.buttonOrientationPortraitFlipped:
                        orientation=3;
                        break;
                }

                App.getSession().setScreenOrientation(orientation);
                App.orientActivity(getActivity());
            }
        };
        getView().findViewById(R.id.buttonOrientationLandscape).setOnClickListener(changeOrientationListener);
        getView().findViewById(R.id.buttonOrientationLandscapeFlipped).setOnClickListener(changeOrientationListener);
        getView().findViewById(R.id.buttonOrientationPortrait).setOnClickListener(changeOrientationListener);
        getView().findViewById(R.id.buttonOrientationPortraitFlipped).setOnClickListener(changeOrientationListener);

        getView().findViewById(R.id.button_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Sign out
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity)getActivity()).closeSession();
                }

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
}