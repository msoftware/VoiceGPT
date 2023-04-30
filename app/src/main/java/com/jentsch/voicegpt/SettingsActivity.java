package com.jentsch.voicegpt;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jentsch.voicegpt.db.DbSingleton;

public class SettingsActivity extends AppCompatActivity {

    public static final String OPENAI_API_KEY = "OPENAI_API_KEY";
    public static final String MAX_TOKENS = "MAX_TOKENS";
    public static final String ELEVENLABS_ENABLE = "ELEVENLABS_ENABLE";
    public static final String ELEVENLABS_API_KEY = "ELEVENLABS_API_KEY";

    SharedPreferences prefs;

    EditText inputOpenaiApiKey;
    EditText inputOpenaiMaxTokens;
    Switch elevenlabsEnableSwitch;
    EditText elevenlabsApiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);

        inputOpenaiApiKey = findViewById(R.id.input_openai_api_key);
        inputOpenaiMaxTokens = findViewById(R.id.input_openai_max_tokens);
        inputOpenaiApiKey.setText(prefs.getString(OPENAI_API_KEY, ""));
        inputOpenaiMaxTokens.setText(""+ prefs.getInt(MAX_TOKENS, 500));
        elevenlabsEnableSwitch = findViewById(R.id.switch_elevenlabs_enable);
        elevenlabsApiKey = findViewById(R.id.input_elevenlabs_api_key);
        if (prefs.getInt(ELEVENLABS_ENABLE, 0) == 1) {
            elevenlabsEnableSwitch.setChecked(true);
        } else {
            elevenlabsEnableSwitch.setChecked(false);
        }
        elevenlabsApiKey.setText(prefs.getString(ELEVENLABS_API_KEY, ""));

        findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String apiKey = inputOpenaiApiKey.getText().toString();
                String maxTokens = inputOpenaiMaxTokens.getText().toString();

                // Validation
                if (apiKey.length() < 20) {
                    Toast.makeText(SettingsActivity.this, "Invalid OpenAI API Key", Toast.LENGTH_LONG).show();
                    return;
                }
                int maxTokensInt = 500;
                try {
                    maxTokensInt = Integer.parseInt(maxTokens);
                    if (maxTokensInt < 100) {
                        Toast.makeText(SettingsActivity.this, "Invalid 'Max. Tokens' value. Min. 100", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (maxTokensInt > 10000) {
                        Toast.makeText(SettingsActivity.this, "Invalid 'Max. Tokens' value. Max. 10000", Toast.LENGTH_LONG).show();
                        return;
                    }
                } catch (Exception e) {
                    Toast.makeText(SettingsActivity.this, "Invalid 'Max. Tokens' value " + e.toString(), Toast.LENGTH_LONG).show();
                    return;
                }

                if (elevenlabsEnableSwitch.isChecked()) {
                    if (elevenlabsApiKey.getText().toString().length() < 10) {
                        Toast.makeText(SettingsActivity.this, "ElevenLabs API Key is needed is enabled.", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        prefs.edit().putString(ELEVENLABS_API_KEY, elevenlabsApiKey.getText().toString()).commit();
                    }

                }

                if (elevenlabsEnableSwitch.isChecked()) {
                    prefs.edit().putInt(ELEVENLABS_ENABLE, 1).commit();
                } else {
                    prefs.edit().putInt(ELEVENLABS_ENABLE, 0).commit();
                }

                // Save
                prefs.edit().putString(OPENAI_API_KEY, apiKey).commit();
                prefs.edit().putInt(MAX_TOKENS, maxTokensInt).commit();
                finish();
            }
        });

        findViewById(R.id.delete_history_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DbSingleton.instance(SettingsActivity.this).getAppDatabase().chatMessageDao().deleteAll();
                finish();
            }
        });
    }
}