package home;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

/**
 * Created by FBI on 2015/12/21.
 */
public class SmartServicePage extends BasePage {
    public SmartServicePage(Context context) {

        super(context);
    }

    @Override
    public View initView() {
        TextView textView = new TextView(mContext);
        textView.setText("SmartServicePage");
        textView.setTextColor(Color.BLACK);

        return textView;
    }

    @Override
    public void initData() {

    }
}
