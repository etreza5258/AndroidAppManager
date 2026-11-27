
package com.androidappmanager;

import android.app.Activity;
import android.os.Bundle;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private TextView status;
    private Button stopButton;

    private static final String[] EXCLUDED = {
        "com.agah.asatrader",
        "com.precision.loopclicker",
        "com.androidappmanager"
    };

    private static final String[] EXTRA_APPS = {
        "com.termux",
        "com.topjohnwu.magisk"
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

    private boolean excluded(String pkg) {
        for (String item : EXCLUDED) {
            if (item.equals(pkg)) return true;
        }
        return false;
    }

    private void stopApps() {
        stopButton.setEnabled(false);
        status.setText("در حال شناسایی و توقف برنامه‌ها...");

        new Thread(() -> {
            List<String> apps = new ArrayList<>();
            PackageManager pm = getPackageManager();

            try {
                List<PackageInfo> installed =
                    pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);

                for (PackageInfo info : installed) {
                    String pkg = info.packageName;

                    if (excluded(pkg)) continue;

                    if ((info.applicationInfo.flags &
                        ApplicationInfo.FLAG_SYSTEM) != 0) continue;

                    boolean hasInternet = false;

                    if (info.requestedPermissions != null) {
                        for (String permission : info.requestedPermissions) {
                            if ("android.permission.INTERNET".equals(permission)) {
                                hasInternet = true;
                                break;
                            }
                        }
                    }

                    if (hasInternet) apps.add(pkg);
                }

                for (String pkg : EXTRA_APPS) {
                    if (!excluded(pkg) && !apps.contains(pkg)) {
                        apps.add(pkg);
                    }
                }

                int success = 0;
                StringBuilder result = new StringBuilder();

                for (String pkg : apps) {
                    try {
                        Process process = new ProcessBuilder(
                            "su", "-c", "am force-stop " + pkg
                        ).redirectErrorStream(true).start();

                        java.io.BufferedReader reader =
                            new java.io.BufferedReader(
                                new java.io.InputStreamReader(
                                    process.getInputStream()
                                )
                            );

                        while (reader.readLine() != null) {}

                        int code = process.waitFor();

                        if (code == 0) {
                            success++;
                            result.append("متوقف شد: ")
                                .append(pkg).append("\n");
                        } else {
                            result.append("خطا: ")
                                .append(pkg).append("\n");
                        }
                    } catch (Exception e) {
                        result.append("خطا: ")
                            .append(pkg).append("\n");
                    }
                }

                final String message =
                    "تعداد توقف موفق: " + success + " از " +
                    apps.size() + "\n\n" + result;

                runOnUiThread(() -> {
                    status.setText(message);
                    stopButton.setEnabled(true);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    status.setText("خطا: " + e.getMessage());
                    stopButton.setEnabled(true);
                });
            }
        }).start();
    }
}
