package io.darkblock.darkblock;

import android.graphics.drawable.Drawable;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.ViewGroup;

import io.darkblock.darkblock.app.App;
import io.darkblock.darkblock.app.tools.ArtHelper;
import io.darkblock.darkblock.app.Artwork;
import io.darkblock.darkblock.app.tools.ThumbnailLoader;
import io.darkblock.darkblock.fragment.MainFragment;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {
    private static final String TAG = "CardPresenter";

    private static final int CARD_WIDTH = 313;
    private static final int CARD_HEIGHT = 176;
    private static int sSelectedBackgroundColor;
    private static int sDefaultBackgroundColor;
    private Drawable mDefaultCardImage;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Log.d(TAG, "onCreateViewHolder");

        sDefaultBackgroundColor =
                ContextCompat.getColor(parent.getContext(), R.color.secondary_blue);
        sSelectedBackgroundColor =
                ContextCompat.getColor(parent.getContext(), R.color.primary_blue);
        /*
         * This template uses a default image in res/drawable, but the general case for Android TV
         * will require your resources in xhdpi. For more information, see
         * https://developer.android.com/training/tv/start/layouts.html#density-resources
         */
        mDefaultCardImage = ContextCompat.getDrawable(parent.getContext(), R.drawable.default_background);

        ImageCardView cardView =
                new ImageCardView(parent.getContext()) {
                    @Override
                    public void setSelected(boolean selected) {
                        updateCardBackgroundColor(this, selected);
                        super.setSelected(selected);
                    }
                };

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        updateCardBackgroundColor(cardView, false);
        return new ViewHolder(cardView);
    }

    private static void updateCardBackgroundColor(ImageCardView view, boolean selected) {
        int color = selected ? sSelectedBackgroundColor : sDefaultBackgroundColor;
        // Both background colors should be set because the view"s background is temporarily visible
        // during animations.
        view.setBackgroundColor(color);
        view.findViewById(R.id.info_field).setBackgroundColor(color);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        bindView(cardView,item);
    }

    public void bindView(ImageCardView cardView, Object item) {
        cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);

        // In case of a movie...
        if (item instanceof Artwork) {
            Artwork art = (Artwork) item;

            cardView.setTitleText(art.getTitle());
            cardView.setContentText(App.getAppResources().getString(R.string.author_detail,art.getAuthor()));
            // Load image with glide
            ThumbnailLoader.glideLoad(art.getThumbnailImagUrl(),cardView.getMainImageView());
        }
        Log.d(TAG, "onBindViewHolder");
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        Log.d(TAG, "onUnbindViewHolder");
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        // Remove references to images so that the garbage collector can free up memory
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }
}