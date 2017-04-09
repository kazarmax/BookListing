package com.example.android.booklisting;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.List;

public class BookAdapter extends ArrayAdapter<Book> {

    public BookAdapter(Activity context, List<Book> books) {
        super(context, 0, books);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.book_list_item, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.bookImage = (ImageView) convertView.findViewById(R.id.book_image);
            viewHolder.bookTitle = (TextView) convertView.findViewById(R.id.book_title);
            viewHolder.bookAuthor = (TextView) convertView.findViewById(R.id.book_author);
            viewHolder.bookPublishDate = (TextView) convertView.findViewById(R.id.book_publish_date);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Book currentBook = getItem(position);

        new DownloadImageTask(viewHolder.bookImage).execute(currentBook.getThumbnailUrl());

        viewHolder.bookTitle.setText(currentBook.getTitle());

        viewHolder.bookAuthor.setText(currentBook.getAuthor());

        viewHolder.bookPublishDate.setVisibility(View.VISIBLE);
        if (currentBook.getPublishedDate() == null) {
            viewHolder.bookPublishDate.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.bookPublishDate.setText(currentBook.getPublishedDate());
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageView bookImage;
        TextView bookTitle;
        TextView bookAuthor;
        TextView bookPublishDate;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        private DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            Bitmap image = null;
            try {
                InputStream in = new java.net.URL(imageUrl).openStream();
                image = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return image;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}