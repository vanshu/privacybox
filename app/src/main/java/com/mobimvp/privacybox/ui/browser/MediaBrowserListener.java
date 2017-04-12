package com.mobimvp.privacybox.ui.browser;

import java.io.File;

public interface MediaBrowserListener {
    public void OnBrowse(MediaBrowserAdapter.BrowseMode mode, File folder);

    public void OnScanFinished(boolean stopped);
}
