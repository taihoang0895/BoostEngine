package com.example.boostengine

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.boostengine.services.AccessibilityHelperService
import com.example.processmanager.CoreProcessManager
import com.example.processmanager.process.ProcessInfo
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val mainScope = MainScope()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /*  startActivityForResult(
              Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), 1
          )*/
        val intent = Intent(this, AccessibilityHelperService::class.java)
        startService(intent)
        findViewById<Button>(R.id.btn_show_log).setOnClickListener {
            mainScope.launch {
                if (PermissionUtil.isAccessibilityEnabled(this@MainActivity)) {
                    val listRunningProcessInfo = CoreProcessManager.getRunningProcessManager()
                    var selectedProcessInfo:ProcessInfo? = null
                    listRunningProcessInfo.forEach {
                        if(!it.pkgName.equals(this@MainActivity.packageName)){
                            selectedProcessInfo = it;
                        }
                    }
                    if(selectedProcessInfo != null){
                        CoreProcessManager.killApp(selectedProcessInfo!!)
                        delay(2000)
                        val intent = Intent(this@MainActivity, MainActivity::class.java)
                        this@MainActivity.startActivity(intent)

                    }


                } else {
                    goToAccessibilitySetting()
                }
            }
        }
    }

    private fun goToAccessibilitySetting() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
}