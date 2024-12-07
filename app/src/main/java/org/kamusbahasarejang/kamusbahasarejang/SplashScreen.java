package org.kamusbahasarejang.kamusbahasarejang;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SplashScreen extends AppCompatActivity {

    private static final String SERVER_URL = "https://kamusrejang.glitch.me"; // Your server URL
    private static final int RETRY_INTERVAL = 5000; // 5 seconds between retries
    private static final int MAX_RETRIES = 10; // Max retries before giving up

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide ActionBar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash_screen);

        // Start checking server status
        checkServerStatus(0); // Start with retry count 0
    }

    private void proceedToHome() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();
            }
        }, 3000L); // Wait for 3 seconds before moving to HomeActivity
    }

    private void checkServerStatus(final int retryCount) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isUp = false;
                try {
                    HttpURLConnection urlConnection = (HttpURLConnection) new URL(SERVER_URL).openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(5000); // 5 seconds timeout
                    urlConnection.setReadTimeout(5000); // 5 seconds read timeout
                    urlConnection.connect();

                    int responseCode = urlConnection.getResponseCode();
                    isUp = (responseCode == HttpURLConnection.HTTP_OK); // Check if the response is 200
                } catch (IOException e) {
                    e.printStackTrace();
                }

                final boolean finalIsUp = isUp;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalIsUp) {
                            // If server is up, proceed to HomeActivity
                            proceedToHome();
                        } else if (retryCount < MAX_RETRIES) {
                            // If server is down and retry count is less than MAX_RETRIES
                            Toast.makeText(SplashScreen.this, "Server down, retrying...", Toast.LENGTH_SHORT).show();
                            // Retry after a delay
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    checkServerStatus(retryCount + 1); // Retry
                                }
                            }, RETRY_INTERVAL);
                        } else {
                            // If max retries reached
                            Toast.makeText(SplashScreen.this, "Server is still down. Please try again later.", Toast.LENGTH_LONG).show();
                            proceedToHome(); // Proceed even if server is down
                        }
                    }
                });
            }
        }).start();
    }
}
