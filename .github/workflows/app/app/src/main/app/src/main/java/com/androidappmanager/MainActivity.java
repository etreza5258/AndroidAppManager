
package com.androidappmanager;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView status;
    private Button stopButton;

    private final String[] apps = {
        "com.openai.chatgpt",
        "ir.eitaa.messenger",
        "com.farsitel.bazaar",
        "com.google.android.apps.nbu.files",
        "com.google.android.apps.searchlite",
        "com.sec.android.app.samsungapps",
        "com.android.vending",
        "com.aparat.filimo",
        "ir.filmnet.android"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(28, 28, 28, 28);
        layout.setBackgroundColor(Color.rgb(20, 30, 42));

        TextView title = new TextView(this);
        title.setText("مدیریت برنامه‌های غیرضروری");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        status = new TextView(this);
        status.setText("آماده اجرا");
        status.setTextColor(Color.WHITE);
        status.setTextSize(16);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 35, 0, 35);
        layout.addView(status);

        stopButton = new Button(this);
        stopButton.setText("توقف همه برنامه‌های غیرضروری");
        layout.addView(stopButton);

        stopButton.setOnClickListener(v -> stopApps());

        setContentView(layout);
    }

    private void stopApps() {
        stopButton.setEnabled(false);
        status.setText("در حال توقف برنامه‌ها...");

        new Thread(() -> {
            StringBuilder result = new StringBuilder();
            int success = 0;

            try {
                for (String pkg : apps) {
                    Process process = new ProcessBuilder(
                        "su", "-c", "am force-stop " + pkg
                    ).redirectErrorStream(true).start();

                    java.io.BufferedReader reader =
                        new java.io.BufferedReader(
                            new java.io.InputStreamReader(
                                process.getInputStream()
                            )
                        );

                    while (reader.readLine() != null) {
                    }

                    int code = process.waitFor();

                    if (code == 0) {
                        success++;
                        result.append("متوقف شد: ").append(pkg).append("\n");
                    } else {
                        result.append("خطا: ").append(pkg).append("\n");
                    }
                }
            } catch (Exception e) {
                result.append("خطا: ").append(e.getMessage());
            }

            final String message =
                "تعداد توقف موفق: " + success + " از " + apps.length
                + "\n\n" + result;

            runOnUiThread(() -> {
                status.setText(message);
                stopButton.setEnabled(true);
            });
        }).start();
    }
}
