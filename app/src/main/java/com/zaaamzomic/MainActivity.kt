package com.zaaamzomic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.zaaamzomic.navigation.ZomicNav
import com.zaaamzomic.ui.theme.ZomicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as ZomicApp).container
        setContent {
            ZomicTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ZomicNav(container)
                }
            }
        }
    }
}
