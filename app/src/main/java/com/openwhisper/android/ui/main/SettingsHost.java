package com.openwhisper.android.ui.main;

import com.openwhisper.android.data.NetworkModule;

/** Host for {@link SettingsFragment} from main or pre-login settings screen. */
public interface SettingsHost {

    NetworkModule network();

    /** Whether the log out control should be shown (hidden before login). */
    boolean showLogout();

    /** Called after instance URL was validated and persisted. */
    void onInstanceUrlSaved();
}
