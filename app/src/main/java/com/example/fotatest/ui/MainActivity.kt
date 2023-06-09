package com.example.fotatest.ui

import android.R.attr.process
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fotatest.R
import com.example.fotatest.databinding.ActivityMainBinding
import com.example.fotatest.utils.PermissionsHelper
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val managePermissions by lazy {
        val listPermission = listOf<String>()
        PermissionsHelper(this, listPermission, REQUEST_PERMISSION)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        setEvent()
    }

    private fun needToRequestPermission() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            !packageManager.canRequestPackageInstalls() &&
            !Environment.isExternalStorageManager()

    private fun setEvent() {
        binding.btnInstall.setOnClickListener {
            if (needToRequestPermission()) {
                requestPermissionPackage()
            } else {
                installPackage()
            }
        }
    }

    private fun requestPermissionPackage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !packageManager.canRequestPackageInstalls()) {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, REQUEST_INSTALL_PACKAGES)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_INSTALL_PACKAGES) {
            if (resultCode == RESULT_OK) {
                managePermissions.requestPermissionManageAllApp(this@MainActivity)
            } else {
                /** to be implement action failed **/
            }
        }

        if (requestCode == INSTALL_PACKAGE_DONE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this@MainActivity, "install success ${resultCode}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "install failed ${data?.dataString}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun installPackage() {
        val externalStoragePath: String = Environment.getExternalStorageDirectory().absolutePath
        val packageFile = File("$externalStoragePath$ADDITIONAL_PATH")
//
//        val apkUri = FileProvider.getUriForFile(this, AUTHORITY_FILE_PROVIDER , packageFile)
//        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
//            .setData(apkUri)
//            .putExtra(Intent.EXTRA_RETURN_RESULT, true)
//            .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        startActivityForResult(intent, INSTALL_PACKAGE_DONE)

        val process  = Runtime.getRuntime().exec("pm install -r ${packageFile.absolutePath}")

        val result: Int = process.waitFor()
        if (result == 0) {
            Toast.makeText(this@MainActivity, "Install success", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@MainActivity, "Install failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val INSTALL_PACKAGE_DONE = 123
        const val REQUEST_INSTALL_PACKAGES = 111
        const val REQUEST_PERMISSION = 112
        const val ADDITIONAL_PATH = "/apk_install/lindur.apk"
        const val AUTHORITY_FILE_PROVIDER = "com.example.fotatest.fileprovider"
    }
}