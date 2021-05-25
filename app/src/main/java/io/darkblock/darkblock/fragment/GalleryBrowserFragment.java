package io.darkblock.darkblock.fragment;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.darkblock.darkblock.R;
import io.darkblock.darkblock.app.Artwork;
import io.darkblock.darkblock.app.layout.GalleryAdapter;
import io.darkblock.darkblock.app.layout.SpacesItemDecoration;
import io.darkblock.darkblock.app.tools.ArtHelper;

public class GalleryBrowserFragment extends Fragment {

    private static GalleryBrowserFragment instance;

    public static GalleryBrowserFragment getInstance() {
        return instance;
    }

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

        instance = this;

        loadGallery();

        //recyclerView.setAdapter(new GalleryAdapter(getView()));
        //recyclerView.requestFocus();
    }

    @Override
    public void onDestroyView() {
        instance = null;
        super.onDestroyView();
    }

    public void loadGallery() {
        List<Artwork> list = ArtHelper.getArtworkList();
        if (list == null) return;
        //System.out.println("hello mario");
        View v = getView();
        RecyclerView recyclerView = v.findViewById(R.id.recycler_gallery);
        recyclerView.setAdapter(new GalleryAdapter(list));
        recyclerView.requestFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //walletUpdateChecker.removeCallbacksAndMessages(null);
    }

}
