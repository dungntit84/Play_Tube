package com.hteck.playtube.common;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.data.PlaylistInfo;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.service.IYoutubeAction;
import com.hteck.playtube.service.PlaylistService;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.UUID;

public class ViewHelper {
    public static View getYoutubeView(View convertView, YoutubeInfo youtubeInfo,
                                      Constants.YoutubeListType videoListType, View.OnClickListener onClickListener) {
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();

        View v = getConvertView(convertView, R.layout.item_youtube_view);

        try {
            TextView textViewTitle = v.findViewById(R.id.item_youtube_tv_title);
            textViewTitle.setText(youtubeInfo.title.toUpperCase());

            ImageView iv = (ImageView) v.findViewById(R.id.item_youtube_img_thumb);
            displayYoutubeThumb(iv, youtubeInfo.imageUrl);

            TextView textViewUploadedDate = (TextView) v.findViewById(R.id.item_youtube_tv_uploaded_date);
            textViewUploadedDate.setText(youtubeInfo.uploadedDate);
            TextView textViewPlaysNo = (TextView) v.findViewById(R.id.item_youtube_tv_plays_no);
            textViewPlaysNo.setText(Utils.getDisplayViews(youtubeInfo.viewsNo, false));

            TextView textViewLikesNo = (TextView) v.findViewById(R.id.item_youtube_tv_likes_no);
            textViewLikesNo.setText(Utils.getDisplayLikes(youtubeInfo.likesNo, false));
            TextView textViewTime = (TextView) v.findViewById(R.id.item_youtube_tv_time);
            textViewTime.setText(Utils.getDisplayTime((int) youtubeInfo.duration));
            TextView textViewUploader = (TextView) v.findViewById(R.id.item_youtube_tv_uploader);
            textViewUploader.setText(youtubeInfo.uploaderName);

            ImageView imageViewAction = (ImageView) v
                    .findViewById(R.id.item_youtube_img_action);

            imageViewAction.setTag(youtubeInfo);
            imageViewAction.setOnClickListener(onClickListener);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return v;
    }

    public static View getGridYoutubeView(View convertView, YoutubeInfo youtubeInfo,
                                          Constants.YoutubeListType videoListType, View.OnClickListener onClickListener) {
        View v = getConvertView(convertView, R.layout.grid_item_youtube_view);

        try {
            TextView textViewTitle = v.findViewById(R.id.item_youtube_tv_title);
            textViewTitle.setText(youtubeInfo.title.toUpperCase());

            ImageView iv = (ImageView) v.findViewById(R.id.item_youtube_img_thumb);
            displayYoutubeThumb(iv, youtubeInfo.imageUrl);
            if (iv.getWidth() == 0) {
                int width = Utils.getYoutubeWidth();
                int height = width * 18 / 32;
                iv.setLayoutParams(new FrameLayout.LayoutParams(width, height));
            }
            TextView textViewUploadedDate = (TextView) v.findViewById(R.id.item_youtube_tv_uploaded_date);
            textViewUploadedDate.setText(youtubeInfo.uploadedDate);
            TextView textViewPlaysNo = (TextView) v.findViewById(R.id.item_youtube_tv_plays_no);
            textViewPlaysNo.setText(Utils.getDisplayViews(youtubeInfo.viewsNo, true));

            TextView textViewTime = (TextView) v.findViewById(R.id.item_youtube_tv_time);
            textViewTime.setText(Utils.getDisplayTime((int) youtubeInfo.duration));
            TextView textViewUploader = (TextView) v.findViewById(R.id.item_youtube_tv_uploader);
            textViewUploader.setText(youtubeInfo.uploaderName);

            ImageView imageViewAction = (ImageView) v
                    .findViewById(R.id.item_youtube_img_action);

            imageViewAction.setTag(youtubeInfo);
            imageViewAction.setOnClickListener(onClickListener);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return v;
    }

    public static PopupMenu showYoutubeMenu(Constants.YoutubeListType youtubeListType,
                                            final IYoutubeAction eventListener, boolean isOnPlayer, final View vDock) {
        PopupMenu popup = new PopupMenu(MainActivity.getInstance(), vDock);

        popup.getMenuInflater().inflate(R.menu.item_video, popup.getMenu());
        YoutubeInfo videoInfo = (YoutubeInfo) vDock.getTag();
        if (youtubeListType == Constants.YoutubeListType.Normal) {
            popup.getMenu().removeItem(R.id.menu_item_remove);
        }

        popup.show();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                YoutubeInfo videoInfo = (YoutubeInfo) vDock.getTag();
//                if (item.getItemId() == R.id.menu_item_add_to_playlist) {
//                    addVideoToPlaylist(videoInfo, PlaylistType.Online);
//                } else if (item.getItemId() == R.id.menu_item_download_to_playlist) {
//                    addVideoToPlaylist(videoInfo, PlaylistType.Offline);
//                } else if (item.getItemId() == R.id.menu_item_add_to_account_playlist) {
//                    selectedVideoInfo = videoInfo;
//                    addToMyPlaylist(true);
//                } else if (item.getItemId() == R.id.menu_item_add_to_favourites) {
//                    MainActivity.getInstance().addVideoToFavourite(videoInfo);
//                } else if (item.getItemId() == R.id.menu_item_remove) {
//                    if (eventListener != null) {
//                        eventListener.onEvent(videoInfo, Action.REMOVE);
//                    }
//                } else if (item.getItemId() == R.id.menu_item_download_ringtone) {
//                    Utils.initRingtoneEvent(videoInfo);
//                } else if (item.getItemId() == R.id.menu_item_share) {
                Utils.shareVideo(videoInfo);
//                }
                return false;
            }
        });
        return popup;
    }

    public static View getConvertView(View convertView, int resId) {
        LayoutInflater inflater = MainActivity.getInstance().getLayoutInflater();
        View v;
        if (convertView == null) {
            v = inflater.inflate(resId, null);
            v.setTag(Constants.CUSTOM_TAG, resId);
        } else {
            v = convertView;
            if (v.getTag(Constants.CUSTOM_TAG) == null || !(v.getTag(Constants.CUSTOM_TAG) instanceof Integer) || (int) v.getTag(Constants.CUSTOM_TAG) != resId) {
                v = inflater.inflate(resId, null);
                v.setTag(Constants.CUSTOM_TAG, resId);
            }
        }
        return v;
    }

    public static void displayYoutubeThumb(ImageView imageView, String url) {
        if (imageView.getTag() == null
                || !imageView.getTag().toString()
                .equalsIgnoreCase(url)) {
            imageView.setTag(url);
            imageView.setImageResource(R.drawable.ic_thumb);
            ImageLoader.getInstance().displayImage(
                    url, imageView);
        }
    }

    public static void showAddPlaylist() {
        final View v = MainActivity.getInstance().getLayoutInflater().inflate(R.layout.edit_playlist, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.getInstance());
        builder.setPositiveButton(android.R.string.yes, null);
        builder.setNegativeButton(android.R.string.no, null);
        builder.setTitle(Utils.getString(R.string.add_new_playlist));
        builder.setView(v);
        final AlertDialog mAlertDialog = builder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        try {
                            EditText editText = v.findViewById(R.id.edit_playlist_edit_text);
                            String title = editText.getText().toString().trim();
                            if (Utils.stringIsNullOrEmpty(title)) {
                                return;
                            }
                            PlaylistInfo playlistInfo = new PlaylistInfo();
                            playlistInfo.id = UUID.randomUUID();

                            playlistInfo.title = title;
                            PlaylistService.addPlaylist(playlistInfo);
                            Utils.showMessage(Utils.getString(R.string.added));
                            dialog.dismiss();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        });
        mAlertDialog.show();
    }
}
