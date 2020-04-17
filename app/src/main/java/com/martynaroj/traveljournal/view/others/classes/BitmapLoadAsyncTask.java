package com.martynaroj.traveljournal.view.others.classes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.URL;

public class BitmapLoadAsyncTask extends AsyncTask<Void, Void, Bitmap> {

    private String url;


    public BitmapLoadAsyncTask(String url) {
        this.url = url;
    }


    @Override
    protected Bitmap doInBackground(Void... params) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(new URL(url).openStream());
        } catch (IOException ignored) {
        }
        return bitmap;
    }


    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
    }

}