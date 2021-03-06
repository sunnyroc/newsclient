package menu;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.sunpeng.newsclient.R;

import java.util.ArrayList;
import java.util.List;

import adapter.NewItemAdapter;
import bean.NewCenter;
import bean.NewItemData;
import home.BasePage;
import utils.CommonUtil;
import utils.DensityUtil;
import utils.GsonTools;
import utils.HMAPI;
import utils.SharePrefencesUtils;
import view.RollViewPager;

/**
 * Created by FBI on 2015/12/24.
 */
public class NewItemPage extends BasePage {
    private String url;

    @ViewInject(R.id.lv_item_news)
    private ListView lv_item_news;
    //存放轮播图
    @ViewInject(R.id.top_news_viewpager)
    private LinearLayout top_news_viewpager;
    //热门新闻的标题
    @ViewInject(R.id.top_news_title)
    private TextView top_news_title;
    //点的线性布局
    @ViewInject(R.id.dots_ll)
    private LinearLayout dots_ll;
    private View topView;
    private RollViewPager rollViewPager;

    public NewItemPage(Context context,String url) {
        super(context);
        this.url=url;
    }

    @Override
    public View initView() {
        View view = View.inflate(mContext, R.layout.frag_item_news, null);
        topView = View.inflate(mContext, R.layout.layout_roll_view, null);
        ViewUtils.inject(this,view);
        ViewUtils.inject(this, topView);
//        TextView textView = new TextView(mContext);
//        textView.setText("cccc");
//        textView.setTextColor(Color.BLACK);
        //给ListView设置条目点击事件，设置一个标志记录ListView条目是否被点击过
        lv_item_news.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NewItemData.News item = (NewItemData.News) lv_item_news.getItemAtPosition(position);
                //判断当前的条目是否读过
                if (!item.isRead){
                    item.isRead=true;
                }
                mNewItemAdapter.notifyDataSetChanged();

            }
        });

        return view;
    }

    @Override
    public void initData() {
        String result = SharePrefencesUtils.getString(mContext, HMAPI.BASE_URL + url, "");
        if (!TextUtils.isEmpty(result)){
            progressData(result);
        }
        getNewItemData();

    }

    private void getNewItemData() {
        HttpUtils httpUtils = new HttpUtils();
        httpUtils.send(HttpRequest.HttpMethod.GET, HMAPI.BASE_URL + url, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                //// TODO: 2015/12/25 打印jason
//                System.out.println(">>>"+responseInfo.result+"<<");
//                System.out.println(">>>url<<<<"+url);
                //缓存新闻数据
                SharePrefencesUtils.saveString(mContext,HMAPI.BASE_URL + url,responseInfo.result);
                progressData(responseInfo.result);
            }

            @Override
            public void onFailure(HttpException e, String s) {

            }
        });
    }
    private List<NewItemData.News> mNews = new ArrayList<NewItemData.News>();
    private NewItemAdapter mNewItemAdapter;
//    private List<NewItemData.Topic> mTopic = new ArrayList<NewItemData.Topic>();
//    private List<NewItemData.Topnews> mTopNews = new ArrayList<NewItemData.Topnews>();

    private void progressData(String result) {
        NewItemData newItemData = GsonTools.changeGsonToBean(result, NewItemData.class);
        if (null!= newItemData){
            if (newItemData.retcode==200){
                if (null!=rollViewPager){
                    rollViewPager.stop();
                }
                mNews.clear();
                isLoading=true;
                //获取热门新闻
                List<NewItemData.Topnews> topnews = newItemData.data.topnews;
                //轮播图的标题
                List<String> mTitles = new ArrayList<String>();
                //轮播图的背景图片
                List<String> mImageLists = new ArrayList<String>();
                //初始化轮播图的点
                initDot(topnews.size());
                //遍历所有的热门新闻
                for (NewItemData.Topnews topNew:newItemData.data.topnews){
                    mTitles.add(topNew.title);
                    mImageLists.add(topNew.topimage);
                }

                //遍历所有的新闻
                for (NewItemData.News news:newItemData.data.news){
                   mNews.add(news);
               }

                //设置自动跳动
                rollViewPager = new RollViewPager(mContext,mDots, new RollViewPager.ViewPagerOnTouchListener() {
                    @Override
                    public void onViewPagerClickListener() {
                        Toast.makeText(mContext,"卧槽谁点我",Toast.LENGTH_SHORT).show();
                    }
                });
                //设置标题数据
                rollViewPager.setTextTitle(top_news_title, mTitles);
                //设置背景图片
                rollViewPager.setImageRes(mImageLists);
                //开始滚动
                rollViewPager.start();
                //添加到轮播图
                top_news_viewpager.removeAllViews();
                top_news_viewpager.addView(rollViewPager);
                //判断ListView是否有头部，如果有的话就不添加如果没有就添加，但是这里轮播图的点会出现两次
                if (lv_item_news.getHeaderViewsCount()<1){

                    lv_item_news.addHeaderView(topView);
                }


                if (mNewItemAdapter==null){
                    mNewItemAdapter = new NewItemAdapter(mNews,mContext);
                    lv_item_news.setAdapter(mNewItemAdapter);
                }else{
                    mNewItemAdapter.notifyDataSetChanged();
                }


            }

        }
    }

    private List<View> mDots = new ArrayList<View>();
    //初始化点
    private void initDot(int size) {

        dots_ll.removeAllViews();
        mDots.clear();
        for (int i=0;i<size;i++){
            View view = new View(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(CommonUtil.dip2px(mContext,5),
                    CommonUtil.dip2px(mContext, 5));
            params.leftMargin = CommonUtil.dip2px(mContext,5);
            if (i==0){
                view.setBackgroundResource(R.drawable.dot_focus);
            }else{
                view.setBackgroundResource(R.drawable.dot_normal);
            }
            view.setLayoutParams(params);
            dots_ll.addView(view);
            mDots.add(view);
        }
    }


}
