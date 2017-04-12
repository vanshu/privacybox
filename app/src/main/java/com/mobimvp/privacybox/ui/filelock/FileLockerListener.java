package com.mobimvp.privacybox.ui.filelock;

public interface FileLockerListener {
    public void onEnterEditMode(int position);

    public void OnPreviewItem(int position);

    public void OnRefreshUI();
}
