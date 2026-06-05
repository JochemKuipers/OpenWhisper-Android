package com.openwhisper.android.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
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

    private ActivityMainBinding binding;
    private NetworkModule network;
    private ChatsFragment chatsFragment;
    private ContactsFragment contactsFragment;

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

        if (savedInstanceState == null) {
            chatsFragment = new ChatsFragment();
            contactsFragment = new ContactsFragment();
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            tx.add(R.id.fragmentContainer, chatsFragment, TAB_CHATS);
            tx.add(R.id.fragmentContainer, contactsFragment, TAB_CONTACTS);
            tx.hide(contactsFragment);
            tx.commit();
        } else {
            chatsFragment = (ChatsFragment) getSupportFragmentManager().findFragmentByTag(TAB_CHATS);
            contactsFragment = (ContactsFragment) getSupportFragmentManager().findFragmentByTag(TAB_CONTACTS);
        }

        binding.bottomNav.setOnItemSelectedListener(this::onNavItemSelected);

        String tab = getIntent().getStringExtra(EXTRA_TAB);
        if (TAB_CONTACTS.equals(tab)) {
            binding.bottomNav.setSelectedItemId(R.id.nav_contacts);
            showContacts();
        } else {
            binding.bottomNav.setSelectedItemId(R.id.nav_chats);
            showChats();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String tab = intent.getStringExtra(EXTRA_TAB);
        if (TAB_CONTACTS.equals(tab)) {
            binding.bottomNav.setSelectedItemId(R.id.nav_contacts);
            showContacts();
        } else if (TAB_CHATS.equals(tab)) {
            binding.bottomNav.setSelectedItemId(R.id.nav_chats);
            showChats();
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
        if (item.getItemId() == R.id.nav_chats) {
            showChats();
            return true;
        }
        if (item.getItemId() == R.id.nav_contacts) {
            showContacts();
            return true;
        }
        return false;
    }

    private void showChats() {
        if (chatsFragment == null || contactsFragment == null) {
            return;
        }
        getSupportFragmentManager()
                .beginTransaction()
                .show(chatsFragment)
                .hide(contactsFragment)
                .commit();
    }

    private void showContacts() {
        if (chatsFragment == null || contactsFragment == null) {
            return;
        }
        getSupportFragmentManager()
                .beginTransaction()
                .hide(chatsFragment)
                .show(contactsFragment)
                .commit();
        contactsFragment.refresh();
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
                        new Callback<ResponseBody>() {
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
