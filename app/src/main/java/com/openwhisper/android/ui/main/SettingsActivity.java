package com.openwhisper.android.ui.main;

import android.os.Bundle;

import androidx.fragment.app.FragmentTransaction;

import com.openwhisper.android.OpenWhisperApp;
import com.openwhisper.android.R;
import com.openwhisper.android.data.NetworkModule;
import com.openwhisper.android.databinding.ActivitySettingsBinding;
import com.openwhisper.android.ui.base.BaseActivity;

/** Settings screen reachable before login (instance URL, theme). */
public class SettingsActivity extends BaseActivity implements SettingsHost {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupEdgeToEdge();
        ActivitySettingsBinding binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settingsContainer, new SettingsFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
    }

    @Override
    public NetworkModule network() {
        return ((OpenWhisperApp) getApplication()).network();
    }

    @Override
    public boolean showLogout() {
        return false;
    }

    @Override
    public void onInstanceUrlSaved() {
        ((OpenWhisperApp) getApplication()).recreateNetworkModule();
        finish();
    }
}
