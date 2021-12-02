package com.example.iobrokerwear;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.iobrokerwear.databinding.ActivitySettingsBinding;

public class activity_settings extends Activity {

    private TextView mTextView;
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Focus for Bezel
        LinearLayout settings_linear = findViewById(R.id.settings_linear);
        settings_linear.requestFocus();

        //Load Settings
        EditText inpSettingsHost = (EditText)findViewById(R.id.inp_settings_host);
        EditText inpSettingsPort = (EditText)findViewById(R.id.inp_settings_port);
        SharedPreferences settings = getApplicationContext().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        inpSettingsHost.setText(settings.getString("settings_host",""));
        inpSettingsPort.setText(settings.getString("settings_port", "8087"));

    }

    public void onClick_settings_save(View view) {
        EditText inpSettingsHost = (EditText)findViewById(R.id.inp_settings_host);
        EditText inpSettingsPort = (EditText)findViewById(R.id.inp_settings_port);

        SharedPreferences settings = getApplicationContext().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("settings_host", inpSettingsHost.getText().toString());
        editor.putString("settings_port", inpSettingsPort.getText().toString());
        editor.apply();
        finish();
    }
}