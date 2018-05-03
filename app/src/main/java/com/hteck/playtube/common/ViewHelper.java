package com.hteck.playtube.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.data.ChannelInfo;
import com.hteck.playtube.data.PlaylistInfo;
import com.hteck.playtube.data.PlaylistItemInfo;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.databinding.GridItemYoutubeViewBinding;
import com.hteck.playtube.databinding.ItemUserActivityBinding;
import com.hteck.playtube.databinding.ItemYoutubeViewBinding;
import com.hteck.playtube.holder.BaseViewHolder;
import com.hteck.playtube.service.HistoryService;
import com.hteck.playtube.service.PlaylistService;
import com.hteck.playtube.holder.GridItemYoutubeViewHolder;
import com.hteck.playtube.holder.ItemYoutubeViewHolder;
import com.hteck.playtube.view.PlaylistsDialogView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.UUID;

import static com.hteck.playtube.common.Utils.getString;

public class ViewHelper {
    public static View getYoutubeView(View convertView, ViewGroup group, YoutubeInfo youtubeInfo, View.OnClickListener onClickListener) {
        LayoutInflater inflater = MainActivity.getInstance().getLayoutInflater();
        ItemYoutubeViewHolder holder;
        if (convertView == null || convertView.getTag() == null || !(convertView.getTag() instanceof ItemYoutubeViewHolder)) {
            ItemYoutubeViewBinding itemBinding = DataBindingUtil.inflate(inflater, R.layout.item_youtube_view, group, false);
            holder = new ItemYoutubeViewHolder(itemBinding);
            holder.view = itemBinding.getRoot();
            holder.view.setTag(holder);
        } else {
            holder = (ItemYoutubeViewHolder) convertView.getTag();
        }
        if (youtubeInfo == null) {
            return holder.view;
        }
        try {
            holder.binding.itemYoutubeTvTitle.setText(youtubeInfo.title.toUpperCase());
            displayYoutubeThumb(holder.binding.itemYoutubeImgThumb, youtubeInfo.imageUrl);
            holder.binding.itemYoutubeTvUploadedDate.setText(youtubeInfo.uploadedDate);
            holder.binding.itemYoutubeTvPlaysNo.setText(Utils.getDisplayViews(youtubeInfo.viewsNo, false));
            holder.binding.itemYoutubeTvLikesNo.setText(Utils.getDisplayLikes(youtubeInfo.likesNo, false));
            holder.binding.itemYoutubeTvTime.setText(Utils.getDisplayTime((int) youtubeInfo.duration));
            holder.binding.itemYoutubeTvUploader.setText(youtubeInfo.uploaderName);
            holder.binding.itemYoutubeImgAction.setTag(youtubeInfo);
            holder.binding.itemYoutubeImgAction.setOnClickListener(onClickListener);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return holder.view;
    }

    public static View getGridYoutubeView(View convertView, ViewGroup group, YoutubeInfo youtubeInfo, View.OnClickListener onClickListener) {
        LayoutInflater inflater = MainActivity.getInstance().getLayoutInflater();
        GridItemYoutubeViewHolder holder;
        if (convertView == null) {
            GridItemYoutubeViewBinding itemBinding = DataBindingUtil.inflate(inflater, R.layout.grid_item_youtube_view, group, false);
            holder = new GridItemYoutubeViewHolder(itemBinding);
            holder.view = itemBinding.getRoot();
            holder.view.setTag(holder);
        } else {
            holder = (GridItemYoutubeViewHolder) convertView.getTag();
        }
        if (youtubeInfo == null) {
            return holder.view;
        }
        try {

            holder.binding.itemYoutubeTvTitle.setText(youtubeInfo.title.toUpperCase());

            displayYoutubeThumb(holder.binding.itemYoutubeImgThumb, youtubeInfo.imageUrl);
            if (holder.binding.itemYoutubeImgThumb.getWidth() == 0) {
                int width = Utils.getYoutubeWidth();
                int height = width * 18 / 32;
                holder.binding.itemYoutubeImgThumb.setLayoutParams(new FrameLayout.LayoutParams(width, height));
            }
            holder.binding.itemYoutubeTvUploadedDate.setText(youtubeInfo.uploadedDate);
            holder.binding.itemYoutubeTvPlaysNo.setText(Utils.getDisplayViews(youtubeInfo.viewsNo, true));
            holder.binding.itemYoutubeTvTime.setText(Utils.getDisplayTime((int) youtubeInfo.duration));
            holder.binding.itemYoutubeTvUploader.setText(youtubeInfo.uploaderName);
            holder.binding.itemYoutubeImgAction.setTag(youtubeInfo);
            holder.binding.itemYoutubeImgAction.setOnClickListener(onClickListener);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return holder.view;
    }

    public static PopupMenu showYoutubeMenu(Constants.YoutubeListType youtubeListType,
                                            final Object dataContext, final View vDock) {
        PopupMenu popup = new PopupMenu(MainActivity.getInstance(), vDock);

        popup.getMenuInflater().inflate(R.menu.item_video, popup.getMenu());
        final YoutubeInfo youtubeInfo = (YoutubeInfo) vDock.getTag();
        if (youtubeListType != Constants.YoutubeListType.Playlist && youtubeListType != Constants.YoutubeListType.Recent) {
            popup.getMenu().removeItem(R.id.menu_item_remove);
        }
        popup.show();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    if (item.getItemId() == R.id.menu_item_add_to_playlist) {
                        showPlaylistsToAdd(youtubeInfo);
                    } else if (item.getItemId() == R.id.menu_item_add_to_favourites) {
                        PlaylistService.addYoutubeToFavouritePlaylist(youtubeInfo);
                        Utils.showMessage(getString(R.string.added));
                        MainActivity.getInstance().refreshPlaylistData();
                    } else if (item.getItemId() == R.id.menu_item_share) {
                        Utils.shareVideo(youtubeInfo);
                    } else if (item.getItemId() == R.id.menu_item_remove) {
                        if (dataContext != null && dataContext instanceof PlaylistInfo) {
                            removeYoutubeFromPlaylist((PlaylistInfo) dataContext, youtubeInfo);
                        } else {
                            removeYoutubeFromHistory(youtubeInfo);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
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
            if (convertView.getTag(Constants.CUSTOM_TAG) == null || !convertView.getTag(Constants.CUSTOM_TAG).equals(resId)) {
                v = inflater.inflate(resId, null);
                v.setTag(Constants.CUSTOM_TAG, resId);
            } else {
                v = convertView;
            }
        }
        return v;
    }

    public static BaseViewHolder getViewHolder(LayoutInflater inflater, View convertView, int resId, ViewGroup group) {
        BaseViewHolder holder;
        if (convertView == null || convertView.getTag(Constants.CUSTOM_TAG) == null || !convertView.getTag(Constants.CUSTOM_TAG).equals(resId)) {
            ViewDataBinding binding = DataBindingUtil.inflate(inflater, resId, group, false);
            holder = new BaseViewHolder(binding);
            holder.view.setTag(Constants.CUSTOM_TAG, resId);
            holder.view.setTag(holder);
        } else {
            holder = (BaseViewHolder) convertView.getTag();
        }

        return holder;
    }

    public static void displayYoutubeThumb(ImageView imageView, String url) {
        if (imageView.getTag() == null
                || !imageView.getTag().toString()
                .equalsIgnoreCase(url)) {
            imageView.setTag(url);
            imageView.setImageResource(R.drawable.ic_thumb);
            if (!Utils.stringIsNullOrEmpty(url)) {
                ImageLoader.getInstance().displayImage(
                        url, imageView);
            }
        }
    }

    public static void displayChannelThumb(ImageView imageView, String url) {
        if (imageView.getTag() == null
                || !imageView.getTag().toString()
                .equalsIgnoreCase(url)) {
            imageView.setTag(url);
            imageView.setImageResource(R.drawable.ic_user);
            if (!Utils.stringIsNullOrEmpty(url)) {
                ImageLoader.getInstance().displayImage(
                        url, imageView);
            }
        }
    }

    public static void showAddNewPlaylist(final YoutubeInfo youtubeInfo, final Dialog parent) {
        final View v = MainActivity.getInstance().getLayoutInflater().inflate(R.layout.edit_playlist, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.getInstance());
        builder.setPositiveButton(android.R.string.yes, null);
        builder.setNegativeButton(android.R.string.no, null);
        builder.setTitle(getString(R.string.add_new_playlist));
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
                            PlaylistService.addPlaylist(playlistInfo, youtubeInfo);
                            Utils.showMessage(getString(R.string.added));
                            dialog.dismiss();
                            parent.dismiss();
                            MainActivity.getInstance().refreshPlaylistData();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        });
        mAlertDialog.show();
    }

    public static void showAddNewPlaylist(final PlaylistInfo playlistInfo) {
        final View v = MainActivity.getInstance().getLayoutInflater().inflate(R.layout.edit_playlist, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.getInstance());
        builder.setPositiveButton(android.R.string.yes, null);
        builder.setNegativeButton(android.R.string.no, null);
        builder.setTitle(getString(R.string.add_new_playlist));
        if (playlistInfo != null) {
            EditText editText = v.findViewById(R.id.edit_playlist_edit_text);
            editText.setText(playlistInfo.title);
        }
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
                            PlaylistInfo playlistInfoNew = new PlaylistInfo();
                            if (playlistInfo == null) {
                                playlistInfoNew.id = UUID.randomUUID();
                            } else {
                                playlistInfoNew = playlistInfo;
                            }
                            playlistInfoNew.title = title;
                            if (playlistInfo == null) {
                                PlaylistService.addPlaylist(playlistInfoNew);
                            } else {
                                PlaylistService.updatePlaylist(playlistInfoNew);
                            }
                            Utils.showMessage(getString(R.string.added));
                            dialog.dismiss();
                            MainActivity.getInstance().refreshPlaylistData();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        });
        mAlertDialog.show();
    }

    public static void showPlaylistsToAdd(YoutubeInfo youtubeInfo) {

        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.getInstance());
        builder.setNegativeButton(android.R.string.no, null);

        final PlaylistsDialogView v = new PlaylistsDialogView(MainActivity.getInstance(), youtubeInfo);
        builder.setView(v);
        AlertDialog mAlertDialog = builder.create();
        v.mDialogParent = mAlertDialog;
        mAlertDialog.show();
    }

    private static void removeYoutubeFromPlaylist(final PlaylistInfo playlistInfo, final YoutubeInfo youtubeInfo) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.getInstance(),
                AlertDialog.THEME_HOLO_LIGHT);
        String msg = String.format(getString(R.string.remove_youtube_confirm), youtubeInfo.title);
        alert.setMessage(msg);

        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                PlaylistService.removeYoutube(playlistInfo, youtubeInfo);
                MainActivity.getInstance().refreshPlaylistData();
            }
        });

        alert.setNeutralButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        alert.show();
    }

    private static void removeYoutubeFromHistory(final YoutubeInfo youtubeInfo) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.getInstance(),
                AlertDialog.THEME_HOLO_LIGHT);
        String msg = String.format(getString(R.string.remove_youtube_confirm), youtubeInfo.title);
        alert.setMessage(msg);

        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                HistoryService.removeYoutubeHistory(youtubeInfo.id);
                MainActivity.getInstance().refreshHistoryData();
            }
        });

        alert.setNeutralButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        alert.show();
    }

    public static View getActivityYoutubeView(LayoutInflater inflater, View convertView, ViewGroup group,
                                              YoutubeInfo youtubeInfo,
                                              ChannelInfo channelInfo, PlaylistItemInfo itemViewInfo,
                                              View.OnClickListener onClickListener) {

        BaseViewHolder holder = getViewHolder(inflater, convertView, R.layout.item_user_activity, group);
        ItemUserActivityBinding binding = (ItemUserActivityBinding) holder.binding;
        ImageLoader.getInstance().displayImage(channelInfo.imageUrl,
                binding.imageViewChannelThumb);
        binding.textViewUploader.setText(channelInfo.title);
        String actionTitle = String.format(getString(R.string.user_action_description),
                getActionDescription(itemViewInfo.playlistItemType),
                itemViewInfo.time);
        binding.textViewOther.setText(actionTitle);

        binding.included.itemYoutubeTvTitle.setText(youtubeInfo.title.toUpperCase());
        displayYoutubeThumb(binding.included.itemYoutubeImgThumb, youtubeInfo.imageUrl);
        binding.included.itemYoutubeTvUploadedDate.setText(youtubeInfo.uploadedDate);
        binding.included.itemYoutubeTvPlaysNo.setText(Utils.getDisplayViews(youtubeInfo.viewsNo, false));
        binding.included.itemYoutubeTvLikesNo.setText(Utils.getDisplayLikes(youtubeInfo.likesNo, false));
        binding.included.itemYoutubeTvTime.setText(Utils.getDisplayTime((int) youtubeInfo.duration));
        binding.included.itemYoutubeTvUploader.setText(youtubeInfo.uploaderName);
        binding.included.itemYoutubeImgAction.setTag(youtubeInfo);
        binding.included.itemYoutubeImgAction.setOnClickListener(onClickListener);

        return holder.view;
    }

    private static String getActionDescription(int actionType) {
        switch (actionType) {
            case Constants.PlaylistItemType.UPLOADED: {
                return getString(R.string.uploaded);
            }
            case Constants.PlaylistItemType.LIKED: {
                return getString(R.string.liked);
            }
            case Constants.PlaylistItemType.COMMENTED: {
                return getString(R.string.commented);
            }
            case Constants.PlaylistItemType.UPLOADEDANDPOSTED: {
                return getString(R.string.uploaded_and_posted);
            }
            case Constants.PlaylistItemType.SUBSCRIBED: {
                return getString(R.string.subscribed);
            }
            case Constants.PlaylistItemType.RECOMMENDED: {
                return getString(R.string.recommended);
            }
            default: {
                return getString(R.string.had_an_action_on);
            }
        }
    }
}
