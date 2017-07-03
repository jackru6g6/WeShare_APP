package com.example.ntut.weshare.message;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ntut.weshare.Common;
import com.example.ntut.weshare.R;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class MessageFragment extends Fragment {
    private static final String TAG = "MsgListFragment";
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvMsgs;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_fragment, container, false);

        swipeRefreshLayout =
                (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {//監聽有沒有發生下拉，有執行onRefresh()
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);//播放動畫
                showAllMsgs();//更新
                swipeRefreshLayout.setRefreshing(false);//停止動畫
            }
        });

        rvMsgs = (RecyclerView) view.findViewById(R.id.rvMsgs);
        rvMsgs.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    private void showAllMsgs() {
        if (Common.networkConnected(getActivity())) {//檢查網路
            String url = Common.URL + "MsgServlet";
            List<MessageBean> msgs = null;
            SharedPreferences pref = getActivity().getSharedPreferences(Common.PREF_FILE, MODE_PRIVATE);
            String account = pref.getString("user", "");
            try {//抓全部景點
                msgs = new MsgGetAllTask().execute(url, account).get();//.get()要請SpotGetAllTask()的執行結果回傳給我，會等他抓完資料(doInBackground的回傳結果)才會往下執行
                //先抓文字的資料，因為資料小，圖片則是要畫面滑動的過程中才會抓取
                //如果連圖一起抓，會變很慢，而且避免塞爆user的記憶體
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (msgs == null || msgs.isEmpty()) {
                Common.showToast(getActivity(), R.string.msg_NoMsgsFound);
            } else {
                //Common.showToast(getActivity(), R.string.msg_NoMsgsFound);
                rvMsgs.setAdapter(new MsgsRecyclerViewAdapter(getActivity(), msgs));//畫面RecyclerView(畫面,資料)，getActivity()取的他所依附的頁面(主頁面)
            }
        } else {
            Common.showToast(getActivity(), R.string.msg_NoNetwork);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        showAllMsgs();
    }

    private class MsgsRecyclerViewAdapter extends RecyclerView.Adapter<MsgsRecyclerViewAdapter.MyViewHolder> {//CH05 RecyclerView
        private LayoutInflater layoutInflater;
        private List<MessageBean> msgs;


        public MsgsRecyclerViewAdapter(Context context, List<MessageBean> msgs) {
            layoutInflater = LayoutInflater.from(context);
            this.msgs = msgs;
        }

        @Override
        public int getItemCount() {
            return msgs.size();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.message_recycleview_yes_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder myViewHolder, int position) {//將圖文顯示出來
            final MessageBean msg = msgs.get(position);//文字資料
            String url = Common.URL + "UserServlet";
            String accountId = null;
            SharedPreferences pref = getActivity().getSharedPreferences(Common.PREF_FILE, MODE_PRIVATE);
            String account = pref.getString("user", "");
            if (account.equalsIgnoreCase(msgs.get(position).getMsgSourceId())) {
                accountId = msg.getMsgEndId();//取的文字的id
            } else if (account.equalsIgnoreCase(msgs.get(position).getMsgEndId())) {
                accountId = msg.getMsgSourceId();//取的文字的id
            }
            int imageSize = 250;//要縮圖的大小像素，要放在250*250的框
            //這邊啟動AsyncTask，抓圖片
            //不用.get()，不然會卡畫面，這邊利用SpotGetImageTask(myViewHolder.imageView)放圖，myViewHolder.imageView將imageView元件傳給AsyncTask，再用onPostExecute()將圖貼上
            new UserGetImageTask(myViewHolder.ivUserPic).execute(url, accountId, imageSize);//.execute(網址, 圖片id, 這邊做縮圖imageSize，在server端縮圖完再傳過來)

//            SharedPreferences pref = getActivity().getSharedPreferences(Common.PREF_FILE, MODE_PRIVATE);
//            String account = pref.getString("user", "");
            if (account.equalsIgnoreCase(msg.getMsgSourceId())) {
                myViewHolder.tvMsgName.setText(msg.getMsgEndId());
            } else if (account.equalsIgnoreCase(msg.getMsgEndId())) {
                myViewHolder.tvMsgName.setText(msg.getMsgSourceId());
            }
            myViewHolder.tvText.setText(msg.getMsgText());
            myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Intent intent = new Intent();
//                    intent.setClass(getActivity(), MessageReplyActivity.class);
//                    Bundle bundle = new Bundle();
//                    bundle.putSerializable("msg", msg);
//                    intent.putExtra("intentMsgs", bundle);
//                    startActivity(intent);

                    Fragment fragment = new MessageReplyFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("msg", msg);
                    fragment.setArguments(bundle);
                    switchFragment(fragment);
                }
            });
//            myViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    PopupMenu popupMenu = new PopupMenu(getActivity(), view, Gravity.END);
//                    popupMenu.inflate(R.menu.popup_menu);
//                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                        @Override
//                        public boolean onMenuItemClick(MenuItem item) {
//                            switch (item.getItemId()) {
//                                case R.id.update:
//                                    Intent updateIntent = new Intent(getActivity(),
//                                            SpotUpdateActivity.class);
//                                    Bundle bundle = new Bundle();
//                                    bundle.putSerializable("spot", spot);
//                                    updateIntent.putExtras(bundle);
//                                    startActivity(updateIntent);
//                                    break;
//                                case R.id.delete:
//                                    if (Common.networkConnected(getActivity())) {
//                                        String url = Common.URL + "SpotServlet";
//                                        String action = "spotDelete";
//                                        int count = 0;
//                                        try {
//                                            count = new SpotUpdateTask().execute(url, action, spot, null).get();
//                                        } catch (Exception e) {
//                                            Log.e(TAG, e.toString());
//                                        }
//                                        if (count == 0) {
//                                            Common.showToast(getActivity(), R.string.msg_DeleteFail);
//                                        } else {
//                                            spots.remove(spot);
//                                            SpotsRecyclerViewAdapter.this.notifyDataSetChanged();
//                                            Common.showToast(getActivity(), R.string.msg_DeleteSuccess);
//                                        }
//                                    } else {
//                                        Common.showToast(getActivity(), R.string.msg_NoNetwork);
//                                    }
//                            }
//                            return true;
//                        }
//                    });
//                    popupMenu.show();
//                    return true;
//                }
//                }

//                );
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivUserPic;
            TextView tvMsgName, tvText;

            public MyViewHolder(View itemView) {
                super(itemView);
                ivUserPic = (ImageView) itemView.findViewById(R.id.ivUserPic);
                tvMsgName = (TextView) itemView.findViewById(R.id.tvMsgName);
                tvText = (TextView) itemView.findViewById(R.id.tvText);

            }
        }
    }

    private void switchFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.body, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
