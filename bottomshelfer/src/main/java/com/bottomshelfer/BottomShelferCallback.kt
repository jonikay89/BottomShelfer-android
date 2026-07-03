package com.bottomshelfer

interface BottomShelferCallback {
    fun onDismiss() {}
    fun onGrabberDragBegan() {}
    fun onGrabberDragEnded() {}
    fun onContentDragBegan() {}
    fun onContentDragEnded() {}
    fun onDetentChanged(index: Int, height: Int) {}
}
