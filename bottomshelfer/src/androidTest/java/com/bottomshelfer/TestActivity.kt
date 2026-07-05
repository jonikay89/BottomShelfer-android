package com.bottomshelfer

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity

class TestActivity : ComponentActivity() {

    val container: FrameLayout by lazy { findViewById<FrameLayout>(android.R.id.content) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(this).apply {
            id = android.R.id.content
        })
    }
}
