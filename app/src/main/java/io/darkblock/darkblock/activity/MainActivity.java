package io.darkblock.darkblock.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import io.darkblock.darkblock.R;
import io.darkblock.darkblock.app.Artwork;
import io.darkblock.darkblock.app.layout.GalleryAdapter;
import io.darkblock.darkblock.app.tools.ArtHelper;
import io.darkblock.darkblock.app.tools.ArtKeyGenerator;
import io.darkblock.darkblock.fragment.GalleryBrowserFragment;

/*
 * Main Activity class that loads {@link MainFragment}.
 */
public class MainActivity extends Activity {

    private ArtKeyGenerator artKeyGenerator;

    // Poll every so many seconds for updates to the wallet
    private static final int WALLET_POLL_DELAY = 30 * 1000;
    private final Handler walletUpdateChecker = new Handler();

    // Used to ensure we don't try and fetch wallet contents twice at ocne
    protected boolean fetching;

    // Navigation
    private TextView lastFocusedView;

    GalleryBrowserFragment fragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNavigation();

        // The main fragment
        fragment = (GalleryBrowserFragment) getFragmentManager().findFragmentById(R.id.main_fragment);

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
        View.OnFocusChangeListener listener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                // Change color
                TextView textView = (TextView) v;
                if (lastFocusedView != null) {
                    lastFocusedView.setTextColor(getResources().getColor(R.color.white));
                }
                textView.setTextColor(getResources().getColor(R.color.accent_green));

                // Scroll
                if (hasFocus) {
                    int x = (1920 - v.getWidth()) / 2;
                    int dx = (int) (v.getX() - x);

                    navigation.animate().translationX(-dx).setDuration(300);

                    lastFocusedView = textView;
                }
            }
        };

        // Create buttons
        for (int i=0;i<3;i++) {
            // Create navigation item
            TextView navItem = new TextView(this);
            navItem.setTextAppearance(R.style.NavigationItem);
            navItem.setLayoutParams(optionParams);
            navItem.setText("Option " + i);

            // Set listeners
            navItem.setFocusable(true);
            navItem.setOnFocusChangeListener(listener);

            // Add to navigation view
            navigation.addView(navItem);
        }

    }


    // Call this to load artwork into the gallery view
    private void loadGallery() {
        List<Artwork> list = ArtHelper.getArtworkList();
        System.out.println(fragment);
        if (list == null || fragment == null) return;

        View v = fragment.getView();
        RecyclerView recyclerView = v.findViewById(R.id.recycler_gallery);
        recyclerView.setAdapter(new GalleryAdapter(list));
        recyclerView.requestFocus();
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