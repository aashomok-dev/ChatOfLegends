package com.ashomok.chatoflegends.activities.about;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ashomok.chatoflegends.BuildConfig;
import com.ashomok.chatoflegends.R;
import com.ashomok.chatoflegends.activities.main.messaging.update_to_premium.UpdateToPremiumActivity;
import com.ashomok.chatoflegends.activities.settings.Settings;
import com.ashomok.chatoflegends.utils.LogHelper;

/**
 * Created by Iuliia on 30.08.2015.
 */
public class AboutActivity extends AppCompatActivity {

    private static final String TAG = LogHelper.makeLogTag(AboutActivity.class);
    private TextView mTextView_email1;
    private TextView mTextView_email2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.about_layout);

            initToolbar();

            //base app data
            TextView mTextView_appName = findViewById(R.id.appName);
            mTextView_appName.setText(R.string.app_name);

            TextView mTextView_developer = findViewById(R.id.developer);
            mTextView_developer.setText(R.string.author);

            mTextView_email1 = findViewById(R.id.email);
            mTextView_email1.setText(
                    Html.fromHtml("<u>" + getString(R.string.my_email) + "</u>"));
            mTextView_email1.setOnClickListener(
                    view -> copyTextToClipboard(mTextView_email1.getText()));

            mTextView_email2 = findViewById(R.id.email2);
            mTextView_email2.setText(
                    Html.fromHtml("<u>" + getString(R.string.my_email) + "</u>"));
            mTextView_email2.setOnClickListener(
                    view -> copyTextToClipboard(mTextView_email2.getText()));

            TextView mTextView_version = findViewById(R.id.version);
            String version = getString(R.string.version) + " " + BuildConfig.VERSION_NAME;
            mTextView_version.setText(version);

            TextView privacy_policy_link = findViewById(R.id.privacy_policy_link);
            privacy_policy_link.setText(
                    Html.fromHtml("<u>" + getString(R.string.privacy_policy_agreement) + "</u>"));

            privacy_policy_link.setOnClickListener(view -> openPrivacyPolicy());

            if (!Settings.isPremium) {
                View freeVersionCard = findViewById(R.id.free_version_explanation_card);
                freeVersionCard.setVisibility(View.VISIBLE);
                //please buy ads-free version
                TextView mTextView_buy_paid = findViewById(R.id.buy_paid);
                mTextView_buy_paid.setText(
                        Html.fromHtml("<u>" + getString(R.string.buy_paid) + "</u>"));

                mTextView_buy_paid.setOnClickListener(view -> startUpdateToPremiumActivity());
            }

        } catch (Exception e) {
            LogHelper.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    private void openPrivacyPolicy() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Settings.PRIVACY_POLICY_LINK)));
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void copyTextToClipboard(CharSequence text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.my_email), text);
        if (null != clipboard) {
            clipboard.setPrimaryClip(clip);
        }
        Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 300 milliseconds
        if (null != v) {
            v.vibrate(300);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startUpdateToPremiumActivity() {
        Intent intent = new Intent(this, UpdateToPremiumActivity.class);
        startActivity(intent);
    }
}
