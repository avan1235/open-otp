package ml.dev.kotlin.openotp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidOpenOtpApp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidOpenOtpApp()
    }
}