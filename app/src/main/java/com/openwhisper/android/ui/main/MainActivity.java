package com.openwhisper.android.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.openwhisper.android.OpenWhisperApp;
import com.openwhisper.android.R;
import com.openwhisper.android.data.NetworkModule;
import com.openwhisper.android.data.UserSession;
import com.openwhisper.android.databinding.ActivityMainBinding;
import com.openwhisper.android.ui.base.BaseActivity;
import com.openwhisper.android.ui.login.LoginActivity;
import com.openwhisper.android.ui.rooms.ChatsFragment;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements MainHost {

    public static final String EXTRA_CHAT_ID = "chat_id";
    public static final String EXTRA_CHAT_TITLE = "chat_title";
    public static final String EXTRA_TAB = "tab";

    public static final String TAB_CHATS = "chats";
    public static final String TAB_CONTACTS = "contacts";
    public static final String TAB_SETTINGS = "settings";

    private static final String STATE_SELECTED_TAB = "selected_tab";

    private ActivityMainBinding binding;
    private NetworkModule network;
    private ChatsFragment chatsFragment;
    private ContactsFragment contactsFragment;
    private SettingsFragment settingsFragment;
    private String currentTab = TAB_CHATS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupEdgeToEdge();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyBottomNavInsets(binding.bottomNav);

        network = ((OpenWhisperApp) getApplication()).network();
        if (!network.tokenStore().hasAccess()) {
            navigateLogin();
            return;
        }

        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getString(STATE_SELECTED_TAB, TAB_CHATS);
        }

        if (savedInstanceState == null) {
            chatsFragment = new ChatsFragment();
            contactsFragment = new ContactsFragment();
            settingsFragment = new SettingsFragment();
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            tx.add(R.id.fragmentContainer, chatsFragment, TAB_CHATS);
            tx.add(R.id.fragmentContainer, contactsFragment, TAB_CONTACTS);
            tx.add(R.id.fragmentContainer, settingsFragment, TAB_SETTINGS);
            tx.hide(contactsFragment);
            tx.hide(settingsFragment);
            tx.commit();
        } else {
            chatsFragment = (ChatsFragment) getSupportFragmentManager().findFragmentByTag(TAB_CHATS);
            contactsFragment = (ContactsFragment) getSupportFragmentManager().findFragmentByTag(TAB_CONTACTS);
            settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(TAB_SETTINGS);
        }

        binding.bottomNav.setOnItemSelectedListener(this::onNavItemSelected);

        String tab =
                savedInstanceState != null
                        ? currentTab
                        : getIntent().getStringExtra(EXTRA_TAB) != null
                                ? getIntent().getStringExtra(EXTRA_TAB)
                                : TAB_CHATS;
        selectTab(tab);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_SELECTED_TAB, currentTab);
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String tab = intent.getStringExtra(EXTRA_TAB);
        if (tab != null) {
            selectTab(tab);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!network.tokenStore().hasAccess()) {
            navigateLogin();
        }
    }

    private boolean onNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_chats) {
            currentTab = TAB_CHATS;
            showChats();
            return true;
        }
        if (id == R.id.nav_contacts) {
            currentTab = TAB_CONTACTS;
            showContacts();
            return true;
        }
        if (id == R.id.nav_settings) {
            currentTab = TAB_SETTINGS;
            showSettings();
            return true;
        }
        return false;
    }

    private void selectTab(@NonNull String tab) {
        currentTab = tab;
        switch (tab) {
            case TAB_CONTACTS -> {
                binding.bottomNav.setSelectedItemId(R.id.nav_contacts);
                showContacts();
            }
            case TAB_SETTINGS -> {
                binding.bottomNav.setSelectedItemId(R.id.nav_settings);
                showSettings();
            }
            default -> {
                binding.bottomNav.setSelectedItemId(R.id.nav_chats);
                showChats();
            }
        }
    }

    private void showChats() {
        showOnly(chatsFragment);
    }

    private void showContacts() {
        showOnly(contactsFragment);
        if (contactsFragment != null) {
            contactsFragment.refresh();
        }
    }

    private void showSettings() {
        showOnly(settingsFragment);
    }

    private void showOnly(Fragment target) {
        if (target == null) {
            return;
        }
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        if (chatsFragment != null && chatsFragment != target) {
            tx.hide(chatsFragment);
        }
        if (contactsFragment != null && contactsFragment != target) {
            tx.hide(contactsFragment);
        }
        if (settingsFragment != null && settingsFragment != target) {
            tx.hide(settingsFragment);
        }
        tx.show(target).commit();
    }

    @Override
    public NetworkModule network() {
        return network;
    }

    @Override
    public void logout() {
        network.api()
                .logout()
                .enqueue(
                        new Callback<>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                finishLogout();
                            }

                            @Override
                            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                finishLogout();
                            }
                        });
    }

    private void finishLogout() {
        network.tokenStore().clear();
        UserSession.clear();
        navigateLogin();
    }

    private void navigateLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
