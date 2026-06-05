package com.openwhisper.android.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.openwhisper.android.BuildConfig;
import com.openwhisper.android.OpenWhisperApp;
import com.openwhisper.android.R;
import com.openwhisper.android.data.ApiConfig;
import com.openwhisper.android.data.SettingsStore;
import com.openwhisper.android.databinding.FragmentSettingsBinding;
import com.openwhisper.android.ui.login.LoginActivity;
import com.openwhisper.android.util.AppTheme;
import com.openwhisper.android.util.InstanceUrlChecker;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsHost host;
    private SettingsStore settings;
    private boolean savingInstanceUrl;

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (!(context instanceof SettingsHost)) {
            throw new IllegalStateException("Host must implement SettingsHost");
        }
        host = (SettingsHost) context;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        settings = ((OpenWhisperApp) requireActivity().getApplication()).settings();
        bindThemeControls();
        bindInstanceUrl();
        binding.logoutButton.setVisibility(host.showLogout() ? View.VISIBLE : View.GONE);
        if (host.showLogout()) {
            binding.logoutButton.setOnClickListener(v -> ((MainHost) requireActivity()).logout());
        }
    }

    private void bindThemeControls() {
        int themeButtonId =
                switch (settings.getThemeMode()) {
                    case SettingsStore.THEME_LIGHT -> R.id.themeLight;
                    case SettingsStore.THEME_DARK -> R.id.themeDark;
                    default -> R.id.themeSystem;
                };
        binding.themeGroup.check(themeButtonId);

        binding.themeGroup.addOnButtonCheckedListener(
                (group, checkedId, isChecked) -> applyThemeSelection(checkedId, isChecked));

        if (AppTheme.isDynamicColorAvailable()) {
            binding.dynamicColorsRow.setVisibility(View.VISIBLE);
            binding.dynamicColorsSwitch.setChecked(settings.isDynamicColorsEnabled());
            binding.dynamicColorsSwitch.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> {
                        if (isChecked == settings.isDynamicColorsEnabled()) {
                            return;
                        }
                        settings.setDynamicColorsEnabled(isChecked);
                        applyThemeAndRecreate();
                    });
        } else {
            binding.dynamicColorsRow.setVisibility(View.GONE);
        }
    }

    private void applyThemeSelection(int checkedId, boolean isChecked) {
        if (!isChecked) {
            return;
        }
        String mode =
                checkedId == R.id.themeLight
                        ? SettingsStore.THEME_LIGHT
                        : checkedId == R.id.themeDark ? SettingsStore.THEME_DARK : SettingsStore.THEME_SYSTEM;
        if (mode.equals(settings.getThemeMode())) {
            return;
        }
        settings.setThemeMode(mode);
        applyThemeAndRecreate();
    }

    private void applyThemeAndRecreate() {
        OpenWhisperApp app = (OpenWhisperApp) requireActivity().getApplication();
        app.applyThemeSettings();
        requireActivity().recreate();
    }

    private void bindInstanceUrl() {
        binding.instanceUrlInput.setText(settings.getCustomInstanceUrl());
        binding.instanceUrlHelper.setText(getString(R.string.instance_url_default, BuildConfig.API_BASE_URL));

        binding.saveInstanceUrlButton.setOnClickListener(v -> saveInstanceUrl());
    }

    private void saveInstanceUrl() {
        if (savingInstanceUrl) {
            return;
        }

        String input =
                binding.instanceUrlInput.getText() != null
                        ? binding.instanceUrlInput.getText().toString().trim()
                        : "";
        String previous = settings.getCustomInstanceUrl();

        final String normalizedForStorage;
        try {
            if (input.isEmpty()) {
                normalizedForStorage = "";
            } else {
                normalizedForStorage = ApiConfig.normalizeCustomUrl(input);
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(requireContext(), R.string.instance_url_invalid, Toast.LENGTH_LONG).show();
            return;
        }

        if (normalizedForStorage.equals(previous)) {
            Toast.makeText(requireContext(), R.string.instance_url_saved, Toast.LENGTH_SHORT).show();
            return;
        }

        if (normalizedForStorage.isEmpty()) {
            persistInstanceUrl(normalizedForStorage);
            return;
        }

        setInstanceUrlBusy(true);
        InstanceUrlChecker.verifyReachable(
                normalizedForStorage,
                new InstanceUrlChecker.Listener() {
                    @Override
                    public void onReachable() {
                        if (!isAdded() || binding == null) {
                            return;
                        }
                        persistInstanceUrl(normalizedForStorage);
                    }

                    @Override
                    public void onFailure() {
                        if (!isAdded() || binding == null) {
                            return;
                        }
                        setInstanceUrlBusy(false);
                        Toast.makeText(requireContext(), R.string.instance_url_unreachable, Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private void persistInstanceUrl(String newStored) {
        settings.setCustomInstanceUrl(newStored);
        binding.instanceUrlInput.setText(newStored);
        setInstanceUrlBusy(false);

        OpenWhisperApp app = (OpenWhisperApp) requireActivity().getApplication();
        if (host.showLogout() && host.network().tokenStore().hasAccess()) {
            app.socialWebSocket().stop();
            host.network().tokenStore().clear();
            com.openwhisper.android.data.UserSession.clear();
            app.recreateNetworkModule();
            Toast.makeText(requireContext(), R.string.instance_url_saved_relogin, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
            return;
        }

        host.onInstanceUrlSaved();
        Toast.makeText(requireContext(), R.string.instance_url_saved, Toast.LENGTH_SHORT).show();
    }

    private void setInstanceUrlBusy(boolean busy) {
        savingInstanceUrl = busy;
        binding.saveInstanceUrlButton.setEnabled(!busy);
        binding.instanceUrlInput.setEnabled(!busy);
        binding.saveInstanceUrlButton.setText(busy ? R.string.instance_url_checking : R.string.save);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
