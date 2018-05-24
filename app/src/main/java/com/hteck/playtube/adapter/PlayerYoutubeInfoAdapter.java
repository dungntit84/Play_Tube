package com.hteck.playtube.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
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

import java.util.ArrayList;
import java.util.Vector;

public class PlayerYoutubeInfoAdapter extends BaseAdapter {
    private Context _context;
    private ArrayList<CommentInfo> _commentList = new ArrayList<>();
    private YoutubeInfo _youtubeInfo;
    private boolean _isNetworkError;

    public PlayerYoutubeInfoAdapter(Context context, YoutubeInfo youtubeInfo) {
        super();
        _context = context;
        _youtubeInfo = youtubeInfo;
    }

    public void setDataSource(YoutubeInfo youtubeInfo, ArrayList<CommentInfo> commentList) {
        _youtubeInfo = youtubeInfo;
        _commentList = commentList;
    }

    public ArrayList<CommentInfo> getCommentList() {
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
        switch (getItemViewType(position)) {
            case 0:
            case 1: {
                return ViewHelper.getNetworkErrorView(getItemViewType(position), _context, convertView, group);
            }
            case 2: {
                return getYoutubeInfoView(convertView, group);
            }
            case 3: {
                return ViewHelper.getViewHolder1(LayoutInflater.from(_context), convertView, group, R.layout.header_template_view);
            }
            default: {
                CommentInfo userCommentInfo = _commentList.get(position - 2);
                convertView = ViewHelper.getViewHolder1(LayoutInflater.from(_context), convertView, group, R.layout.item_comment);
                ItemCommentBinding binding = DataBindingUtil.getBinding(convertView);
                binding.itemCommentTextViewTitle.setText(userCommentInfo.userName);
                binding.itemCommentTextViewTime.setText(userCommentInfo.commentedDate);
                binding.itemCommentTextViewContent.setText(userCommentInfo.details);
                return convertView;
            }
        }
    }

    @Override
    public int getCount() {

        return _commentList.size() + 2;
    }

    @Override
    public Object getItem(int position) {
        if (position > 2 && _commentList.size() > position - 2) {
            return _commentList.get(position - 2);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 2;
        } else if (position == 1) {
            return 3;
        } else if (position - 1 == _commentList.size() && _commentList.get(_commentList.size() - 1) == null) {
            if (!_isNetworkError) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return 4;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }

    private View getYoutubeInfoView(View convertView, ViewGroup group) {
        if (convertView == null) {
            convertView = ViewHelper.getViewHolder1(LayoutInflater.from(_context), convertView, group, R.layout.player_info);
        }
        PlayerInfoBinding binding = DataBindingUtil.getBinding(convertView);
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
        return convertView;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewHelper.showYoutubeMenu(Constants.YoutubeListType.Normal, null, v);
        }
    };
}
