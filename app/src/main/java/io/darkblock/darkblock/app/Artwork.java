package io.darkblock.darkblock.app;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.darkblock.darkblock.R;
import io.darkblock.darkblock.app.App;

public class Artwork implements Serializable {
    static final long serialVersionUID = 727566175075960656L;

    public Artwork() {
        title = App.getAppResources().getString(R.string.unknown_title);
        author = App.getAppResources().getString(R.string.unknown_author);
        creationDate = LocalDate.ofEpochDay(0);
    }

    public String toString() {
        return getTitle() + " - " + getAuthor();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getThumbnailImagUrl() {
        return thumbnailImagUrl;
    }

    public void setThumbnailImagUrl(String thumbnailImagUrl) {
        this.thumbnailImagUrl = thumbnailImagUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artwork artwork = (Artwork) o;
        return Objects.equals(artId, artwork.artId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artId);
    }

    private String title;
    private String author;
    private String description;
    private String thumbnailImagUrl;
    private LocalDate creationDate;
    private int num;
    private String darkblockId;

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    private String creator;

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    private boolean isEncrypted;

    public Drawable getThumbnailGraphic() {
        return thumbnailGraphic;
    }

    public void setThumbnailGraphic(Drawable thumbnailGraphic) {
        this.thumbnailGraphic = thumbnailGraphic;
    }

    private Drawable thumbnailGraphic;
    private UUID artId;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    private String transactionId;

    public UUID getId() {
        return artId;
    }

    public void setDarkblockId(String darkblockId) {
        this.darkblockId = darkblockId;
    }

    public String getDarkblockId(){ return darkblockId; }

    public void setArtId(UUID artId) {
        this.artId = artId;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int id) {
        this.num = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }
}
