package com.hteck.playtube.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.common.ViewHelper;
import com.hteck.playtube.data.ChannelInfo;
import com.hteck.playtube.data.PlaylistInfo;
import com.hteck.playtube.data.PlaylistItemInfo;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.data.YoutubePlaylistInfo;
import com.hteck.playtube.databinding.ChannelHeaderBinding;
import com.hteck.playtube.databinding.HeaderTemplateViewBinding;
import com.hteck.playtube.fragment.UserActivityFragment;
import com.hteck.playtube.holder.BaseViewHolder;

import java.util.ArrayList;
import java.util.Vector;

public class YoutubePlaylistItemAdapter extends BaseAdapter {
    protected ArrayList<PlaylistItemInfo> _items = new ArrayList<>();
    private ChannelInfo mChannel;
    private ChannelInfo mSubscriptionInfo;
    public boolean mIsSubscribed;
    private boolean _isPendingToSubscribe = false;
    private boolean _isPendingAction = false;
    private boolean _isCheckingSubscribed = false;
    private boolean _isSubscribedStateChecked;
    UserActivityFragment _channelHomeTab;
    protected Context _context;

    public YoutubePlaylistItemAdapter(Context context, UserActivityFragment channelHomeTab, ChannelInfo channelInfo, ArrayList<PlaylistItemInfo> items) {
        super();
        _context = context;
        _items = items;
        mChannel = channelInfo;
        _channelHomeTab = channelHomeTab;

        checkChannelSubscribed(true);
    }

    public void setDataSource(ArrayList<PlaylistItemInfo> items) {
        _items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return _items.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        return _items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup group) {
        int viewType = getItemViewType(position);
        if (viewType == 2) {
            return getChannelInfoView(convertView, group);
        }
        PlaylistItemInfo playlistItemViewInfo = _items
                .get(position - 1);
        LayoutInflater inflater = LayoutInflater.from(_context);
        if (viewType == 3) {
            convertView = ViewHelper.getViewHolder1(LayoutInflater.from(_context), convertView, group, R.layout.header_template_view);
            HeaderTemplateViewBinding binding = DataBindingUtil.getBinding(convertView);
            String title = playlistItemViewInfo.dataInfo.toString();
            binding.headerTemplateTextViewTitle.setText(title);

        } else if (viewType == 4) {
            YoutubeInfo youtubeInfo = (YoutubeInfo) playlistItemViewInfo.dataInfo;
            OnClickListener onClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = ViewHelper.showYoutubeMenu(
                            Constants.YoutubeListType.Normal, null, v);

                }
            };
            convertView = ViewHelper.getYoutubeView(convertView, group, youtubeInfo,
                    onClickListener);
        } else if (viewType == 5) {
            YoutubePlaylistInfo playlistInfo = (YoutubePlaylistInfo) playlistItemViewInfo.dataInfo;

            convertView = YoutubePlaylistAdapter.getDetailsView(inflater, convertView, group, playlistInfo);
        } else if (viewType == 6) {
            ChannelInfo channelInfo = (ChannelInfo) playlistItemViewInfo.dataInfo;

            convertView = ChannelAdapter.getDetailsView(inflater, convertView, group, channelInfo);
        } else if (viewType == 7) {
            convertView = ViewHelper.getViewHolder1(inflater, convertView, group, R.layout.show_all);
        } else if (viewType == 8) {
            YoutubeInfo youtubeInfo = (YoutubeInfo) playlistItemViewInfo.dataInfo;
            OnClickListener onClickListener = new OnClickListener() {
                @SuppressLint("NewApi")
                @Override
                public void onClick(View v) {
                    ViewHelper.showYoutubeMenu(Constants.YoutubeListType.Normal, null, v);

                }
            };
            convertView = ViewHelper.getActivityYoutubeView(inflater, convertView, group,
                    youtubeInfo, mChannel, playlistItemViewInfo,
                    onClickListener);
        } else {
            convertView = ViewHelper.getViewHolder1(inflater, convertView, group, R.layout.separator_view);
        }
        return convertView;
    }

    private View getChannelInfoView(View convertView, ViewGroup group) {
        convertView = ViewHelper.getViewHolder1(LayoutInflater.from(_context), convertView, group, R.layout.channel_header);
        ChannelHeaderBinding binding = DataBindingUtil.getBinding(convertView);

        binding.textViewTitle.setText(mChannel.title);
        String ex = mChannel.subscriberCount > 1 ? " subscribers"
                : " subscriber";
        binding.textViewSubcriberCount.setText(Utils
                .formatNumber(mChannel.subscriberCount, false) + ex);
        if (_isSubscribedStateChecked) {
            setSubscribeButtonState(binding.buttonSubscribe);
        }
        binding.buttonSubscribe.setOnClickListener(new OnClickListener() {

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
                                    unsubscribeChannel(true);
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

        return convertView;
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
//        YoutubeService ys = new YoutubeService(new IYoutubeAccountService() {
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

    private void setSubscribeButtonState(View buttonSubscribe) {
        buttonSubscribe.setBackgroundResource(mIsSubscribed ? R.drawable.ic_subscribed
                : R.drawable.ic_subscribe);

        buttonSubscribe.setVisibility(View.VISIBLE);
    }

    public void doPendingAction() {
//        if (_isPendingAction && _isPendingToSubscribe) {
//            if (mIsSubscribed) {
//                unsubscribeChannel(false);
//            } else {
//                _isCheckingSubscribed = true;
//                YoutubeService ys = new YoutubeService(
//                        new IYoutubeAccountService() {
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
//        YoutubeService ys = new YoutubeService(new IYoutubeAccountService() {
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
//        YoutubeService ys = new YoutubeService(new IYoutubeAccountService() {
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
