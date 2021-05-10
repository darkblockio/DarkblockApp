package io.darkblock.darkblock.app.layout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.leanback.widget.ImageCardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.darkblock.darkblock.R;
import io.darkblock.darkblock.activity.ArtViewActivity;
import io.darkblock.darkblock.app.App;
import io.darkblock.darkblock.app.Artwork;
import io.darkblock.darkblock.app.tools.ThumbnailLoader;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private final List<Artwork> artworkList;

    public GalleryAdapter(List<Artwork> artworkList) {
        this.artworkList = artworkList;
    }

    @NonNull
    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final ImageCardView cardView = new ImageCardView(new ContextThemeWrapper(parent.getContext(), R.style.GalleryCardStyle));

        cardView.setMainImageAdjustViewBounds(true);
        cardView.setMainImageScaleType(ImageView.ScaleType.FIT_CENTER);

        cardView.setCardType(ImageCardView.CARD_TYPE_FLAG_TITLE | ImageCardView.CARD_TYPE_FLAG_CONTENT);

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        cardView.setBackgroundColor(App.getAppResources().getColor(R.color.accent_green));

        return new ViewHolder(cardView, (RecyclerView) parent);
    }

    @Override
    public void onBindViewHolder(@NonNull final GalleryAdapter.ViewHolder holder, int position) {
        // Set image
        holder.setArtwork(artworkList.get(position));

        // Set default scale
        holder.getView().setScaleX(0.75f);
        holder.getView().setScaleY(0.75f);

        // Set on focus listener
        holder.getView().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // Scale the view
                float scale = hasFocus ? 1f : 0.75f;
                v.animate()
                        .scaleX(scale)
                        .scaleY(scale)
                        .setDuration(250);

                // (screen width - width)/2
                int x = (1920 - v.getWidth())/2;
                holder.recyclerView.smoothScrollBy((int) (v.getX()-x),0);
                // what the FUCK how did that work
            }
        });

        holder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Display fullscreen
                Intent intent = new Intent(holder.getView().getContext(), ArtViewActivity.class);
                intent.putExtra("ArtId", holder.getArtwork().getNum());
                holder.getView().getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artworkList.size();
    }

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageCardView card;

        private final RecyclerView recyclerView;

        // Associated artwork
        private Artwork artwork;

        public ViewHolder(@NonNull View itemView, RecyclerView recyclerView) {
            super(itemView);
            card = (ImageCardView) itemView;
            card.setFocusable(true);
            this.recyclerView = recyclerView;
        }

        public void setArtwork(Artwork artwork) {

            System.out.println(card.getCardType());

            this.artwork = artwork;
            ThumbnailLoader.glideLoadResize(artwork.getThumbnailImagUrl(),card.getMainImageView());
            card.setTitleText(artwork.getTitle());
            card.setContentText(artwork.getAuthor());
        }
        public Artwork getArtwork() {
            return artwork;
        }
        public View getView() {return card;}
    }

}