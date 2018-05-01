package com.hteck.playtube.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.ViewHelper;
import com.hteck.playtube.data.ChannelInfo;
import com.hteck.playtube.data.PlaylistInfo;
import com.hteck.playtube.data.PlaylistItemInfo;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.data.YoutubePlaylistInfo;
import com.hteck.playtube.databinding.HeaderTemplateViewBinding;
import com.hteck.playtube.fragment.UserActivityFragment;
import com.hteck.playtube.holder.BaseViewHolder;

import java.util.Vector;

public class YoutubePlaylistItemAdapter extends BaseAdapter {
    private Vector<PlaylistItemInfo> _items = new Vector<PlaylistItemInfo>();
    private ChannelInfo mChannel;
    private ChannelInfo mSubscriptionInfo;
    public boolean mIsSubscribed;
    private boolean _isPendingToSubscribe = false;
    private boolean _isPendingAction = false;
    private boolean _isCheckingSubscribed = false;
    private boolean _isSubscribedStateChecked;
    UserActivityFragment _channelHomeTab;
    private Context _context;

    public YoutubePlaylistItemAdapter(Context context, UserActivityFragment channelHomeTab, ChannelInfo channelInfo, Vector<PlaylistItemInfo> items) {
        super();
        _context = context;
        _items = items;
        mChannel = channelInfo;
        _channelHomeTab = channelHomeTab;

        checkChannelSubscribed(true);
    }

    public void setDataSource(Vector<PlaylistItemInfo> items) {
        _items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return _items.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        return _items.elementAt(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup group) {
        BaseViewHolder holder;
        if (position == 0) {
            return getChannelInfoView();
        }
        int otherPosition = position - 1;
        PlaylistItemInfo playlistItemViewInfo = _items
                .elementAt(otherPosition);
        LayoutInflater inflater = LayoutInflater.from(_context);
        View v;
        if (playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.NAME) {
            holder = ViewHelper.getViewHolder(LayoutInflater.from(_context), convertView, R.layout.header_template_view, group);
            HeaderTemplateViewBinding binding = (HeaderTemplateViewBinding) holder.binding;
            String title = playlistItemViewInfo.dataInfo.toString();
            binding.headerTemplateTextViewTitle.setText(title);
            v = holder.view;
        } else if (playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.YOUTUBE) {
            YoutubeInfo youtubeInfo = (YoutubeInfo) playlistItemViewInfo.dataInfo;
            OnClickListener onClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = ViewHelper.showYoutubeMenu(
                            Constants.YoutubeListType.Normal, null, v);

                }
            };
            v = ViewHelper.getYoutubeView(convertView, group, youtubeInfo,
                    onClickListener);
        } else if (playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.PLAYLIST) {
            YoutubePlaylistInfo playlistInfo = (YoutubePlaylistInfo) playlistItemViewInfo.dataInfo;

            v = YoutubePlaylistAdapter.getDetailsView(inflater, convertView, group, playlistInfo);
        } else if (playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.CHANNEL) {
            ChannelInfo channelInfo = (ChannelInfo) playlistItemViewInfo.dataInfo;

            v = ChannelAdapter.getDetailsView(inflater, convertView, group, channelInfo);
        } else if (playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.SHOWMORE) {
            v = inflater.inflate(R.layout.show_all, null);
            v.setTag(playlistItemViewInfo.dataInfo);
        } else if (playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.UPLOADED
                || playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.COMMENTED
                || playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.UPLOADEDANDPOSTED
                || playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.RECOMMENDED
                || playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.SUBSCRIBED
                || playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.OTHERACTION
                || playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.LIKED) {
            YoutubeInfo youtubeInfo = (YoutubeInfo) playlistItemViewInfo.dataInfo;
            OnClickListener onClickListener = new OnClickListener() {
                @SuppressLint("NewApi")
                @Override
                public void onClick(View v) {
                    ViewHelper.showYoutubeMenu(Constants.YoutubeListType.Normal, null, v);

                }
            };
            v = ViewHelper.getActivityYoutubeView(inflater, convertView, group,
                    youtubeInfo, mChannel, playlistItemViewInfo,
                    onClickListener);
        } else {
            v = inflater.inflate(R.layout.separator_view, null);
        }
        return v;
    }

    private View getChannelInfoView() {
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();
        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.user_header,
                null);

        TextView textViewTitle = (TextView) v
                .findViewById(R.id.tv_title);
        textViewTitle.setText(mChannel.title);

        TextView textViewNumOfSubcribers = (TextView) v
                .findViewById(R.id.text_view_num_subcribers);
        String ex = mChannel.numSubscribers > 1 ? " subscribers"
                : " subscriber";
        textViewNumOfSubcribers.setText(Utils
                .formatNumber(mChannel.numSubscribers, false) + ex);
        if (_isSubscribedStateChecked) {
            setSubscribeButtonState(v);
        }

        View bt = v.findViewById(R.id.button_subscribe);
        bt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mIsSubscribed) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            MainActivity.getInstance(),
                            AlertDialog.THEME_HOLO_LIGHT);
                    builder.setTitle("Confirmation");
                    builder.setMessage(Utils
                            .getString(R.string.unsubscribe_confirm));
                    builder.setPositiveButton(
                            "OK",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    try {
                                        unsubscribeChannel(true);
                                    } catch (Exception e) {
                                        Utils.showErrorMessage(e);
                                    }
                                }
                            });

                    builder.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    dialog.cancel();
                                }
                            });

                    builder.show();
                } else {
                    subscribeChannel(true);
                }
            }
        });

        return v;
    }

    public void checkChannelSubscribed(boolean isCheckLogedIn) {
//        if (mChannel == null || _isCheckingSubscribed
//                || _isSubscribedStateChecked) {
//            return;
//        }
//        if (isCheckLogedIn
//                && !AccountContext.getInstance().isYoutubeLoggedIn()) {
//            _isSubscribedStateChecked = true;
//            notifyDataSetChanged();
//            return;
//        }
//        _isCheckingSubscribed = true;
//        YoutubeService ys = new YoutubeService(new IYoutubeService() {
//
//            @Override
//            public void onServiceDoneSuccess(Object sender, int param1,
//                                             final Object data) {
//                MainActivity.getInstance().runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        if (data != null) {
//                            String s = data.toString();
//
//                            KeyPairValue<String, Vector<ChannelInfo>> searchResult = YoutubeHelper
//                                    .getSubscriptions(s);
//                            Vector<ChannelInfo> items = searchResult.getValue();
//                            if (items.size() > 0) {
//                                mSubscriptionInfo = items.elementAt(0);
//                            }
//                            mIsSubscribed = items.size() > 0;
//                        }
//                        _isSubscribedStateChecked = true;
//                        notifyDataSetChanged();
//
//                        _isCheckingSubscribed = false;
//                    }
//                });
//
//            }
//
//            @Override
//            public void onServiceDoneFailed(Object sender, int code,
//                                            String error) {
//
//                Utils.showMessageToast(error);
//                _isCheckingSubscribed = false;
//            }
//        });
//
//        ys.checkChannelSubscribed(mChannel.id);
    }

    private void setSubscribeButtonState(View parent) {
//        View bt = parent.findViewById(R.id.button_subscribe);
//
//        bt.setBackgroundResource(mIsSubscribed ? R.drawable.subscribe_ok
//                : R.drawable.subscribe);
//
//        bt.setVisibility(View.VISIBLE);
    }

    public void doPendingAction() {
//        if (_isPendingAction && _isPendingToSubscribe) {
//            if (mIsSubscribed) {
//                unsubscribeChannel(false);
//            } else {
//                _isCheckingSubscribed = true;
//                YoutubeService ys = new YoutubeService(
//                        new IYoutubeService() {
//
//                            @Override
//                            public void onServiceDoneSuccess(Object sender,
//                                                             int param1, final Object data) {
//                                MainActivity.getInstance().runOnUiThread(
//                                        new Runnable() {
//
//                                            @Override
//                                            public void run() {
//                                                if (data != null) {
//                                                    String s = data.toString();
//
//                                                    KeyPairValue<String, Vector<ChannelInfo>> searchResult = YoutubeHelper
//                                                            .getSubscriptions(s);
//                                                    Vector<ChannelInfo> items = searchResult
//                                                            .getValue();
//                                                    if (items.size() > 0) {
//                                                        mSubscriptionInfo = items
//                                                                .elementAt(0);
//                                                    }
//                                                    mIsSubscribed = items
//                                                            .size() > 0;
//                                                }
//                                                _isCheckingSubscribed = false;
//
//                                                if (mIsSubscribed) {
//                                                    notifyDataSetChanged();
//                                                } else {
//                                                    subscribeChannel(false);
//                                                }
//                                            }
//                                        });
//
//                            }
//
//                            @Override
//                            public void onServiceDoneFailed(Object sender,
//                                                            int code, String error) {
//
//                                Utils.showMessageToast(error);
//                                _isCheckingSubscribed = false;
//                            }
//                        });
//
//                ys.checkChannelSubscribed(mChannel.id);
//            }
//        }
    }

    void unsubscribeChannel(boolean isCheckLogedIn) {
//        if (isCheckLogedIn
//                && !AccountContext.getInstance().isYoutubeLoggedIn()) {
//            _isPendingToSubscribe = true;
//            _isPendingAction = true;
//            notifyDataSetChanged();
//            MainActivity.getInstance().signinYoutube(
//                    AccountAction.SubscribeOnChannelDetails);
//            return;
//        }
//
//        if (mSubscriptionInfo == null) {
//            checkChannelSubscribed(isCheckLogedIn);
//            return;
//        }
//        _channelHomeTab.showBusyAnimation();
//        YoutubeService ys = new YoutubeService(new IYoutubeService() {
//
//            @Override
//            public void onServiceDoneSuccess(Object userToken, int param1,
//                                             Object data) {
//                MainActivity.getInstance().runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        Utils.showMessageToast(Utils
//                                .getString(R.string.unsubscribed));
//
//                        mIsSubscribed = false;
//
//                        notifyDataSetChanged();
//                        _channelHomeTab.hideBusyAnimation();
//
//                        AccountContext.getInstance().mIsReloadMySubscriptions = true;
//
//                        mSubscriptionInfo = null;
//                    }
//                });
//            }
//
//            @Override
//            public void onServiceDoneFailed(Object userToken, int code,
//                                            String error) {
//                Utils.showMessageToast(error);
//                MainActivity.getInstance().runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        _channelHomeTab.hideBusyAnimation();
//                    }
//                });
//            }
//        });
//        ys.removeSubscription(mSubscriptionInfo.subscriptionId);
    }

    void subscribeChannel(boolean isCheckLogedIn) {
//        if (isCheckLogedIn
//                && !AccountContext.getInstance().isYoutubeLoggedIn()) {
//            _isPendingToSubscribe = true;
//            _isPendingAction = true;
//            notifyDataSetChanged();
//            MainActivity.getInstance().signinYoutube(
//                    AccountAction.SubscribeOnChannelDetails);
//            return;
//        }
//        _channelHomeTab.showBusyAnimation();
//        YoutubeService ys = new YoutubeService(new IYoutubeService() {
//
//            @Override
//            public void onServiceDoneSuccess(Object userToken, int param1,
//                                             final Object data) {
//                MainActivity.getInstance().runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        Utils.showMessageToast(Utils
//                                .getString(R.string.subscribed));
//
//                        mIsSubscribed = true;
//
//                        notifyDataSetChanged();
//
//                        _channelHomeTab.hideBusyAnimation();
//
//                        AccountContext.getInstance().mIsReloadMySubscriptions = true;
//                        mSubscriptionInfo = (ChannelInfo) data;
//                    }
//                });
//            }
//
//            @Override
//            public void onServiceDoneFailed(Object userToken, int code,
//                                            String error) {
//                Utils.showMessageToast(error);
//                MainActivity.getInstance().runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        _channelHomeTab.hideBusyAnimation();
//                    }
//                });
//            }
//        });
//        ys.insertSubscription(mChannel.id);
    }
}
