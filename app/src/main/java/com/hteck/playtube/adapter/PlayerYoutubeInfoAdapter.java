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
import com.hteck.playtube.databinding.ItemCommentBinding;
import com.hteck.playtube.databinding.ItemLoadMoreBinding;
import com.hteck.playtube.databinding.PlayerInfoBinding;
import com.hteck.playtube.holder.BaseViewHolder;

import java.util.Vector;

public class PlayerYoutubeInfoAdapter extends BaseAdapter {
    private Vector<CommentInfo> _commentList = new Vector<>();
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
        BaseViewHolder holder;
        if (position == 0) {
            holder = getYoutubeInfoView(convertView, group);
        } else if (position == 1) {
            holder = ViewHelper.getViewHolder(convertView, R.layout.header_template_view, group);
        } else if (position - 1 == _commentList.size() && _commentList.lastElement() == null) {
            if (!_isNetworkError) {
                holder = ViewHelper.getViewHolder(convertView, R.layout.loading_view, group);
            } else {
                holder = ViewHelper.getViewHolder(convertView, R.layout.item_load_more, group);
                ((ItemLoadMoreBinding) holder.binding).itemLoadMoreTvMsg.setText(Utils.getString(R.string.network_error_info));
            }
        } else {
            CommentInfo userCommentInfo = _commentList.get(position - 2);
            holder = ViewHelper.getViewHolder(convertView, R.layout.item_comment, group);
            ItemCommentBinding binding = ((ItemCommentBinding) holder.binding);
            binding.itemCommentTextViewTitle.setText(userCommentInfo.userName);
            binding.itemCommentTextViewTime.setText(userCommentInfo.commentedDate);
            binding.itemCommentTextViewContent.setText(userCommentInfo.details);
        }
        return holder.view;
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

    private BaseViewHolder getYoutubeInfoView(View convertView, ViewGroup group) {
        BaseViewHolder holder = ViewHelper.getViewHolder(convertView, R.layout.player_info, group);
        PlayerInfoBinding binding = (PlayerInfoBinding) holder.binding;
        binding.playerInfoTvTitle.setText(_youtubeInfo.title.toUpperCase());
        ViewHelper.displayYoutubeThumb(binding.playerInfoImgThumb, _youtubeInfo.imageUrl);
        binding.playerInfoTvUploadedDate.setText(_youtubeInfo.uploadedDate);
        binding.playerInfoTvPlaysNo.setText(Utils.getDisplayViews(_youtubeInfo.viewsNo, false));
        binding.playerInfoTvLikesNo.setText(Utils.getDisplayLikes(_youtubeInfo.likesNo, false));
        binding.playerInfoTvDislikesNo.setText(Utils.getDisplayLikes(_youtubeInfo.dislikesNo, false));
        binding.playerInfoTvTime.setText(Utils.getDisplayTime((int) _youtubeInfo.duration));
        binding.playerInfoTvUploader.setText(_youtubeInfo.uploaderName);
        binding.playerInfoTextViewDescription.setText(_youtubeInfo.description);
        binding.playerInfoImgAction.setTag(_youtubeInfo);
        binding.playerInfoImgAction.setOnClickListener(onClickListener);
        return holder;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewHelper.showYoutubeMenu(Constants.YoutubeListType.Normal, null, v);
        }
    };
}
