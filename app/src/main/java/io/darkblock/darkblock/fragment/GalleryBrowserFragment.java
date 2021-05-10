package io.darkblock.darkblock.fragment;

import android.os.Bundle;

import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.darkblock.darkblock.R;
import io.darkblock.darkblock.app.layout.GalleryAdapter;
import io.darkblock.darkblock.app.layout.SpacesItemDecoration;

public class GalleryBrowserFragment extends Fragment {

    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery_browser, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RecyclerView recyclerView = getView().findViewById(R.id.recycler_gallery);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        recyclerView.addItemDecoration(new SpacesItemDecoration(16));

        //recyclerView.setAdapter(new GalleryAdapter(getView()));
        recyclerView.requestFocus();

        // Start retrieving art
        //new MainFragment.RetrieveArtTask().execute();

        // Setup art poller
        /*walletUpdateChecker.postDelayed(new Runnable() {
            @Override
            public void run() {
                new MainFragment.CheckWalletUpdateTask().execute();
                // Re-run
                walletUpdateChecker.postDelayed(this,WALLET_POLL_DELAY);
            }
        },WALLET_POLL_DELAY);*/

        // Setup UI stuff
        //setupUIElements();
        //setOnItemViewClickedListener(new MainFragment.ItemViewClickedListener());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //walletUpdateChecker.removeCallbacksAndMessages(null);
    }

}
