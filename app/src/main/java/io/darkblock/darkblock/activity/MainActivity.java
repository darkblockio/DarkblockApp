package io.darkblock.darkblock.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.darkblock.darkblock.R;
import io.darkblock.darkblock.app.App;
import io.darkblock.darkblock.app.Artwork;
import io.darkblock.darkblock.app.NavTab;
import io.darkblock.darkblock.app.layout.GalleryAdapter;
import io.darkblock.darkblock.app.tools.ArtHelper;
import io.darkblock.darkblock.app.tools.ArtKeyGenerator;
import io.darkblock.darkblock.fragment.GalleryBrowserFragment;
import io.darkblock.darkblock.fragment.SettingsFragment;

/*
 * Main Activity class that loads {@link MainFragment}.
 */
public class MainActivity extends FragmentActivity {

    private static final int NUM_TABS = 2;
    private static final String[] TAB_NAMES = {"Gallery","Settings"};

    private ArtKeyGenerator artKeyGenerator;

    // Poll every so many seconds for updates to the wallet
    private static final int WALLET_POLL_DELAY = 30 * 1000;
    private final Handler walletUpdateChecker = new Handler();

    // Used to ensure we don't try and fetch wallet contents twice at ocne
    protected boolean fetching;

    // Navigation
    private NavTab lastFocusedNavView;
    private NavTab lastSelectedNavView;

    private NavTab defaultSelectedNavTab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNavigation();
        defaultSelectedNavTab.requestFocus();

        // Start retrieving art
        new RetrieveArtTask().execute();

        // Setup art poller
        walletUpdateChecker.postDelayed(new Runnable() {
            @Override
            public void run() {
                new CheckWalletUpdateTask().execute();
                // Re-run
                walletUpdateChecker.postDelayed(this,WALLET_POLL_DELAY);
            }
        },WALLET_POLL_DELAY);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        walletUpdateChecker.removeCallbacksAndMessages(null);
    }


    // Setup our navigation stuff
    private void setupNavigation() {

        final LinearLayout navigation = findViewById(R.id.linear_navigation);

        // Create layout params
        LinearLayout.LayoutParams optionParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        optionParams.setMarginStart(32);
        optionParams.setMarginEnd(32);

        // Create listener
        View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                NavTab navTab = (NavTab) v;

                // Change color
                if (lastFocusedNavView != null) {
                    lastFocusedNavView.setTextColor(getResources().getColor(R.color.white));
                }

                // Scroll
                if (hasFocus) {
                    navTab.setTextColor(getResources().getColor(R.color.accent_green));

                    int x = (App.getDisplayWidth() - v.getWidth()) / 2;
                    int dx = (int) (v.getX() - x);

                    navigation.animate().translationX(-dx).setDuration(300);

                    lastFocusedNavView = navTab;
                }
            }
        };

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavTab navTab = (NavTab) v;

                if (lastSelectedNavView != null) {
                    lastSelectedNavView.setPaintFlags(navTab.getPaintFlags());
                }
                navTab.setPaintFlags(navTab.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                // Navigate to the correct fragment
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();

                transaction.addToBackStack(null);
                transaction.setReorderingAllowed(true);
                transaction.replace(R.id.main_fragment,navTab.makeFragment());

                transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

                transaction.commit();

                lastSelectedNavView = navTab;
            }
        };

        // Create buttons
        for (int i=0;i<NUM_TABS;i++) {
            // Create navigation item
            final int x = i;
            NavTab navItem = new NavTab(this) {
                @Override
                public androidx.fragment.app.Fragment makeFragment() {
                    return x == 0 ? new GalleryBrowserFragment() : new SettingsFragment();
                }
            };
            navItem.setLayoutParams(optionParams);
            navItem.setText(TAB_NAMES[i]);

            // Set listeners
            navItem.setOnFocusChangeListener(focusListener);
            navItem.setOnClickListener(clickListener);

            // Add to navigation view
            navigation.addView(navItem);

            if (i == 0) {
                defaultSelectedNavTab = navItem;
                defaultSelectedNavTab.requestFocus();
                defaultSelectedNavTab.callOnClick();
            }
        }

    }

    public void closeSession() {
        App.destroySession();

        artKeyGenerator.interrupt();
        artKeyGenerator = null;

        Intent i = new Intent(this, WelcomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        finish();
        startActivity(i);
    }

    void loadGallery() {
        if (GalleryBrowserFragment.getInstance() != null) {
            GalleryBrowserFragment.getInstance().loadGallery();
        }
    }

    /**
     * Download artwork from arweave
     */
    private class RetrieveArtTask extends AsyncTask<Void,Void,Integer> {

        private static final String URL = "https://%s.arweave.net/%s";

        @Override
        protected Integer doInBackground(Void... none) {
            if (!fetching) {
                fetching = true;
                return ArtHelper.fetchArtwork();
            }else{
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer numArt) {
            if (numArt != -1) {
                loadGallery();
                artKeyGenerator = new ArtKeyGenerator();
                artKeyGenerator.start();
                fetching = false;
            }
        }
    }


    /**
     * Used to check if the number of works in a wallet has changed
     */
    private class CheckWalletUpdateTask extends AsyncTask<Void,Void,Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            int worksInAccount = ArtHelper.getArtListSize();
            int newArtCount = ArtHelper.fetchArtwork();
            return (worksInAccount != newArtCount) || ArtHelper.needsUpdate();
        }

        @Override
        protected void onPostExecute(Boolean reload) {
            if (reload) {
                loadGallery();
            }
        }
    }


}