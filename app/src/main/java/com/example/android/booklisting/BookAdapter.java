package com.example.android.booklisting;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

class BookAdapter extends ArrayAdapter<Book> {

    private static final int NO_BOOK_IMAGE_RES_ID = R.drawable.no_book_pic1;

    BookAdapter(Activity context, List<Book> books) {
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

        if (currentBook.getBookImageBitmap() == null) {
            viewHolder.bookImage.setImageResource(NO_BOOK_IMAGE_RES_ID);
        } else {
            viewHolder.bookImage.setImageBitmap(currentBook.getBookImageBitmap());
        }

        viewHolder.bookTitle.setText(currentBook.getTitle());

        if (currentBook.getAuthor() == null) {
            viewHolder.bookAuthor.setText(R.string.no_book_author);
        } else {
            viewHolder.bookAuthor.setText(currentBook.getAuthor());
        }

        viewHolder.bookPublishDate.setText(currentBook.getPublishedDate());

        return convertView;
    }

    private static class ViewHolder {
        ImageView bookImage;
        TextView bookTitle;
        TextView bookAuthor;
        TextView bookPublishDate;
    }

}