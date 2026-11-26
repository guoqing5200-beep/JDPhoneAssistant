
package com.jd.phoneassistant;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;
import android.content.Intent;
import android.net.Uri;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    EditText productUrl, saleTime;
    TextView countdown, status;
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable timer;
    long targetTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 35, 28, 25);
        root.setBackgroundColor(Color.rgb(248,248,248));

        TextView title = new TextView(this);
        title.setText("京东抢手机助手 3.0");
        title.setTextSize(25);
        title.setTextColor(Color.rgb(190,30,30));
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView tip = new TextView(this);
        tip.setText("商品监控 · 开售倒计时 · 购买辅助");
        tip.setTextSize(14);
        tip.setGravity(Gravity.CENTER);
        tip.setPadding(0,15,0,25);
        root.addView(tip);

        productUrl = new EditText(this);
        productUrl.setSingleLine(true);
        productUrl.setHint("粘贴京东商品链接");
        root.addView(productUrl);

        saleTime = new EditText(this);
        saleTime.setSingleLine(true);
        saleTime.setHint("开售时间 例如 2026-12-01 10:00:00");
        root.addView(saleTime);

        countdown = new TextView(this);
        countdown.setText("00天 00:00:00");
        countdown.setTextSize(27);
        countdown.setTextColor(Color.rgb(200,30,30));
        countdown.setGravity(Gravity.CENTER);
        countdown.setPadding(0,35,0,25);
        root.addView(countdown);

        Button start = new Button(this);
        start.setText("开始倒计时");
        root.addView(start);

        Button open = new Button(this);
        open.setText("打开京东商品页面");
        root.addView(open);

        Button stop = new Button(this);
        stop.setText("停止倒计时");
        root.addView(stop);

        status = new TextView(this);
        status.setText("等待设置商品和开售时间");
        status.setTextSize(14);
        status.setPadding(0,25,0,0);
        root.addView(status);

        TextView notice = new TextView(this);
        notice.setText("提示：请提前登录京东并确认收货地址。验证码、风控及付款由本人处理。");
        notice.setTextSize(13);
        notice.setPadding(0,20,0,0);
        root.addView(notice);

        setContentView(root);

        start.setOnClickListener(v -> startTimer());

        stop.setOnClickListener(v -> {
            handler.removeCallbacksAndMessages(null);
            countdown.setText("已停止");
            status.setText("倒计时已停止");
        });

        open.setOnClickListener(v -> openProduct());
    }

    private void startTimer() {
        try {
            String value = saleTime.getText().toString().trim();
            SimpleDateFormat format =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            format.setLenient(false);
            Date date = format.parse(value);

            if (date == null || date.getTime() <= System.currentTimeMillis()) {
                status.setText("请输入未来的开售时间");
                return;
            }

            targetTime = date.getTime();
            handler.removeCallbacksAndMessages(null);

            timer = new Runnable() {
                @Override
                public void run() {
                    long left = targetTime - System.currentTimeMillis();

                    if (left <= 0) {
                        countdown.setText("开售时间已到！");
                        status.setText("请尽快进入京东商品页面");
                        return;
                    }

                    long seconds = left / 1000;
                    long days = seconds / 86400;
                    long hours = (seconds % 86400) / 3600;
                    long minutes = (seconds % 3600) / 60;
                    long secs = seconds % 60;

                    countdown.setText(String.format(
                        Locale.CHINA,
                        "%d天 %02d:%02d:%02d",
                        days, hours, minutes, secs
                    ));

                    handler.postDelayed(this, 1000);
                }
            };

            status.setText("倒计时运行中");
            handler.post(timer);

        } catch (Exception e) {
            status.setText("时间格式错误，请使用 yyyy-MM-dd HH:mm:ss");
        }
    }

    private void openProduct() {
        String url = productUrl.getText().toString().trim();

        if (url.isEmpty()) {
            status.setText("请先输入京东商品链接");
            return;
        }

        Uri uri = Uri.parse(url);
        String host = uri.getHost();

        if (!"http".equalsIgnoreCase(uri.getScheme())
                && !"https".equalsIgnoreCase(uri.getScheme())) {
            status.setText("请输入有效的网页链接");
            return;
        }

        if (host == null ||
                !(host.equalsIgnoreCase("jd.com")
                || host.endsWith(".jd.com")
                || host.equalsIgnoreCase("3.cn"))) {
            status.setText("请输入京东官方商品链接");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
