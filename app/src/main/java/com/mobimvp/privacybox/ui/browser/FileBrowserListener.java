package com.mobimvp.privacybox.ui.browser;

import java.io.File;

public interface FileBrowserListener {
    public void OnStartLoadFolder(File folder);

    public void OnFinishLoadFolder(File folder);
}
