package com.example.appfortester

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
//import com.example.appfortester.broadcasts.DownloadCompleteReceiver
import com.example.appfortester.databinding.ActivityMainBinding
import com.example.appfortester.utils.Extensions.checkSelfPermissionCompat
import com.example.appfortester.utils.Extensions.requestPermissionsCompat
import com.google.android.material.snackbar.Snackbar
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter


class MainActivity : MvpAppCompatActivity(), MainView {

    @InjectPresenter
    lateinit var mainPresenter: MainPresenter
    private val downloader = Downloader(this)
    private val reqCode = 200
//    private lateinit var downloadCompleteReceiver: DownloadCompleteReceiver
    private val unknownSourceResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            startActivity(intent)
        }
    }
    private var isDownloaded = false

    @ProvidePresenter
    fun provideMainPresenter(): MainPresenter{
        return MainPresenter(downloader = downloader, context = this)
    }

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        installUnknownSourcePermission()
//        downloadCompleteReceiver = DownloadCompleteReceiver()
//        registerReceiver(downloadCompleteReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        binding.btnInstallIntent.setOnClickListener {
            Log.d("info", "click - $isDownloaded")
            if (checkPermissionVersionTwo()){
                if (isDownloaded){
                    mainPresenter.installFileViaIntent()
                } else {
                    mainPresenter.downloadFile()
                }
            } else {
                this.requestPermissionsCompat(arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.REQUEST_INSTALL_PACKAGES), reqCode)
            }
        }

        binding.btnInstallPackInstaller.setOnClickListener {
            if (checkPermissionVersionTwo()){
                if (isDownloaded){
                    mainPresenter.installFileViaPackageInstaller()
                } else {
                    mainPresenter.downloadFile()
                }
            } else {
                this.requestPermissionsCompat(arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.REQUEST_INSTALL_PACKAGES), reqCode)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.reqCode){
            if (grantResults.isNotEmpty()){
                val write = grantResults[0]
                val read = grantResults[1]
                val install = grantResults[2]

                val checkWrite = write == PackageManager.PERMISSION_GRANTED
                val checkRead = read == PackageManager.PERMISSION_GRANTED
                val checkInstall = install == PackageManager.PERMISSION_GRANTED
                if (checkWrite && checkRead && checkInstall){
                    mainPresenter.downloadFile()
                } else {
                    Snackbar.make(binding.root, "Permission denied... Reinstall app", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun installUnknownSourcePermission(){
        //check and install permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !this.packageManager.canRequestPackageInstalls()){
            val intent = Intent().setAction(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                .setData(Uri.parse(String.format("package:%s", this.packageName)))
            unknownSourceResult.launch(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissionVersionTwo(): Boolean{
        val writePermission = checkSelfPermissionCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = checkSelfPermissionCompat(Manifest.permission.READ_EXTERNAL_STORAGE)
        val installPermission = checkSelfPermissionCompat(Manifest.permission.REQUEST_INSTALL_PACKAGES)
        return writePermission == PackageManager.PERMISSION_GRANTED && readPermission == PackageManager.PERMISSION_GRANTED
                &&  installPermission == PackageManager.PERMISSION_GRANTED
    }

    override fun installed() {
        Snackbar.make(binding.root, "Installation is finished successfully", Snackbar.LENGTH_SHORT).show()
        binding.btnInstallIntent.text = "File download and installed!"
        binding.btnInstallIntent.isEnabled = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun downloaded(isDownloaded: Boolean) {
        Snackbar.make(binding.root, "Download is finished successfully", Snackbar.LENGTH_SHORT).show()
        binding.btnInstallIntent.isEnabled = true
        binding.btnInstallIntent.text = "Install via Intent"
        this.isDownloaded = isDownloaded
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
//        unregisterReceiver(downloadCompleteReceiver)
    }
}