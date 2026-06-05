package com.openwhisper.android.ui.main;

import com.openwhisper.android.data.NetworkModule;

public interface MainHost {

    NetworkModule network();

    void logout();
}
