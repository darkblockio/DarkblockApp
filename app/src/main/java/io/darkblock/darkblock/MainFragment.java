package io.darkblock.darkblock;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;
import androidx.leanback.app.BrowseFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.darkblock.darkblock.activity.ArtGridActivity;
import io.darkblock.darkblock.activity.ArtViewActivity;
import io.darkblock.darkblock.activity.WelcomeActivity;
import io.darkblock.darkblock.app.App;
import io.darkblock.darkblock.app.tools.ArtHelper;
import io.darkblock.darkblock.app.Artwork;
import io.darkblock.darkblock.app.tools.ArtKeyGenerator;
import io.darkblock.darkblock.app.tools.TransferManager;

public class MainFragment extends BrowseFragment {
    private static final String TAG = "MainFragment";

    private static final int GRID_ITEM_WIDTH = 200;
    private static final int GRID_ITEM_HEIGHT = 200;
    public static final int MAX_ITEMS = 6;

    private static Drawable iconRefresh;
    private static Drawable iconSignout;

    private ArtKeyGenerator artKeyGenerator;

    // Poll every so many seconds for updates to the wallet
    private static final int WALLET_POLL_DELAY = 30 * 1000;
    private final Handler walletUpdateChecker = new Handler();

    // Used to ensure we don't try and fetch wallet contents twice at ocne
    protected boolean fetching;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        iconRefresh = getContext().getDrawable(R.drawable.icon_refresh);
        iconSignout = getContext().getDrawable(R.drawable.icon_logout);

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

        // Setup UI stuff
        setupUIElements();
        setOnItemViewClickedListener(new ItemViewClickedListener());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        walletUpdateChecker.removeCallbacksAndMessages(null);
    }

    private void loadRows() {
        List<Artwork> list = ArtHelper.getArtworkList();
        if (list == null) return;

        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        CardPresenter cardPresenter = new CardPresenter();

        // Add collection
        ArrayObjectAdapter collectionRowAdapter = new ArrayObjectAdapter(cardPresenter);
        for (int j = 0; j < Math.min(list.size(),MAX_ITEMS); j++) {
            collectionRowAdapter.add(list.get(j % list.size()));
        }
        if (ArtHelper.getArtListSize() > MAX_ITEMS) {
            collectionRowAdapter.add(getString(R.string.view_all));
        }

        HeaderItem header = new HeaderItem(0, getString(R.string.gallery));
        rowsAdapter.add(new ListRow(header, collectionRowAdapter));


        // Add settings
        HeaderItem gridHeader = new HeaderItem(1, getString(R.string.settings));
        GridItemPresenter mGridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
        // Add individual settings
        //gridRowAdapter.add(getString(R.string.preferences));
        gridRowAdapter.add(iconRefresh);
        //gridRowAdapter.add(getString(R.string.about));
        gridRowAdapter.add(iconSignout);

        rowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

        // Set row adapter
        setAdapter(rowsAdapter);
    }

    private void setupUIElements() {
        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedene
        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // Setup background
        getView().setBackground(getContext().getDrawable(R.color.primary_dark));

        // set fastLane (or headers) background color
        setBrandColor(ContextCompat.getColor(getActivity(), R.color.primary_blue));
    }

    public final class ItemViewClickedListener implements OnItemViewClickedListener {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof String) {
                if (item.equals(getString(R.string.view_all))) {
                    // Show full grid
                    Intent intent = new Intent(getActivity(), ArtGridActivity.class);
                    getActivity().startActivity(intent);
                }

            }else if (item instanceof Artwork) {

                // Display fullscreen
                Intent intent = new Intent(getActivity(), ArtViewActivity.class);
                intent.putExtra("ArtId",((Artwork) item).getNum());
                getActivity().startActivity(intent);

            }else if (item instanceof Drawable) {

                if (item.equals(iconSignout)) {
                    // Sign out
                    App.destroySession();
                    artKeyGenerator.interrupt();
                    artKeyGenerator = null;

                    Intent i = new Intent(getContext(), WelcomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    getActivity().finish();
                    startActivity(i);
                }else  if (item.equals(iconRefresh)) {
                    // Refresh rows
                    artKeyGenerator.interrupt();
                    new RetrieveArtTask().execute();
                }

            }
        }
    }

    private class GridItemPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            ImageView view = new ImageView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(
                    ContextCompat.getColor(getActivity(), R.color.secondary_blue));
            //view.setTextColor(Color.WHITE);
            //view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ((ImageView) viewHolder.view).setImageDrawable((Drawable) item);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
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
                loadRows();
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
                loadRows();
            }
        }
    }

}