package com.example.ntut.weshare.goods;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ntut.weshare.Common;
import com.example.ntut.weshare.MainActivity;
import com.example.ntut.weshare.R;

import java.text.SimpleDateFormat;
import java.util.List;


public class GoodsBoxPageChange extends Fragment {
    private static final String TAG = "GoodsBoxPageChange";
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvGoods;
    private ImageView ivNoGoods;

    public void onResume() {
        super.onResume();
        // 從偏好設定檔中取得登入狀態來決定是否顯示「登出」

        SharedPreferences pref = this.getActivity().getSharedPreferences(Common.PREF_FILE, Context.MODE_PRIVATE);
        String user = pref.getString("user", "");
        if (user == "") {
            Toast.makeText(this.getActivity(), "請註冊登入WeShare後，再過來設定您的物資箱喔~",
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
            Intent MainIntent = new Intent(getActivity(), MainActivity.class);
            startActivity(MainIntent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.goodsbox_page_change_fragment, container, false);

        swipeRefreshLayout =
                (SwipeRefreshLayout) view.findViewById(R.id.gb_swipeRefreshLayoutC);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                showAllGoods();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        ivNoGoods = (ImageView) view.findViewById(R.id.ivNoGoods);
        rvGoods = (RecyclerView) view.findViewById(R.id.rvGoodsC);
        rvGoods.setLayoutManager(new LinearLayoutManager(getActivity()));
        FloatingActionButton btAdd = (FloatingActionButton) view.findViewById(R.id.btAdd);
        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent insertIntent = new Intent(getActivity(), GoodsInsertActivity.class);
                startActivity(insertIntent);

            }
        });
        ivNoGoods.setVisibility(View.GONE);
        return view;
    }


    private void showAllGoods() {
        ivNoGoods.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        if (Common.networkConnected(getActivity())) {
            String url = Common.URL + "GoodsServlet";
            SharedPreferences pref = this.getActivity().getSharedPreferences(Common.PREF_FILE, Context.MODE_PRIVATE);
            String user = pref.getString("user", "");
            String ACTION = "getSelfChange";
            List<Goods> goods = null;
            try {
                goods = new GoodsGetSelfTask().execute(url, user, ACTION).get();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (goods == null || goods.isEmpty()) {
                // Common.showToast(getActivity(), R.string.msg_NoGoodsFound);
                ivNoGoods.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setVisibility(View.GONE);
            } else {
                rvGoods.setAdapter(new GoodsBoxPageChange.GoodsRecyclerViewAdapter(getActivity(), goods));

            }
        } else {
            Common.showToast(getActivity(), R.string.msg_NoNetwork);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        showAllGoods();

    }

    private class GoodsRecyclerViewAdapter extends RecyclerView.Adapter<GoodsRecyclerViewAdapter.MyViewHolder> {
        private LayoutInflater layoutInflater;
        private List<Goods> goods;

        public GoodsRecyclerViewAdapter(Context context, List<Goods> goods) {
            layoutInflater = LayoutInflater.from(context);
            this.goods = goods;
        }


        @Override
        public int getItemCount() {
            return goods.size();
        }

        @Override
        public GoodsBoxPageChange.GoodsRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.goods_recycleview_wish1, parent, false);
            return new GoodsBoxPageChange.GoodsRecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(GoodsRecyclerViewAdapter.MyViewHolder myViewHolder, int position) {
            final Goods good = goods.get(position);
            String url = Common.URL + "GoodsServlet";
            int gid = good.getGoodsNo();
            int imageSize = 250;
            new GoodsGetImageTask(myViewHolder.imageView).execute(url, gid, imageSize);

            myViewHolder.ivIsInst.setVisibility(View.GONE);
            myViewHolder.tvGoodsTitle.setText(good.getGoodsName());
//            myViewHolder.tvGoodsClass.setText("類型：" + changeType2String(good.getGoodsType()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            final String exdate = sdf.format(good.getDeadLine());
            myViewHolder.tvNeedTime.setText("到期日：" + exdate);
            myViewHolder.tvNeedNum.setText("數量：" + good.getQty());
            myViewHolder.background.setBackgroundColor(Color.rgb(68, 248, 172));

            myViewHolder.ivMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(getActivity(), view);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_goodcardview, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.goodsMenuUpdate:
                                    Intent intent = new Intent();
                                    intent.setClass(getActivity(), GoodsUpdateActivity.class);
                                    Bundle bundle = new Bundle();

                                    bundle.putSerializable("goods", good);
                                    intent.putExtra("intentGoods", bundle);
                                    startActivity(intent);
                                    break;
                                case R.id.goodsMenuDelete:
                                    if (Common.networkConnected(getActivity())) {
                                        String url = Common.URL + "GoodsServlet";
                                        String action = "goodsDelete";
                                        int count = 0;
                                        try {
                                            count = new GoodsUpdateTask().execute(url, action, good, null).get();
                                        } catch (Exception e) {
                                            Log.e(TAG, e.toString());
                                        }
                                        if (count == 0) {
                                            Common.showToast(getActivity(), R.string.msg_deleteFail);
                                        } else {
                                            Common.showToast(getActivity(), R.string.msg_deleteSuccess);
                                        }
                                    } else {
                                        Common.showToast(getActivity(), R.string.msg_NoNetwork);
                                    }
                                    break;
                            }

                            return true;
                        }

                    });

                    popupMenu.show();

                }
            });


            myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), GoodsInfoActivity.class);
                    Bundle bundle = new Bundle();

                    bundle.putSerializable("goods", good);
                    intent.putExtra("intentGoods", bundle);
                    startActivity(intent);
                }
            });

        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView, ivMenu, ivIsInst;
            TextView tvGoodsTitle, tvGoodsClass, tvNeedTime, tvNeedNum;
            LinearLayout background;

            public MyViewHolder(View itemView) {
                super(itemView);
                ivIsInst = (ImageView) itemView.findViewById(R.id.iv_isInst);
                imageView = (ImageView) itemView.findViewById(R.id.iv_image);
                tvGoodsTitle = (TextView) itemView.findViewById(R.id.tv_goodsTitle);
//                tvGoodsClass = (TextView) itemView.findViewById(R.id.tv_goodsClass);
                tvNeedTime = (TextView) itemView.findViewById(R.id.tv_needTime);
                tvNeedNum = (TextView) itemView.findViewById(R.id.tv_needNum);
                ivMenu = (ImageView) itemView.findViewById(R.id.icon_menu);
                background = (LinearLayout) itemView.findViewById(R.id.lnwish);
            }
        }
    }

    public String changeType2String(int type) {
        String gtype = "";
        switch (type) {
            case 1:
                gtype = "食品";
                break;
            case 2:
                gtype = "服飾配件";
                break;
            case 3:
                gtype = "生活用品";
                break;
            case 4:
                gtype = "家電機器";
                break;
            case 5:
                gtype = "其他";
                break;
        }
        return gtype;
    }

}
