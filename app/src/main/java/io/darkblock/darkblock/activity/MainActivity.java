package io.darkblock.darkblock.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

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

    GalleryBrowserFragment fragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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