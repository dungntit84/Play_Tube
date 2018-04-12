package com.hteck.playtube.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.common.ViewHelper;
import com.hteck.playtube.data.CommentInfo;
import com.hteck.playtube.data.YoutubeInfo;

import java.util.Vector;

public class PlayerYoutubeInfoAdapter extends BaseAdapter {
    public Vector<CommentInfo> _commentList = new Vector<>();
    private YoutubeInfo _youtubeInfo;
    private boolean _isNetworkError;
    public PlayerYoutubeInfoAdapter(YoutubeInfo youtubeInfo) {
        super();
        _youtubeInfo = youtubeInfo;
    }

    public void setDataSource(YoutubeInfo youtubeInfo, Vector<CommentInfo> commentList) {
        _youtubeInfo = youtubeInfo;
        _commentList = commentList;
    }

    public Vector<CommentInfo> getCommentList() {
        return _commentList;
    }

    public void setDataSource(YoutubeInfo youtubeInfo) {
        _youtubeInfo = youtubeInfo;
        notifyDataSetChanged();
    }

    public void setIsNetworkError(boolean isNetworkError) {
        _isNetworkError = isNetworkError;
    }

    public boolean getIsNetworkError() {
        return _isNetworkError;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup group) {
        View v = null;
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();
        if (position == 0) {
            return getYoutubeInfoView(convertView);
        }
        if (position == 1) {
            v = (ViewGroup) inflater.inflate(R.layout.header_template_view, null);
            v.setTag(Constants.CUSTOM_TAG, R.layout.header_template_view);
            return v;
        }
        if (position - 1 == _commentList.size()) {
            if (_commentList.lastElement() == null) {
                if (!_isNetworkError) {
                    v = inflater.inflate(R.layout.loading_view, null);
                } else {
                    v = inflater.inflate(R.layout.item_load_more, null);
                    TextView textView = v.findViewById(R.id.item_load_more_tv_msg);
                    textView.setText(Utils.getString(R.string.network_error_info));
                }
                v.setTag(Constants.CUSTOM_TAG, null);
                return v;
            }
        }

        CommentInfo userCommentInfo = _commentList.get(position - 2);

        v = ViewHelper.getConvertView(convertView, R.layout.item_comment);
        TextView textView = (TextView) v.findViewById(R.id.item_comment_text_view_title);
        textView.setText(userCommentInfo.userName);

        textView = (TextView) v.findViewById(R.id.item_comment_text_view_time);
        textView.setText(userCommentInfo.commentedDate);

        textView = (TextView) v.findViewById(R.id.item_comment_text_view_content);
        textView.setText(userCommentInfo.details);

        return v;
    }

    @Override
    public int getCount() {

        return _commentList.size() + 2;
    }

    @Override
    public Object getItem(int position) {
        if (position > 2 && _commentList.size() > position - 2) {
            return _commentList.elementAt(position - 2);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private View getYoutubeInfoView(View convertView) {
        View v = ViewHelper.getConvertView(convertView, R.layout.player_info);
        TextView textViewTitle = v.findViewById(R.id.player_info_tv_title);
        textViewTitle.setText(_youtubeInfo.title.toUpperCase());

        ImageView iv = (ImageView) v.findViewById(R.id.player_info_img_thumb);
        ViewHelper.displayYoutubeThumb(iv, _youtubeInfo.imageUrl);

        TextView textViewUploadedDate = (TextView) v.findViewById(R.id.player_info_tv_uploaded_date);
        textViewUploadedDate.setText(_youtubeInfo.uploadedDate);
        TextView textViewPlaysNo = (TextView) v.findViewById(R.id.player_info_tv_plays_no);
        textViewPlaysNo.setText(Utils.getDisplayViews(_youtubeInfo.viewsNo, false));

        TextView textViewLikesNo = (TextView) v.findViewById(R.id.player_info_tv_likes_no);
        textViewLikesNo.setText(Utils.getDisplayLikes(_youtubeInfo.likesNo, false));
        TextView textViewDislikesNo = (TextView) v.findViewById(R.id.player_info_tv_dislikes_no);
        textViewDislikesNo.setText(Utils.getDisplayLikes(_youtubeInfo.dislikesNo, false));
        TextView textViewTime = (TextView) v.findViewById(R.id.player_info_tv_time);
        textViewTime.setText(Utils.getDisplayTime((int) _youtubeInfo.duration));
        TextView textViewUploader = (TextView) v.findViewById(R.id.player_info_tv_uploader);
        textViewUploader.setText(_youtubeInfo.uploaderName);
        TextView textViewDescription = (TextView) v.findViewById(R.id.player_info_text_view_description);
        textViewDescription.setText(_youtubeInfo.description);
        v.setTag(Constants.CUSTOM_TAG, R.layout.player_info);
        ImageView imageViewAction = (ImageView) v
                .findViewById(R.id.player_info_img_action);

        imageViewAction.setTag(_youtubeInfo);
        imageViewAction.setOnClickListener(onClickListener);
        return v;
    }
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewHelper.showYoutubeMenu(Constants.YoutubeListType.Normal, null, v);
        }
    };
}
